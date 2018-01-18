package me.theeninja.pfflowing;

import java.util.Stack;

public class ActionManager {
    private final Stack<Action> doneActions;
    private final Stack<Action> undoneActions;

    ActionManager() {
        doneActions = new Stack<>();
        undoneActions = new Stack<>();
    }

    public void perform(Action action) {
        action.execute();
        getDoneActions().push(action);
    }

    public void undo() {
        if (getDoneActions().empty())
            return;

        Action action = getDoneActions().pop();
        action.unexecute();
        getUndoneActions().push(action);
    }

    public void redo() {
        if (getUndoneActions().empty())
            return;

        Action action = getUndoneActions().pop();
        action.execute();
        getDoneActions().push(action);
    }

    public Stack<Action> getDoneActions() {
        return this.doneActions;
    }

    public Stack<Action> getUndoneActions() {
        return undoneActions;
    }
}
