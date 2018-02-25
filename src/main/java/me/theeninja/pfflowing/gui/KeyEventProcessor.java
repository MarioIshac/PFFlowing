package me.theeninja.pfflowing.gui;

import com.google.common.collect.ImmutableMap;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import me.theeninja.pfflowing.flowing.FlowingRegion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static me.theeninja.pfflowing.gui.KeyCodeCombinationUtils.*;

public class KeyEventProcessor {
    private static final Map<KeyCodeCombination, Consumer<FlowDisplayController>> KEYCODE_ACTION_MAP = ImmutableMap.<KeyCodeCombination, Consumer<FlowDisplayController>>builder().put(
            QUESTION, FlowDisplayController::attemptMark).put(
            MERGE, FlowDisplayController::attemptMerge).put(
            REFUTE, FlowDisplayController::attemptRefutation).put(
            EXTEND, FlowDisplayController::attemptExtension).put(
            NARROW_BY_1, FlowDisplayController::narrowOnce).put(
            UPSCALE_BY_1, FlowDisplayController::upscaleOnce).put(
            EDIT, FlowDisplayController::edit).put(
            WRITE, FlowDisplayController::addWriter).put(
            DELETE, FlowDisplayController::attemptDelete).put(
            EXPAND, FlowDisplayController::attemptExpansion).put(
            SELECT_ALL, FlowDisplayController::attemptSelectAll
    ).build();

    private static final Map<KeyCode, BiFunction<FlowGrid, FlowingRegion, Optional<FlowingRegion>>> KEYCODE_REGION_SELECTION_MAP = ImmutableMap.of(
            KeyCode.LEFT, FlowGrid::getLeft,
            KeyCode.RIGHT, FlowGrid::getRight,
            KeyCode.UP, FlowGrid::getAbove,
            KeyCode.DOWN, FlowGrid::getBelow
    );

    private static final Map<KeyCode, Consumer<SpeechList>> KEYCODE_SPEECH_SELECTION_MAP = ImmutableMap.of(
            KeyCode.RIGHT, SpeechList::selectRightSpeech,
            KeyCode.LEFT, SpeechList::selectLeftSpeech
    );

    private static final ImmutableMap<KeyCode, Runnable> KEYCODE_GLOBAL_ACTIONS_MAP = ImmutableMap.of();

    private final FlowDisplayController flowDisplayController;
    private final KeyEvent keyEvent;

    KeyEventProcessor(FlowDisplayController flowDisplayController, KeyEvent keyEvent) {
        this.flowDisplayController = flowDisplayController;
        this.keyEvent = keyEvent;
    }

    public FlowDisplayController getFlowDisplayController() {
        return flowDisplayController;
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    private final List<Runnable> PROCESS_LIST = List.of(
        this::handleIfAction,
        this::handleIfRegionSelection,
        this::handleIfSpeechSelection
    );

    private void handleIfAction() {
        KEYCODE_ACTION_MAP.forEach((key, value) -> {
            if (key.match(keyEvent))
                value.accept(flowDisplayController);
        });
    }

    private void handleIfRegionSelection() {
        KEYCODE_REGION_SELECTION_MAP.forEach((key, value) -> {
            if (keyEvent.getCode() == key)
                getFlowDisplayController().handleSelection(
                        // Partial application
                        flowingRegion -> value.apply(getFlowDisplayController().flowGrid, flowingRegion),
                        keyEvent.isControlDown()
                );
        });
    }

    private void handleIfSpeechSelection() {
        KEYCODE_SPEECH_SELECTION_MAP.forEach((key, value) -> {
            if (key == keyEvent.getCode())
                value.accept(flowDisplayController.getSpeechList());
        });
    }

    void process() {
        PROCESS_LIST.forEach(Runnable::run);
    }
}
