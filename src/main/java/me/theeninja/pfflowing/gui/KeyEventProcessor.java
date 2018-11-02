package me.theeninja.pfflowing.gui;

import com.google.common.collect.ImmutableMap;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import me.theeninja.pfflowing.ActionManager;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.printing.RoundPrinter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static me.theeninja.pfflowing.gui.KeyCodeCombinationUtils.*;

public class KeyEventProcessor {
    private static final Map<KeyCodeCombination, Consumer<FlowDisplayController>> KEYCODE_ACTION_MAP = ImmutableMap.<KeyCodeCombination, Consumer<FlowDisplayController>>builder().put(
        QUESTION, FlowDisplayController::attemptQuestion
    ).put(
        MERGE, FlowDisplayController::attemptMerge
    ).put(
        REFUTE, FlowDisplayController::attemptRefutation
    ).put(
        EXTEND, FlowDisplayController::attemptExtension
    ).put(
        NARROW_BY_1, FlowDisplayController::narrowOnce
    ).put(
        UPSCALE_BY_1, FlowDisplayController::upscaleOnce
    ).put(
        EDIT, FlowDisplayController::attemptEdit
    ).put(
        WRITE, FlowDisplayController::addWriter
    ).put(
        DELETE, FlowDisplayController::attemptDelete
    ).put(
        EXPAND, FlowDisplayController::attemptExpansion
    ).put(
        SELECT_ALL, FlowDisplayController::selectAll
    ).put(
        SHIFT_DISPLAY_LEFT, FlowDisplayController::shiftLeft
    ).put(
        SHIFT_DISPLAY_RIGHT, FlowDisplayController::shiftRight
    ).put(
        SPLIT, FlowDisplayController::attemptSplit)
    .build();

    private static final Map<KeyCodeCombination, Consumer<ActionManager>> KEYCODE_ACTIONMANAGER_MAP = Map.of(
        UNDO, ActionManager::undo,
        REDO, ActionManager::redo
    );

    private static final Map<Direction, KeyCode> KEYCODE_DIRECTION_MAP = Map.of(
        Direction.UP, KeyCode.UP,
        Direction.RIGHT, KeyCode.RIGHT,
        Direction.DOWN, KeyCode.DOWN,
        Direction.LEFT, KeyCode.LEFT
    );

    private static final Map<Direction, Function<FlowDisplayController, Optional<FlowingRegion>>> KEYCODE_REGION_DEFAULT_MAP = Map.of(
        Direction.UP, FlowDisplayController::fromBottom,
        Direction.RIGHT, FlowDisplayController::fromLeft,
        Direction.DOWN, FlowDisplayController::fromTop,
        Direction.LEFT, FlowDisplayController::fromRight
    );

    private static final Map<KeyCodeCombination, Consumer<FlowController>> KEYCODE_MULTI_CONTROLLER_ACTION_MAP = ImmutableMap.of(
        SWITCH_SPEECHLIST, FlowController::switchSpeechList,
        PRINT, FlowController::printSelectedRound,
        BLUETOOTH_SHARE, FlowController::attemptBluetoothShare
    );

    private static final Map<Direction, Consumer<FlowDisplayController>> KEYCODE_SPEECH_SELECTION_MAP = Map.of(
         Direction.RIGHT, FlowDisplayController::selectRightSpeech,
         Direction.LEFT, FlowDisplayController::selectLeftSpeech
    );

    private final FlowController flowController;
    private final KeyEvent keyEvent;

    KeyEventProcessor(FlowController flowController, KeyEvent keyEvent) {
        this.flowController = flowController;
        this.keyEvent = keyEvent;
    }

    public KeyEvent getKeyEvent() {
        return keyEvent;
    }

    private final List<Runnable> PROCESS_LIST = List.of(
        this::handleIfSingleControllerAction,
        this::handleIfMultiControllerAction,
        this::handleIfRegionSelection,
        this::handleIfSpeechSelection,
        this::handleIfEdit
    );

    private void handleIfSingleControllerAction() {
        KEYCODE_ACTION_MAP.forEach((key, value) -> {
            if (key.match(getKeyEvent()))
                value.accept(flowController.getSelectedRound().getSelectedController());
        });
    }

    private void handleIfMultiControllerAction() {
        KEYCODE_MULTI_CONTROLLER_ACTION_MAP.forEach((key, value) -> {
            if (key.match(getKeyEvent()))
                value.accept(flowController);
        });
    }

    private void handleIfRegionSelection() {
        final FlowDisplayController selectedController = flowController.getSelectedRound().getSelectedController();

        for (Direction direction : Direction.values()) {
            KeyCode keyCode = KEYCODE_DIRECTION_MAP.get(direction);
            KeyCodeCombination singleSelect = new KeyCodeCombination(keyCode/*, SELECT*/);
            KeyCodeCombination multiSelect = new KeyCodeCombination(keyCode,/* SELECT,*/ TOGGLE_SELECT_MULTIPLE);

            Function<FlowDisplayController, Optional<FlowingRegion>> function = KEYCODE_REGION_DEFAULT_MAP.get(direction);
            Supplier<Optional<FlowingRegion>> supplier = () -> function.apply(selectedController);

            Function<FlowingRegion, Optional<FlowingRegion>> a = flowingRegion ->
                selectedController.flowGrid.getRelativeFlowingRegion(flowingRegion, direction);

            if (singleSelect.match(getKeyEvent()))
                selectedController.handleSelection(a, supplier, false);
            else if (multiSelect.match(getKeyEvent()))
                selectedController.handleSelection(a, supplier,true);
        }
    }

    private void handleIfSpeechSelection() {
        KEYCODE_SPEECH_SELECTION_MAP.forEach((key, value) -> {
            KeyCode keyCode = KEYCODE_DIRECTION_MAP.get(key);
            KeyCodeCombination test = new KeyCodeCombination(keyCode, KeyCodeCombinationUtils.SPEECH_SELECTOR);
            if (test.match(getKeyEvent()))
                value.accept(flowController.getSelectedRound().getSelectedController());
        });
    }

    private void handleIfEdit() {
        KEYCODE_ACTIONMANAGER_MAP.forEach((key, value) -> {
            if (key.match(getKeyEvent())) {
                value.accept(flowController.getSelectedRound().getSelectedController().getActionManager());
            }
        });
    }

    void process() {
        PROCESS_LIST.forEach(Runnable::run);
    }
}
