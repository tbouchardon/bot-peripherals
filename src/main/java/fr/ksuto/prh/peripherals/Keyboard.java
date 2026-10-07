package fr.ksuto.prh.peripherals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.Random;

import com.google.inject.Inject;

/**
 * Clavier, avec des gestes humains ({@link HumanTiming}) : modificateur enfoncé un peu avant la touche, touche tenue
 * quelques dizaines de millisecondes, durées différentes à chaque fois.
 * <p>
 * {@link #typeString} tape caractère par caractère. Sous Windows, chaque caractère est cherché sur la disposition active
 * du clavier (AZERTY, QWERTY...) et envoyé comme une vraie touche ({@link WindowsKeys}) ; un caractère qu'aucune touche
 * ne produit passe par la saisie Unicode. Ailleurs, la touche Java du caractère, Maj pour les majuscules.
 */
@SuppressWarnings("unused")
public class Keyboard extends Peripheral {

    /**
     * Le clavier natif de Windows est disponible.
     */
    static final boolean WINDOWS = System.getProperty("os.name", "").startsWith("Windows");

    private static final Logger logger = LoggerFactory.getLogger(Keyboard.class);

    @Inject
    private MousePosition mousePosition;
    private final Random  random = new Random();
    private final int     typingDelay;

    Keyboard() throws AWTException {

        super();
        typingDelay = Integer.parseInt(properties.getProperty("ksuto.prh.peripherals.keyboard.typing.delay", "50"));
    }

    private void waitIfUserActive() {

        if (mousePosition != null) {mousePosition.waitIfUserActive();}
    }

    public void altTab() {

        waitIfUserActive();
        pressKey(KeyEvent.VK_TAB, true, false, false);
    }

    public void enter() {

        waitIfUserActive();
        pressKey(KeyEvent.VK_ENTER);
    }

    public void escape() {

        waitIfUserActive();
        pressKey(KeyEvent.VK_ESCAPE);
    }

    /**
     * Touche de fonction F1 à F12.
     */
    public void functionKey(int number) {

        if (number < 1 || number > 12) {throw new IllegalArgumentException("Touche de fonction F1 à F12 : F" + number);}
        waitIfUserActive();
        pressKey(KeyEvent.VK_F1 + number - 1);
    }

    public void f1() {functionKey(1);}

    public void f2() {functionKey(2);}

    public void f3() {functionKey(3);}

    public void f4() {functionKey(4);}

    public void f5() {functionKey(5);}

    public void f6() {functionKey(6);}

    public void f7() {functionKey(7);}

    public void f8() {functionKey(8);}

    public void f9() {functionKey(9);}

    public void f10() {functionKey(10);}

    public void f11() {functionKey(11);}

    public void f12() {functionKey(12);}

    public void pressKey(int keyEvent) {

        pressKey(keyEvent, false, false, false);
    }

    public void pressKey(int keyEvent, boolean alt, boolean ctrl, boolean shift) {

        pressKey(keyEvent, alt, ctrl, shift, 0);
    }

    /**
     * Appuie sur une touche (code Java {@link KeyEvent}), avec ses modificateurs, comme une main : modificateurs d'abord,
     * touche tenue, modificateurs relâchés juste après.
     *
     * @param duration durée d'appui en ms ; 0 = durée humaine tirée au hasard ({@link HumanTiming#hold})
     */
    public void pressKey(int keyEvent, boolean alt, boolean ctrl, boolean shift, int duration) {

        boolean modified = alt || ctrl || shift;
        // Relâchement garanti, même en cas d'erreur : un modificateur resté enfoncé fausserait toutes les frappes suivantes
        try {
            if (alt) {robot.keyPress(KeyEvent.VK_ALT);}
            if (ctrl) {robot.keyPress(KeyEvent.VK_CONTROL);}
            if (shift) {robot.keyPress(KeyEvent.VK_SHIFT);}
            if (modified) {delay(HumanTiming.modifierLead(random));}

            robot.keyPress(keyEvent);
            try {
                delay(duration > 0 ? duration : HumanTiming.hold(random));
            }
            finally {
                robot.keyRelease(keyEvent);
            }
            if (modified) {delay(HumanTiming.modifierLag(random));}
        }
        finally {
            if (shift) {robot.keyRelease(KeyEvent.VK_SHIFT);}
            if (ctrl) {robot.keyRelease(KeyEvent.VK_CONTROL);}
            if (alt) {robot.keyRelease(KeyEvent.VK_ALT);}
        }
        delay(HumanTiming.modifierLag(random));
    }

    public void selectAll() {

        waitIfUserActive();
        pressKey(KeyEvent.VK_A, false, true, false);
    }

    /**
     * Tape un texte caractère par caractère, avec des intervalles humains ({@code ksuto.prh.peripherals.keyboard.typing.delay}
     * à ±50 %).
     */
    public void typeString(String text) {

        waitIfUserActive();
        logger.info("typeString({})", text);

        for (char character : text.toCharArray()) {
            if (WINDOWS) {typeOnWindows(character);}
            else {typeWithRobot(character);}
            delay(HumanTiming.betweenCharacters(random, typingDelay));
        }
        delay(i_DELAY);
    }

    /**
     * La touche du caractère sur la disposition active, avec ses modificateurs ; saisie Unicode s'il n'y en a pas.
     */
    private void typeOnWindows(char character) {

        Optional<WindowsKeys.Stroke> stroke = WindowsKeys.strokeFor(character);
        if (stroke.isEmpty()) {
            WindowsKeys.unicode(character, false);
            delay(HumanTiming.hold(random));
            WindowsKeys.unicode(character, true);
            return;
        }
        WindowsKeys.Stroke key = stroke.get();
        boolean modified = key.shift() || key.ctrl() || key.alt();
        try {
            if (key.ctrl()) {WindowsKeys.key(WindowsKeys.VK_CONTROL, false);}
            if (key.alt()) {WindowsKeys.key(WindowsKeys.VK_MENU, false);}
            if (key.shift()) {WindowsKeys.key(WindowsKeys.VK_SHIFT, false);}
            if (modified) {delay(HumanTiming.modifierLead(random));}
            WindowsKeys.key(key.vk(), false);
            try {
                delay(HumanTiming.hold(random));
            }
            finally {
                WindowsKeys.key(key.vk(), true);
            }
            if (modified) {delay(HumanTiming.modifierLag(random));}
        }
        finally {
            if (key.shift()) {WindowsKeys.key(WindowsKeys.VK_SHIFT, true);}
            if (key.alt()) {WindowsKeys.key(WindowsKeys.VK_MENU, true);}
            if (key.ctrl()) {WindowsKeys.key(WindowsKeys.VK_CONTROL, true);}
        }
    }

    /**
     * Hors Windows : la touche Java du caractère (disposition QWERTY supposée pour la ponctuation), Maj pour les
     * majuscules.
     */
    private void typeWithRobot(char character) {

        int keyCode = KeyEvent.getExtendedKeyCodeForChar(character);
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            logger.warn("Caractère sans touche, ignoré : {}", character);
            return;
        }
        try {
            pressKey(keyCode, false, false, Character.isUpperCase(character));
        }
        catch (IllegalArgumentException e) {
            logger.warn("Caractère impossible à taper ici, ignoré : {} ({})", character, e.getMessage());
        }
    }
}
