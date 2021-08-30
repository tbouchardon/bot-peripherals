package fr.ksuto.prh.peripherals;

import fr.ksuto.prh.properties.Constants;
import fr.ksuto.prh.tools.Debug;

import java.awt.*;
import java.awt.event.KeyEvent;

import com.google.inject.Inject;

/**
 * Created by thomas.bouchardon on 26/11/2015!
 */
@SuppressWarnings("unused")
public class Keyboard extends Peripheral {
    
    @Inject
    private MousePosition mousePosition;
    
    Keyboard() throws AWTException {
        
    }
    
    public void altTab() {
    
        mousePosition.waitIfUserActive();
        
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_TAB);
        robot.keyRelease(KeyEvent.VK_TAB);
        robot.keyRelease(KeyEvent.VK_ALT);
    }
    
    public void enter() {
    
        mousePosition.waitIfUserActive();
        
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        delay(Constants.i_DELAY);
    }
    
    public void pressKey(int keyEvent) {
        
        pressKey(keyEvent, false, false, false);
    }
    
    public void pressKey(int keyEvent, boolean alt, boolean ctrl, boolean shift) {
        
        if (alt) { robot.keyPress(KeyEvent.VK_ALT); }
        if (ctrl) { robot.keyPress(KeyEvent.VK_CONTROL); }
        if (shift) { robot.keyPress(KeyEvent.VK_SHIFT); }
        
        robot.delay(Constants.i_DELAY);
        robot.keyPress(keyEvent);
        robot.keyRelease(keyEvent);
        robot.delay(Constants.i_DELAY);
        
        if (shift) { robot.keyRelease(KeyEvent.VK_SHIFT); }
        if (ctrl) { robot.keyRelease(KeyEvent.VK_CONTROL); }
        if (alt) { robot.keyRelease(KeyEvent.VK_ALT); }
    }
    
    public void escape() {
    
        mousePosition.waitIfUserActive();
        
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.keyRelease(KeyEvent.VK_ESCAPE);
        delay(Constants.i_DELAY);
    }
    
    public void selectAll() {
    
        Debug.sout();
        mousePosition.waitIfUserActive();
        
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        delay(Constants.i_DELAY);
    }
    
    public void typeString(String strText) {
    
        mousePosition.waitIfUserActive();
        
        Debug.sout("typeString(" + strText + ")");
        
        for (char cLetter : strText.toCharArray()) {
            
            switch (cLetter) {
                case 'a':
                    robot.keyPress(KeyEvent.VK_A);
                    robot.keyRelease(KeyEvent.VK_A);
                    break;
                case 'b':
                    robot.keyPress(KeyEvent.VK_B);
                    robot.keyRelease(KeyEvent.VK_B);
                    break;
                case 'c':
                    robot.keyPress(KeyEvent.VK_C);
                    robot.keyRelease(KeyEvent.VK_C);
                    break;
                case 'd':
                    robot.keyPress(KeyEvent.VK_D);
                    robot.keyRelease(KeyEvent.VK_D);
                    break;
                case 'e':
                    robot.keyPress(KeyEvent.VK_E);
                    robot.keyRelease(KeyEvent.VK_E);
                    break;
                case 'f':
                    robot.keyPress(KeyEvent.VK_F);
                    robot.keyRelease(KeyEvent.VK_F);
                    break;
                case 'g':
                    robot.keyPress(KeyEvent.VK_G);
                    robot.keyRelease(KeyEvent.VK_G);
                    break;
                case 'h':
                    robot.keyPress(KeyEvent.VK_H);
                    robot.keyRelease(KeyEvent.VK_H);
                    break;
                case 'i':
                    robot.keyPress(KeyEvent.VK_I);
                    robot.keyRelease(KeyEvent.VK_I);
                    break;
                case 'j':
                    robot.keyPress(KeyEvent.VK_J);
                    robot.keyRelease(KeyEvent.VK_J);
                    break;
                case 'k':
                    robot.keyPress(KeyEvent.VK_K);
                    robot.keyRelease(KeyEvent.VK_K);
                    break;
                case 'l':
                    robot.keyPress(KeyEvent.VK_L);
                    robot.keyRelease(KeyEvent.VK_L);
                    break;
                case 'm':
                    robot.keyPress(KeyEvent.VK_M);
                    robot.keyRelease(KeyEvent.VK_M);
                    break;
                case 'n':
                    robot.keyPress(KeyEvent.VK_N);
                    robot.keyRelease(KeyEvent.VK_N);
                    break;
                case 'o':
                    robot.keyPress(KeyEvent.VK_O);
                    robot.keyRelease(KeyEvent.VK_O);
                    break;
                case 'p':
                    robot.keyPress(KeyEvent.VK_P);
                    robot.keyRelease(KeyEvent.VK_P);
                    break;
                case 'q':
                    robot.keyPress(KeyEvent.VK_Q);
                    robot.keyRelease(KeyEvent.VK_Q);
                    break;
                case 'r':
                    robot.keyPress(KeyEvent.VK_R);
                    robot.keyRelease(KeyEvent.VK_R);
                    break;
                case 's':
                    robot.keyPress(KeyEvent.VK_S);
                    robot.keyRelease(KeyEvent.VK_S);
                    break;
                case 't':
                    robot.keyPress(KeyEvent.VK_T);
                    robot.keyRelease(KeyEvent.VK_T);
                    break;
                case 'u':
                    robot.keyPress(KeyEvent.VK_U);
                    robot.keyRelease(KeyEvent.VK_U);
                    break;
                case 'v':
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_V);
                    break;
                case 'w':
                    robot.keyPress(KeyEvent.VK_W);
                    robot.keyRelease(KeyEvent.VK_W);
                    break;
                case 'x':
                    robot.keyPress(KeyEvent.VK_X);
                    robot.keyRelease(KeyEvent.VK_X);
                    break;
                case 'y':
                    robot.keyPress(KeyEvent.VK_Y);
                    robot.keyRelease(KeyEvent.VK_Y);
                    break;
                case 'z':
                    robot.keyPress(KeyEvent.VK_Z);
                    robot.keyRelease(KeyEvent.VK_Z);
                    break;
                case 'A':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_A);
                    robot.keyRelease(KeyEvent.VK_A);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'B':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_B);
                    robot.keyRelease(KeyEvent.VK_B);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'C':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_C);
                    robot.keyRelease(KeyEvent.VK_C);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'D':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_D);
                    robot.keyRelease(KeyEvent.VK_D);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'E':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_E);
                    robot.keyRelease(KeyEvent.VK_E);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'F':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_F);
                    robot.keyRelease(KeyEvent.VK_F);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'G':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_G);
                    robot.keyRelease(KeyEvent.VK_G);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'H':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_H);
                    robot.keyRelease(KeyEvent.VK_H);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'I':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_I);
                    robot.keyRelease(KeyEvent.VK_I);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'J':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_J);
                    robot.keyRelease(KeyEvent.VK_J);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'K':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_K);
                    robot.keyRelease(KeyEvent.VK_K);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'L':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_L);
                    robot.keyRelease(KeyEvent.VK_L);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'M':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_M);
                    robot.keyRelease(KeyEvent.VK_M);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'N':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_N);
                    robot.keyRelease(KeyEvent.VK_N);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'O':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_O);
                    robot.keyRelease(KeyEvent.VK_O);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'P':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_P);
                    robot.keyRelease(KeyEvent.VK_P);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'Q':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_Q);
                    robot.keyRelease(KeyEvent.VK_Q);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'R':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_R);
                    robot.keyRelease(KeyEvent.VK_R);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'S':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_S);
                    robot.keyRelease(KeyEvent.VK_S);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'T':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_T);
                    robot.keyRelease(KeyEvent.VK_T);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'U':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_U);
                    robot.keyRelease(KeyEvent.VK_U);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'V':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_V);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'W':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_W);
                    robot.keyRelease(KeyEvent.VK_W);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'X':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_X);
                    robot.keyRelease(KeyEvent.VK_X);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'Y':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_Y);
                    robot.keyRelease(KeyEvent.VK_Y);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case 'Z':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_Z);
                    robot.keyRelease(KeyEvent.VK_Z);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '0':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_0);
                    robot.keyRelease(KeyEvent.VK_0);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '1':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_1);
                    robot.keyRelease(KeyEvent.VK_1);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '2':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_2);
                    robot.keyRelease(KeyEvent.VK_2);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '3':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_3);
                    robot.keyRelease(KeyEvent.VK_3);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '4':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_4);
                    robot.keyRelease(KeyEvent.VK_4);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '5':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_5);
                    robot.keyRelease(KeyEvent.VK_5);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '6':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_6);
                    robot.keyRelease(KeyEvent.VK_6);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '7':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_7);
                    robot.keyRelease(KeyEvent.VK_7);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '8':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_8);
                    robot.keyRelease(KeyEvent.VK_8);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '9':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_9);
                    robot.keyRelease(KeyEvent.VK_9);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '&':
                    robot.keyPress(KeyEvent.VK_1);
                    robot.keyRelease(KeyEvent.VK_1);
                    break;
                case '"':
                    robot.keyPress(KeyEvent.VK_3);
                    robot.keyRelease(KeyEvent.VK_3);
                    break;
                case '\'':
                    robot.keyPress(KeyEvent.VK_4);
                    robot.keyRelease(KeyEvent.VK_4);
                    break;
                case '-':
                    robot.keyPress(KeyEvent.VK_6);
                    robot.keyRelease(KeyEvent.VK_6);
                    break;
                case ';':
                    robot.keyPress(KeyEvent.VK_SEMICOLON);
                    robot.keyRelease(KeyEvent.VK_SEMICOLON);
                    break;
                case ':':
                    robot.keyPress(KeyEvent.VK_COLON);
                    robot.keyRelease(KeyEvent.VK_COLON);
                    break;
                case '.':
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    robot.keyPress(KeyEvent.VK_SEMICOLON);
                    robot.keyRelease(KeyEvent.VK_SEMICOLON);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                    break;
                case '/':
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(KeyEvent.VK_NUMPAD0);
                    robot.keyRelease(KeyEvent.VK_NUMPAD0);
                    robot.keyPress(KeyEvent.VK_NUMPAD0);
                    robot.keyRelease(KeyEvent.VK_NUMPAD0);
                    robot.keyPress(KeyEvent.VK_NUMPAD4);
                    robot.keyRelease(KeyEvent.VK_NUMPAD4);
                    robot.keyPress(KeyEvent.VK_NUMPAD7);
                    robot.keyRelease(KeyEvent.VK_NUMPAD7);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    break;
                case ' ':
                    robot.keyPress(KeyEvent.VK_SPACE);
                    robot.keyRelease(KeyEvent.VK_SPACE);
                    break;
                case '!':
                    robot.keyPress(KeyEvent.VK_EXCLAMATION_MARK);
                    robot.keyRelease(KeyEvent.VK_EXCLAMATION_MARK);
                    break;
                case '@':
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(KeyEvent.VK_NUMPAD0);
                    robot.keyRelease(KeyEvent.VK_NUMPAD0);
                    robot.keyPress(KeyEvent.VK_NUMPAD0);
                    robot.keyRelease(KeyEvent.VK_NUMPAD0);
                    robot.keyPress(KeyEvent.VK_NUMPAD6);
                    robot.keyRelease(KeyEvent.VK_NUMPAD6);
                    robot.keyPress(KeyEvent.VK_NUMPAD4);
                    robot.keyRelease(KeyEvent.VK_NUMPAD4);
                    robot.keyRelease(KeyEvent.VK_ALT);
                    break;
            }
            delay(Constants.i_TYPING_DELAY);
        }
        delay(Constants.i_DELAY);
    }
    
    public void f1() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F1);
        robot.keyRelease(KeyEvent.VK_F1);
        delay(Constants.i_DELAY);
    }
    
    public void f2() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F2);
        robot.keyRelease(KeyEvent.VK_F2);
        delay(Constants.i_DELAY);
    }
    
    public void f3() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F3);
        robot.keyRelease(KeyEvent.VK_F3);
        delay(Constants.i_DELAY);
    }
    
    public void f4() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F4);
        robot.keyRelease(KeyEvent.VK_F4);
        delay(Constants.i_DELAY);
    }
    
    public void f5() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F5);
        robot.keyRelease(KeyEvent.VK_F5);
        delay(Constants.i_DELAY);
    }
    
    public void f6() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F6);
        robot.keyRelease(KeyEvent.VK_F6);
        delay(Constants.i_DELAY);
    }
    
    public void f7() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F7);
        robot.keyRelease(KeyEvent.VK_F7);
        delay(Constants.i_DELAY);
    }
    
    public void f8() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F8);
        robot.keyRelease(KeyEvent.VK_F8);
        delay(Constants.i_DELAY);
    }
    
    public void f9() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F9);
        robot.keyRelease(KeyEvent.VK_F9);
        delay(Constants.i_DELAY);
    }
    
    public void f10() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F10);
        robot.keyRelease(KeyEvent.VK_F10);
        delay(Constants.i_DELAY);
    }
    
    public void f11() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F11);
        robot.keyRelease(KeyEvent.VK_F11);
        delay(Constants.i_DELAY);
    }
    
    public void f12() {
    
        mousePosition.waitIfUserActive();
        robot.keyPress(KeyEvent.VK_F12);
        robot.keyRelease(KeyEvent.VK_F12);
        delay(Constants.i_DELAY);
    }
}
    
  
