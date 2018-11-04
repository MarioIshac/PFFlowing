package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.actions.*;
import me.theeninja.pfflowing.gui.FlowController;

public class PutOperationScale {
    public static final byte AFF_SCALE = 0;
    public static final byte NEG_SCALE = 1;

    public static boolean isSideScale(byte scale) {
        return scale == AFF_SCALE || scale == NEG_SCALE;
    }

    @SuppressWarnings("unchecked")
    private static final Class<? extends Action>[] ACTION_SUBCLASSES = new Class[] {
        Delete.class,
        Drop.class,
        Edit.class,
        Extend.class,
        Merge.class,
        ModifyCard.class,
        ProactiveWrite.class,
        Question.class,
        Refute.class,
        Split.class
    };

    public static boolean isActionClass(byte actionClassRepresentation) {
        return 0 <= actionClassRepresentation && actionClassRepresentation < ACTION_SUBCLASSES.length;
    }

    public static Class<? extends Action> getActionClass(byte actionClassRepresentation) {
        return ACTION_SUBCLASSES[actionClassRepresentation];
    }
}
