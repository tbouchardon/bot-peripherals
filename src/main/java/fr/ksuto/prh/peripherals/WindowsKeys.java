package fr.ksuto.prh.peripherals;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

/**
 * Clavier Windows par l'API native de Java (FFM, {@code user32}), sans DLL à compiler :
 * <ul>
 *   <li>{@code VkKeyScanW} : la touche et les modificateurs qui produisent un caractère <b>sur la disposition active</b>
 *   (AZERTY, QWERTY...) ;</li>
 *   <li>{@code SendInput} : l'envoi de la touche, code virtuel Windows et code de balayage, comme un vrai clavier.</li>
 * </ul>
 * Nécessite {@code --enable-native-access=ALL-UNNAMED}. La classe charge {@code user32} à son initialisation : ne
 * l'utiliser que sous Windows ({@link Keyboard#WINDOWS}) ; {@link Stroke#decode} reste utilisable partout.
 */
final class WindowsKeys {

    static final int VK_SHIFT = 0x10, VK_CONTROL = 0x11, VK_MENU = 0x12;

    private static final int INPUT_KEYBOARD   = 1;
    private static final int KEYEVENTF_KEYUP  = 0x0002;
    private static final int KEYEVENTF_UNICODE = 0x0004;
    private static final int MAPVK_VK_TO_VSC  = 0;

    /**
     * Taille d'une structure INPUT en 64 bits : type (4 octets, aligné sur 8), puis l'union, dont la plus grande variante
     * (MOUSEINPUT) fait 32 octets.
     */
    static final long INPUT_SIZE = 40;

    private static final MethodHandle VK_KEY_SCAN;
    private static final MethodHandle MAP_VIRTUAL_KEY;
    private static final MethodHandle SEND_INPUT;

    static {
        Linker       linker = Linker.nativeLinker();
        SymbolLookup user32 = SymbolLookup.libraryLookup("user32", Arena.global());
        VK_KEY_SCAN = linker.downcallHandle(user32.findOrThrow("VkKeyScanW"),
                                            FunctionDescriptor.of(ValueLayout.JAVA_SHORT, ValueLayout.JAVA_CHAR));
        MAP_VIRTUAL_KEY = linker.downcallHandle(user32.findOrThrow("MapVirtualKeyW"),
                                                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        SEND_INPUT = linker.downcallHandle(user32.findOrThrow("SendInput"),
                                           FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    }

    /**
     * Touche qui produit un caractère, avec ses modificateurs (AltGr = Ctrl + Alt).
     *
     * @param vk code virtuel Windows
     */
    record Stroke(int vk, boolean shift, boolean ctrl, boolean alt) {

        /**
         * @param scan résultat de {@code VkKeyScanW} : octet faible = code virtuel, octet fort = modificateurs (1 Maj,
         *             2 Ctrl, 4 Alt) ; -1 si aucune touche ne produit le caractère
         */
        static Optional<Stroke> decode(short scan) {

            if (scan == -1) {return Optional.empty();}
            int vk = scan & 0xFF, state = scan >> 8 & 0xFF;
            if (vk == 0xFF || state > 7) {return Optional.empty();} // état réservé (touche morte, hankaku...)
            return Optional.of(new Stroke(vk, (state & 1) != 0, (state & 2) != 0, (state & 4) != 0));
        }
    }

    private WindowsKeys() {}

    /**
     * @return la touche qui produit ce caractère sur la disposition active, vide s'il n'y en a pas
     */
    static Optional<Stroke> strokeFor(char character) {

        try {
            return Stroke.decode((short) VK_KEY_SCAN.invokeExact(character));
        }
        catch (Throwable e) {
            throw new IllegalStateException("VkKeyScanW", e);
        }
    }

    /**
     * Appuie (ou relâche) une touche par son code virtuel Windows.
     */
    static void key(int vk, boolean up) {

        try {
            int scan = (int) MAP_VIRTUAL_KEY.invokeExact(vk, MAPVK_VK_TO_VSC);
            send((short) vk, (short) scan, up ? KEYEVENTF_KEYUP : 0);
        }
        catch (Throwable e) {
            throw new IllegalStateException("SendInput", e);
        }
    }

    /**
     * Tape un caractère qu'aucune touche de la disposition ne produit (lettre d'un autre alphabet...) : saisie Unicode.
     */
    static void unicode(char character, boolean up) {

        send((short) 0, (short) character, KEYEVENTF_UNICODE | (up ? KEYEVENTF_KEYUP : 0));
    }

    private static void send(short vk, short scan, int flags) {

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment input = arena.allocate(INPUT_SIZE, 8);
            input.set(ValueLayout.JAVA_INT, 0, INPUT_KEYBOARD);
            // KEYBDINPUT, à partir de l'octet 8 : wVk, wScan, dwFlags, time, dwExtraInfo
            input.set(ValueLayout.JAVA_SHORT, 8, vk);
            input.set(ValueLayout.JAVA_SHORT, 10, scan);
            input.set(ValueLayout.JAVA_INT, 12, flags);
            int sent = (int) SEND_INPUT.invokeExact(1, input, (int) INPUT_SIZE);
            if (sent != 1) {throw new IllegalStateException("SendInput a refusé la touche (entrée bloquée par une autre application ?)");}
        }
        catch (IllegalStateException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new IllegalStateException("SendInput", e);
        }
    }
}
