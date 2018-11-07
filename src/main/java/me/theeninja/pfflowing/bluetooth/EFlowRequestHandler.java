package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.ActionManager;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.gui.FlowController;
import me.theeninja.pfflowing.gui.FlowDisplayController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;
import org.apache.commons.io.IOUtils;

import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class EFlowRequestHandler extends ServerRequestHandler {
    private final FlowController flowController;

    public EFlowRequestHandler(FlowController flowController) {
        this.flowController = flowController;
    }

    @Override
    public int onConnect(HeaderSet receivedHeaders, HeaderSet repliedHeaders) {
        return ResponseCodes.OBEX_HTTP_OK;
    }

    @Override
    public void onDisconnect(HeaderSet receivedHeaders, HeaderSet repliedHeaders) {
        // Nothing
    }

    @Override
    public int onPut(Operation putOperation) {
        try {
            HeaderSet receivedHeaders = putOperation.getReceivedHeaders();

            if (receivedHeaders == null) {
                return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
            }

            byte putOperationType = (byte) receivedHeaders.getHeader(EFlowHeader.TYPE);

            switch (putOperationType) {
                case PutOperationType.NEW_ROUND: {
                    return newRound(receivedHeaders);
                }

                case PutOperationType.NEW_ACTION: {
                    return newAction(putOperation);
                }

                case PutOperationType.REDO_ACTION: {
                    return modifyAction(receivedHeaders, ActionManager::redo);
                }

                case PutOperationType.UNDO_ACTION: {
                    return modifyAction(receivedHeaders, ActionManager::undo);
                }
            }

            return ResponseCodes.OBEX_HTTP_OK;
        }
        catch (IOException e) {
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
    }

    private int modifyAction(HeaderSet headerSet, Consumer<ActionManager> actionManagerConsumer) throws IOException {
        String roundName = (String) headerSet.getHeader(EFlowHeader.ROUND_NAME);

        byte sideRepresentation = (byte) headerSet.getHeader(EFlowHeader.SIDE);
        Side side = Side.getSide(sideRepresentation);

        if (!PutOperationScale.isSideScale(sideRepresentation)) {
            return EFlowResponseCodes.NO_SIDE;
        }

        FlowDisplayController flowDisplayController = getRound(roundName).getController(side);

        ActionManager actionManager = flowDisplayController.getActionManager();

        actionManagerConsumer.accept(actionManager);

        return ResponseCodes.OBEX_HTTP_OK;
    }

    private int newRound(HeaderSet headerSet) throws IOException {
        String roundName = (String) headerSet.getHeader(EFlowHeader.ROUND_NAME);

        byte sideRepresentation = (byte) headerSet.getHeader(EFlowHeader.SIDE);
        Side side = Side.getSide(sideRepresentation);

        if (!PutOperationScale.isSideScale(sideRepresentation)) {
            return EFlowResponseCodes.NO_SIDE;
        }

        Round round = new Round(roundName, side);

        getFlowController().addRound(round);

        return ResponseCodes.OBEX_HTTP_OK;
    }

    private int newAction(Operation operation) throws IOException {
        HeaderSet receivedHeaders = operation.getReceivedHeaders();

        String roundName = (String) receivedHeaders.getHeader(EFlowHeader.ROUND_NAME);

        byte actionClassRepresentation = (byte) receivedHeaders.getHeader(EFlowHeader.ACTION_CLASS);

        if (!PutOperationScale.isActionClass(actionClassRepresentation)) {
            return EFlowResponseCodes.NO_ACTION_CLASS;
        }

        byte sideRepresentation = (byte) receivedHeaders.getHeader(EFlowHeader.SIDE);
        Side side = Side.getSide(sideRepresentation);

        if (side == null) {
            return EFlowResponseCodes.NO_SIDE;
        }

        Class<? extends Action> actionClass = PutOperationScale.getActionClass(actionClassRepresentation);

        InputStream actionInputStream = operation.openInputStream();
        String actionJson = IOUtils.toString(actionInputStream, StandardCharsets.UTF_8);

        Action<?> action = EFlow.getInstance().getGSON().fromJson(actionJson, actionClass);

        Round round = getRound(roundName);

        if (round == null) {
            return EFlowResponseCodes.NO_ROUND_NAME;
        }

        FlowDisplayController flowDisplayController = round.getController(side);
        ActionManager actionManager = flowDisplayController.getActionManager();

        actionManager.perform(action);

        return ResponseCodes.OBEX_HTTP_OK;
    }

    @Override
    public int onGet(Operation getOperation) {
        return super.onGet(getOperation);
    }

    public FlowController getFlowController() {
        return flowController;
    }

    private Round getRound(String roundName) {
        for (Round round : getFlowController().getRounds()) {
            String existingRoundName = round.getRoundName();

            if (existingRoundName.equals(roundName)) {
               return round;
            }
        }

        throw new IllegalArgumentException("No round name with name " + roundName);
    }
}
