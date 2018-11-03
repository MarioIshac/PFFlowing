package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.actions.Action;

import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import java.io.IOException;

public class EFlowRequestHandler extends ServerRequestHandler {
    @Override
    public int onConnect(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
    }

    @Override
    public void onDisconnect(HeaderSet request, HeaderSet reply) {
        super.onDisconnect(request, reply);
    }

    @Override
    public int onPut(Operation putOperation) {
        try {
            HeaderSet receivedHeaders = putOperation.getReceivedHeaders();

            if (receivedHeaders == null) {
                return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
            }

            Object actionClassObject = receivedHeaders.getHeader(2);

            @SuppressWarnings("unchecked")
            Class<? extends Action> actionClassName = (Class<? extends Action>) actionClassObject;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
    }

    @Override
    public int onGet(Operation getOperation) {
        return super.onGet(getOperation);
    }
}
