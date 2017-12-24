package me.theeninja.pfflowing.gui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class KeyCodeCombinationUtils {
    public static final KeyCodeCombination REFUTE = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination EXTEND = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination NEXT = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_LEFT_ONLY = new KeyCodeCombination(KeyCode.LEFT);
    public static final KeyCodeCombination SELECT_RIGHT_ONLY = new KeyCodeCombination(KeyCode.RIGHT);
    public static final KeyCodeCombination SELECT_UP_ONLY = new KeyCodeCombination(KeyCode.UP);
    public static final KeyCodeCombination SELECT_DOWN_ONLY = new KeyCodeCombination(KeyCode.DOWN);
    public static final KeyCodeCombination SELECT_LEFT_TOO = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_RIGHT_TOO = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_UP_TOO = new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_DOWN_TOO = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination UNFOCUS = new KeyCodeCombination(KeyCode.ESCAPE);
    public static final KeyCodeCombination SWITCH = new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination RIGHT_SPEECH = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination LEFT_SPEECH = new KeyCodeCombination(KeyCode.LEFT, KeyCodeCombination.SHIFT_DOWN);
    public static final KeyCodeCombination WRITE = new KeyCodeCombination(KeyCode.W, KeyCodeCombination.CONTROL_DOWN);

}
