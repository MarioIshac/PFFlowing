package me.theeninja.pfflowing.bluetooth;

public class PutOperationType {
    public static final byte NEW_ACTION = 0;
    public static final byte REDO_ACTION = 1;
    public static final byte UNDO_ACTION = 2;

    private PutOperationType() {
        throw new IllegalStateException("No instance");
    }
}
