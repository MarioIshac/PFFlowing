package me.theeninja.pfflowing;

import me.theeninja.pfflowing.actions.Action;

import java.util.*;

public class ActionManager {
    private final LinkedList<Action<?>> doneActions = new LinkedList<>();
    private final LinkedList<Action<?>> undoneActions = new LinkedList<>();

    public void perform(Action<?> action) {
        action.execute();
        getDoneActions().push(action);
    }

    public void undo() {
        if (getDoneActions().isEmpty())
            return;

        Action<?> action = getDoneActions().pop();
        action.unexecute();
        getUndoneActions().push(action);
    }

    public void redo() {
        if (getUndoneActions().isEmpty())
            return;

        Action<?> action = getUndoneActions().pop();
        action.execute();
        getDoneActions().push(action);
    }

    /**
     * Done
     * Write
     *
     *
     */

    public LinkedList<Action<?>> getDoneActions() {
        return this.doneActions;
    }

    public LinkedList<Action<?>> getUndoneActions() {
        return undoneActions;
    }
}
