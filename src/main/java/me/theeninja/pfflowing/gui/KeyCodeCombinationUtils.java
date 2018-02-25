package me.theeninja.pfflowing.gui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class KeyCodeCombinationUtils {
    // Display-Management Keys
    public static final KeyCodeCombination UPSCALE_BY_1 = new KeyCodeCombination(KeyCode.UP,   KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination NARROW_BY_1  = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

    public static final KeyCodeCombination SHIFT_DISPLAY_RIGHT = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination SHIFT_DISPLAY_LEFT  = new KeyCodeCombination(KeyCode.LEFT,  KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

    // Flow-Management Keys
    public static final KeyCodeCombination REFUTE = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination EXTEND = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination ORGANIZE = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination WRITE = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination TOGGLE_CASE_WRITE = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);
    public static final KeyCodeCombination DROP = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination MERGE = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination QUESTION = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination EDIT = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

    public static final KeyCombination.Modifier TOGGLE_SELECT_MULTIPLE = KeyCombination.CONTROL_DOWN;

    public static final KeyCodeCombination SELECT_UP_ONLY = new KeyCodeCombination(KeyCode.UP);
    public static final KeyCodeCombination SELECT_DOWN_ONLY = new KeyCodeCombination(KeyCode.DOWN);
    public static final KeyCodeCombination SELECT_RIGHT_ONLY = new KeyCodeCombination(KeyCode.RIGHT);
    public static final KeyCodeCombination SELECT_LEFT_ONLY = new KeyCodeCombination(KeyCode.LEFT);

    public static final KeyCodeCombination SELECT_UP_TOO = new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_DOWN_TOO = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_RIGHT_TOO = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_LEFT_TOO = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);

    public static final KeyCodeCombination UNFOCUS = new KeyCodeCombination(KeyCode.ESCAPE);

    public static final KeyCodeCombination SWITCH_SPEECHLIST = new KeyCodeCombination(KeyCode.N, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination SELECT_LEFT_SPEECH = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination SELECT_RIGHT_SPEECH = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_DOWN);

    // Application keys
    public static final KeyCodeCombination TOGGLE_FULLSCREEN = new KeyCodeCombination(KeyCode.F11);

    // Undo and Redo
    public static final KeyCodeCombination UNDO = new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN);
    public static final KeyCodeCombination REDO = new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN);

    public static final KeyCodeCombination DELETE = new KeyCodeCombination(KeyCode.DELETE, KeyCodeCombination.CONTROL_DOWN);

    public static final KeyCodeCombination EXPAND = new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN);
    public static final KeyCodeCombination SELECT_ALL = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);

    public static final KeyCodeCombination SAVE = new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);;
    public static final KeyCodeCombination NEW = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    public static final KeyCodeCombination OPEN = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);


}


