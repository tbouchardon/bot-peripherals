package fr.ksuto.prh.capture;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capture Windows par DXGI Desktop Duplication : la carte graphique fournit chaque nouvelle image du bureau, copiée
 * dans une texture lisible par le processeur ; une capture ne lit ensuite que la zone demandée. Appelée directement
 * par l'API native de Java (FFM) et les interfaces COM de Direct3D 11, sans DLL à compiler.
 * <p>
 * Une capture renvoie la dernière image affichée : si l'écran n'a pas changé depuis, l'image précédente, gardée dans la
 * texture, est relue sans attendre. Écran principal uniquement. Les appels sont sérialisés (contexte Direct3D non
 * partageable entre threads). Une seule instance par processus : Windows refuse une seconde duplication du même écran
 * ({@code DuplicateOutput} échoue avec E_INVALIDARG). Nécessite {@code --enable-native-access=ALL-UNNAMED}.
 */
public class DxgiCaptureBackend implements CaptureBackend, AutoCloseable {

    // Indices dans les tables de méthodes COM (ordre de déclaration des interfaces, méthodes héritées comprises)
    private static final int RELEASE                   = 2;
    private static final int QUERY_INTERFACE           = 0;
    private static final int DXGI_DEVICE_GET_ADAPTER   = 7;
    private static final int ADAPTER_ENUM_OUTPUTS      = 7;
    private static final int OUTPUT_GET_DESC           = 7;
    private static final int OUTPUT1_DUPLICATE_OUTPUT  = 22;
    private static final int DUPLICATION_ACQUIRE_FRAME = 8;
    private static final int DUPLICATION_RELEASE_FRAME = 14;
    private static final int DEVICE_CREATE_TEXTURE_2D  = 5;
    private static final int CONTEXT_MAP               = 14;
    private static final int CONTEXT_UNMAP             = 15;
    private static final int CONTEXT_COPY_RESOURCE     = 47;

    private static final int WAIT_TIMEOUT = 0x887A0027;
    private static final int ACCESS_LOST  = 0x887A0026;

    private static final MemorySegment IID_DXGI_DEVICE   = guid(0x54ec77fa, 0x1377, 0x44e6, 0x8c, 0x32, 0x88, 0xfd, 0x5f, 0x44, 0xc8, 0x4c);
    private static final MemorySegment IID_TEXTURE_2D    = guid(0x6f15aaf2, 0xd208, 0x4e89, 0x9a, 0xb4, 0x48, 0x95, 0x35, 0xd3, 0x4f, 0x9c);

    private static final Linker       LINKER = Linker.nativeLinker();

    private static final Map<FunctionDescriptor, MethodHandle> CALLERS = new ConcurrentHashMap<>();
    private static final MethodHandle D3D11_CREATE_DEVICE;

    static {
        SymbolLookup d3d11 = SymbolLookup.libraryLookup("d3d11", Arena.global());
        D3D11_CREATE_DEVICE = LINKER.downcallHandle(d3d11.findOrThrow("D3D11CreateDevice"),
                                                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                                                                          ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                                                                          ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                                                                          ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    }

    private final Arena  arena = Arena.ofShared();
    private final double scaleX;
    private final double scaleY;

    private MemorySegment device;
    private MemorySegment context;
    private MemorySegment duplication;
    private MemorySegment staging;
    private int           desktopWidth;
    private int           desktopHeight;
    private boolean       hasFrame;

    public DxgiCaptureBackend() {

        AffineTransform transform = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                                                       .getDefaultConfiguration().getDefaultTransform();
        scaleX = transform.getScaleX();
        scaleY = transform.getScaleY();
        open();
    }

    @Override
    public synchronized Frame capture(Rectangle screenZone) {

        int x      = (int) Math.round(screenZone.x * scaleX);
        int y      = (int) Math.round(screenZone.y * scaleY);
        int width  = (int) Math.round(screenZone.width * scaleX);
        int height = (int) Math.round(screenZone.height * scaleY);

        refresh();

        BufferedImage image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[]         pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        try (Arena call = Arena.ofConfined()) {
            MemorySegment mapped = call.allocate(16); // D3D11_MAPPED_SUBRESOURCE : pData, RowPitch, DepthPitch
            check((int) invoke(context, CONTEXT_MAP, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                                                                            ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
                                                                            ValueLayout.ADDRESS),
                               context, staging, 0, 1, 0, mapped), "Map");
            try {
                int           rowPitch = mapped.get(ValueLayout.JAVA_INT, 8);
                MemorySegment data     = mapped.get(ValueLayout.ADDRESS, 0).reinterpret((long) rowPitch * desktopHeight);
                // Pixels BGRA : lus comme des int petit-boutistes, ils donnent 0xAARRGGBB, l'alpha étant ignoré par TYPE_INT_RGB
                for (int row = 0; row < height; row++) {
                    MemorySegment.copy(data, ValueLayout.JAVA_INT, (long) (y + row) * rowPitch + (long) x * 4, pixels, row * width, width);
                }
            }
            finally {
                invoke(context, CONTEXT_UNMAP, FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
                       context, staging, 0);
            }
        }
        return Frame.of(image, screenZone);
    }

