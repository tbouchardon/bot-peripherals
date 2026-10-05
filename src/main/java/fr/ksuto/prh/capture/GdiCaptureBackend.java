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

/**
 * Capture Windows par GDI ({@code BitBlt} depuis l'écran vers une DIB), appelée directement par l'API native de Java
 * (FFM, {@code java.lang.foreign}) : pas de DLL à compiler. Bien plus rapide que {@link java.awt.Robot} sur les petites
 * zones (le QR code), où le Robot plafonne autour de 18 ms.
 * <p>
 * Chaque thread garde son contexte de dessin et sa DIB, recréée si la taille de la zone change.
 * Nécessite {@code --enable-native-access=ALL-UNNAMED}.
 */
public class GdiCaptureBackend implements CaptureBackend {

    private static final int SRCCOPY = 0x00CC0020;

    private static final MethodHandle GET_DC;
    private static final MethodHandle RELEASE_DC;
    private static final MethodHandle CREATE_COMPATIBLE_DC;
    private static final MethodHandle CREATE_DIB_SECTION;
    private static final MethodHandle SELECT_OBJECT;
    private static final MethodHandle BIT_BLT;
    private static final MethodHandle DELETE_OBJECT;
    private static final MethodHandle DELETE_DC;
    private static final MethodHandle GDI_FLUSH;

    static {
        Linker       linker = Linker.nativeLinker();
        SymbolLookup user32 = SymbolLookup.libraryLookup("user32", Arena.global());
        SymbolLookup gdi32  = SymbolLookup.libraryLookup("gdi32", Arena.global());
        ValueLayout  handle = ValueLayout.ADDRESS;
        ValueLayout  num    = ValueLayout.JAVA_INT;

        GET_DC = linker.downcallHandle(user32.findOrThrow("GetDC"), FunctionDescriptor.of(handle, handle));
        RELEASE_DC = linker.downcallHandle(user32.findOrThrow("ReleaseDC"), FunctionDescriptor.of(num, handle, handle));
        CREATE_COMPATIBLE_DC = linker.downcallHandle(gdi32.findOrThrow("CreateCompatibleDC"), FunctionDescriptor.of(handle, handle));
        CREATE_DIB_SECTION = linker.downcallHandle(gdi32.findOrThrow("CreateDIBSection"),
                                                   FunctionDescriptor.of(handle, handle, handle, num, handle, handle, num));
        SELECT_OBJECT = linker.downcallHandle(gdi32.findOrThrow("SelectObject"), FunctionDescriptor.of(handle, handle, handle));
        BIT_BLT = linker.downcallHandle(gdi32.findOrThrow("BitBlt"),
                                        FunctionDescriptor.of(num, handle, num, num, num, num, handle, num, num, num));
        DELETE_OBJECT = linker.downcallHandle(gdi32.findOrThrow("DeleteObject"), FunctionDescriptor.of(num, handle));
        DELETE_DC = linker.downcallHandle(gdi32.findOrThrow("DeleteDC"), FunctionDescriptor.of(num, handle));
        GDI_FLUSH = linker.downcallHandle(gdi32.findOrThrow("GdiFlush"), FunctionDescriptor.of(num));
    }

    /**
     * Contexte de dessin d'un thread : DC de l'écran, DC mémoire et DIB de la taille de la dernière zone capturée.
     */
    private static final class Context {

        final MemorySegment screenDc;
        final MemorySegment memoryDc;
        MemorySegment bitmap = MemorySegment.NULL;
        MemorySegment bits   = MemorySegment.NULL;
        int           width;
        int           height;

        Context() throws Throwable {

            screenDc = (MemorySegment) GET_DC.invokeExact(MemorySegment.NULL);
            memoryDc = (MemorySegment) CREATE_COMPATIBLE_DC.invokeExact(screenDc);
        }

        /**
         * DIB 32 bits de haut en bas (hauteur négative) : chaque pixel est un int 0x00RRGGBB, comme TYPE_INT_RGB.
         */
        void ensureSize(int newWidth, int newHeight) throws Throwable {

            if (newWidth == width && newHeight == height) {return;}
            if (bitmap.address() != 0) {int deleted = (int) DELETE_OBJECT.invokeExact(bitmap);}

            try (Arena arena = Arena.ofConfined()) {
                MemorySegment info = arena.allocate(44); // BITMAPINFOHEADER (40) + une couleur
                info.set(ValueLayout.JAVA_INT, 0, 40);
                info.set(ValueLayout.JAVA_INT, 4, newWidth);
                info.set(ValueLayout.JAVA_INT, 8, -newHeight);
                info.set(ValueLayout.JAVA_SHORT, 12, (short) 1);
                info.set(ValueLayout.JAVA_SHORT, 14, (short) 32);
                MemorySegment bitsPointer = arena.allocate(ValueLayout.ADDRESS);
                bitmap = (MemorySegment) CREATE_DIB_SECTION.invokeExact(memoryDc, info, 0, bitsPointer, MemorySegment.NULL, 0);
                if (bitmap.address() == 0) {throw new IllegalStateException("CreateDIBSection a échoué");}
                bits = bitsPointer.get(ValueLayout.ADDRESS, 0).reinterpret((long) newWidth * newHeight * 4);
            }
            MemorySegment previous = (MemorySegment) SELECT_OBJECT.invokeExact(memoryDc, bitmap);
            width = newWidth;
            height = newHeight;
        }
    }

    private final ThreadLocal<Context> contexts = ThreadLocal.withInitial(() -> {
        try {
            return new Context();
        }
        catch (Throwable e) {
            throw new IllegalStateException("Contexte GDI impossible à créer", e);
        }
    });

    /**
     * Échelle de l'affichage : Java donne des coordonnées logiques, GDI attend des pixels physiques.
     */
    private final double scaleX;
    private final double scaleY;

    public GdiCaptureBackend() {

        AffineTransform transform = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                                                       .getDefaultConfiguration().getDefaultTransform();
        scaleX = transform.getScaleX();
        scaleY = transform.getScaleY();
    }

    @Override
    public Frame capture(Rectangle screenZone) {

        int x      = (int) Math.round(screenZone.x * scaleX);
        int y      = (int) Math.round(screenZone.y * scaleY);
        int width  = (int) Math.round(screenZone.width * scaleX);
        int height = (int) Math.round(screenZone.height * scaleY);

        try {
            Context context = contexts.get();
            context.ensureSize(width, height);
            int copied = (int) BIT_BLT.invokeExact(context.memoryDc, 0, 0, width, height, context.screenDc, x, y, SRCCOPY);
            if (copied == 0) {throw new IllegalStateException("BitBlt a échoué");}
            int flushed = (int) GDI_FLUSH.invokeExact();

            BufferedImage image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[]         pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            MemorySegment.copy(context.bits, ValueLayout.JAVA_INT, 0, pixels, 0, width * height);
            return Frame.of(image, screenZone);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new IllegalStateException("Capture GDI impossible", e);
        }
    }
}
