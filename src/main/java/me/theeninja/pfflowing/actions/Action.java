package me.theeninja.pfflowing.actions;

import javafx.scene.Node;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowing.Speech;
import me.theeninja.pfflowing.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.scene.layout.GridPane.getRowIndex;

/**
 * Represents an action that is both undoable and redoable.
 */
public abstract class Action<T> {
    protected final static int PREVIOUS_ROW_POSITION = 0;
    protected final static int FINAL_ROW_POSITION = 1;
    private final T scale;

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

    protected Action(T scale) {
        this.scale = scale;
    }

    private static final int IDENTIFIER_CHAR_LIMIT = 10;

    protected static String getActionIdentifier(FlowingRegion flowingRegion) {
        return flowingRegion.getFullText().substring(0, IDENTIFIER_CHAR_LIMIT);
    }

    public T getScale() {
        return this.scale;
    }
}