    /**
     * Copie la nouvelle image du bureau dans la texture lisible, s'il y en a une ; sinon la texture garde la précédente.
     */
    private void refresh() {

        try (Arena call = Arena.ofConfined()) {
            MemorySegment info     = call.allocate(64); // DXGI_OUTDUPL_FRAME_INFO (48 octets)
            MemorySegment resource = call.allocate(ValueLayout.ADDRESS);
            // Première capture : attendre une image ; ensuite, ne pas attendre
            int result = (int) invoke(duplication, DUPLICATION_ACQUIRE_FRAME,
                                      FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                                                            ValueLayout.ADDRESS),
                                      duplication, hasFrame ? 0 : 500, info, resource);
            if (result == WAIT_TIMEOUT) {return;}
            if (result == ACCESS_LOST) {
                // Changement de mode d'affichage, écran de verrouillage... : on recrée la duplication
                close();
                open();
                return;
            }
            check(result, "AcquireNextFrame");
            MemorySegment desktop = resource.get(ValueLayout.ADDRESS, 0);
            try {
                MemorySegment texture = queryInterface(desktop, IID_TEXTURE_2D);
                invoke(context, CONTEXT_COPY_RESOURCE, FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
                       context, staging, texture);
                release(texture);
                hasFrame = true;
            }
            finally {
                release(desktop);
                invoke(duplication, DUPLICATION_RELEASE_FRAME, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS), duplication);
            }
        }
    }

    private void open() {

        try (Arena call = Arena.ofConfined()) {
            MemorySegment devicePointer  = call.allocate(ValueLayout.ADDRESS);
            MemorySegment contextPointer = call.allocate(ValueLayout.ADDRESS);
            int created;
            try {
                created = (int) D3D11_CREATE_DEVICE.invokeExact(MemorySegment.NULL, 1 /* D3D_DRIVER_TYPE_HARDWARE */, MemorySegment.NULL, 0,
                                                                MemorySegment.NULL, 0, 7 /* D3D11_SDK_VERSION */, devicePointer, MemorySegment.NULL,
                                                                contextPointer);
            }
            catch (Throwable e) {
                throw new IllegalStateException("D3D11CreateDevice impossible", e);
            }
            check(created, "D3D11CreateDevice");
            device = devicePointer.get(ValueLayout.ADDRESS, 0);
            context = contextPointer.get(ValueLayout.ADDRESS, 0);

            MemorySegment dxgiDevice = queryInterface(device, IID_DXGI_DEVICE);
            MemorySegment adapter    = outParameter(dxgiDevice, DXGI_DEVICE_GET_ADAPTER, "GetAdapter");
            release(dxgiDevice);

            MemorySegment outputPointer = call.allocate(ValueLayout.ADDRESS);
            check((int) invoke(adapter, ADAPTER_ENUM_OUTPUTS,
                               FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS),
                               adapter, 0, outputPointer), "EnumOutputs");
            release(adapter);
            MemorySegment output = outputPointer.get(ValueLayout.ADDRESS, 0);

            // DXGI_OUTPUT_DESC : DeviceName[32] (WCHAR, 64 octets), puis DesktopCoordinates (RECT : left, top, right, bottom)
            MemorySegment desc = call.allocate(96);
            check((int) invoke(output, OUTPUT_GET_DESC, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
                               output, desc), "GetDesc");
            desktopWidth = desc.get(ValueLayout.JAVA_INT, 72) - desc.get(ValueLayout.JAVA_INT, 64);
            desktopHeight = desc.get(ValueLayout.JAVA_INT, 76) - desc.get(ValueLayout.JAVA_INT, 68);

            // IDXGIOutput hérite en ligne droite jusqu'à IDXGIOutput6 : la table de méthodes contient déjà DuplicateOutput
            MemorySegment duplicationPointer = call.allocate(ValueLayout.ADDRESS);
            check((int) invoke(output, OUTPUT1_DUPLICATE_OUTPUT,
                               FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
                               output, device, duplicationPointer), "DuplicateOutput");
            release(output);
            duplication = duplicationPointer.get(ValueLayout.ADDRESS, 0);

            // Texture lisible par le processeur, de la taille du bureau : D3D11_TEXTURE2D_DESC
            MemorySegment textureDesc = call.allocate(44);
            int[] fields = {desktopWidth, desktopHeight, 1, 1, 87 /* B8G8R8A8_UNORM */, 1, 0, 3 /* STAGING */, 0, 0x20000 /* CPU_ACCESS_READ */, 0};
            for (int i = 0; i < fields.length; i++) {textureDesc.set(ValueLayout.JAVA_INT, i * 4L, fields[i]);}
            MemorySegment stagingPointer = call.allocate(ValueLayout.ADDRESS);
            check((int) invoke(device, DEVICE_CREATE_TEXTURE_2D,
                               FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
                               device, textureDesc, MemorySegment.NULL, stagingPointer), "CreateTexture2D");
            staging = stagingPointer.get(ValueLayout.ADDRESS, 0);
            hasFrame = false;
        }
    }

    @Override
    public synchronized void close() {

        for (MemorySegment object : new MemorySegment[]{staging, duplication, context, device}) {
            if (object != null && object.address() != 0) {release(object);}
        }
        staging = duplication = context = device = null;
    }

    /**
     * Appelle la méthode d'indice {@code index} de l'objet COM (le premier argument est l'objet lui-même).
     */
    private static Object invoke(MemorySegment object, int index, FunctionDescriptor descriptor, Object... arguments) {

        MemorySegment table  = object.reinterpret(ValueLayout.ADDRESS.byteSize()).get(ValueLayout.ADDRESS, 0)
                                     .reinterpret((index + 1L) * ValueLayout.ADDRESS.byteSize());
        MemorySegment method = table.getAtIndex(ValueLayout.ADDRESS, index);
        // Un appelant par signature, l'adresse de la méthode en premier argument : le créer coûte cher, pas l'appeler
        MethodHandle caller = CALLERS.computeIfAbsent(descriptor, LINKER::downcallHandle);
        Object[]     all    = new Object[arguments.length + 1];
        all[0] = method;
        System.arraycopy(arguments, 0, all, 1, arguments.length);
        try {
            return caller.invokeWithArguments(all);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new IllegalStateException("Appel COM impossible (méthode " + index + ")", e);
        }
    }

    private static MemorySegment queryInterface(MemorySegment object, MemorySegment iid) {

        try (Arena call = Arena.ofConfined()) {
            MemorySegment pointer = call.allocate(ValueLayout.ADDRESS);
            check((int) invoke(object, QUERY_INTERFACE,
                               FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
                               object, iid, pointer), "QueryInterface");
            return pointer.get(ValueLayout.ADDRESS, 0);
        }
    }

    /**
     * Méthode sans argument qui rend un objet par un pointeur de sortie (GetAdapter...).
     */
    private static MemorySegment outParameter(MemorySegment object, int index, String name) {

        try (Arena call = Arena.ofConfined()) {
            MemorySegment pointer = call.allocate(ValueLayout.ADDRESS);
            check((int) invoke(object, index, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS), object, pointer),
                  name);
            return pointer.get(ValueLayout.ADDRESS, 0);
        }
    }

    private static void release(MemorySegment object) {

        invoke(object, RELEASE, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS), object);
    }

    private static void check(int result, String call) {

        if (result < 0) {throw new IllegalStateException(call + " a échoué : 0x" + Integer.toHexString(result));}
    }

    private static MemorySegment guid(int data1, int data2, int data3, int... data4) {

        MemorySegment guid = Arena.global().allocate(16);
        guid.set(ValueLayout.JAVA_INT, 0, data1);
        guid.set(ValueLayout.JAVA_SHORT, 4, (short) data2);
        guid.set(ValueLayout.JAVA_SHORT, 6, (short) data3);
        for (int i = 0; i < 8; i++) {guid.set(ValueLayout.JAVA_BYTE, 8 + i, (byte) data4[i]);}
        return guid;
    }
}
