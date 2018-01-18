package me.theeninja.pfflowing;

/**
 * Represents an action that is both undoable and redoable.
 */
public abstract class Action {

    /**
     * Executions the procedure outlined in the Action through this method.
     */
    public abstract void execute();

    /**
     * Performs the exact opposite of the above procedure, restoring the previous
     * state of the environment to the point where execution and unexecution of
     * a certain action should not alter the environment in any way. It is up
     * to the implementor to make sure that unexecution of an Action guarantees
     * exact reversal of execution.
     */
    public abstract void unexecute();

    /**
     * @return The name of this action, which it goes by.
     */
    public abstract String getName();
}
