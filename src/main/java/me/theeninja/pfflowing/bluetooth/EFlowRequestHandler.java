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

public class EFlowRequestHandler extends ServerRequestHandler {
    private final FlowController flowController;

    public EFlowRequestHandler(FlowController flowController) {
        this.flowController = flowController;
    }

    @Override
    public int onConnect(HeaderSet receivedHeaders, HeaderSet repliedHeaders) {
        try {
            if (receivedHeaders == null) {
                return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
            }

            String roundName = (String) receivedHeaders.getHeader(EFlowHeader.ROUND_NAME);

            if (roundName == null) {
                return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
            }

            byte sideRepresentation = (byte) receivedHeaders.getHeader(EFlowHeader.SIDE);
            Side side = Side.getSide(sideRepresentation);

            if (side == null) {
                return EFlowResponseCodes.NO_SIDE;
            }

            Round round = new Round(side);
            round.setName(roundName);

            getFlowController().addRound(round);

            return ResponseCodes.OBEX_HTTP_OK;
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
    }

    @Override
    public void onDisconnect(HeaderSet receivedHeaders, HeaderSet repliedHeaders) {
        super.onDisconnect(receivedHeaders, repliedHeaders);
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
                case PutOperationType.NEW_ACTION: {
                    String roundName = (String) receivedHeaders.getHeader(EFlowHeader.ROUND_NAME);

                    byte actionClassRepresentation = (byte) receivedHeaders.getHeader(EFlowHeader.ACTION_CLASS);

                    if (!PutOperationScale.isActionClass(actionClassRepresentation)) {
                        return EFlowResponseCodes.NO_ACTION_CLASS;
                    }

                    byte sideRepresentation = (byte) receivedHeaders.getHeader(EFlowHeader.SIDE);

                    if (!PutOperationScale.isSideScale(sideRepresentation)) {
                        return EFlowResponseCodes.NO_SIDE;
                    }

                    Class<? extends Action> actionClass = PutOperationScale.getActionClass(actionClassRepresentation);

                    InputStream actionInputStream = putOperation.openInputStream();
                    String actionJson = IOUtils.toString(actionInputStream, StandardCharsets.UTF_8);

                    Action<?> action = EFlow.getInstance().getGSON().fromJson(actionJson, actionClass);

                    Round round = getRound(roundName);

                    if (round == null) {
                        return EFlowResponseCodes.NO_ROUND_NAME;
                    }

                    FlowDisplayController flowDisplayController = sideRepresentation == PutOperationScale.AFF_SCALE ?
                            round.getAffController() :
                            round.getNegController();

                    ActionManager actionManager = flowDisplayController.getActionManager();

                    actionManager.perform(action);

                    break;
                }

                /* case PutOperationType.REDO_ACTION: {
                    byte sideRepresentation = (byte) receivedHeaders.getHeader(EFlowHeader.SIDE);

                    if (!PutOperationScale.isSideScale(sideRepresentation)) {
                        return EFlowResponseCodes.NO_SIDE;
                    }

                    FlowDisplayController flowDisplayController = sideRepresentation == PutOperationScale.AFF_SCALE ?
                            getRound().getAffController() :
                            getRound().getNegController();

                    ActionManager actionManager = flowDisplayController.getActionManager();

                    actionManager.redo();

                    break;
                }

                case PutOperationType.UNDO_ACTION: {
                    byte sideRepresentation = (byte) receivedHeaders.getHeader(EFlowHeader.SIDE);

                    if (!PutOperationScale.isSideScale(sideRepresentation)) {
                        return EFlowResponseCodes.NO_SIDE;
                    }

                    FlowDisplayController flowDisplayController = sideRepresentation == PutOperationScale.AFF_SCALE ?
                            getRound().getAffController() :
                            getRound().getNegController();

                    ActionManager actionManager = flowDisplayController.getActionManager();

                    actionManager.undo();

                    break;
                } */
            }

            return ResponseCodes.OBEX_HTTP_OK;
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
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
            String existingRoundName = round.getName();

            if (existingRoundName.equals(roundName)) {
               return round;
            }
        }

        return null;
    }
}
