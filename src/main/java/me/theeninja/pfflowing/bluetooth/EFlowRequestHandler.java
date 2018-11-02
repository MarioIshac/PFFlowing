package me.theeninja.pfflowing.bluetooth;

import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import java.io.IOException;

public class EFlowRequestHandler extends ServerRequestHandler {
    @Override
    public int onConnect(HeaderSet request, HeaderSet reply) {
        try {
            System.out.println("Incoming connection with name " + request.getHeader(HeaderSet.NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
    }

    @Override
    public void onDisconnect(HeaderSet request, HeaderSet reply) {
        super.onDisconnect(request, reply);
    }

    @Override
    public int onPut(Operation op) {
        return super.onPut(op);
    }

    @Override
    public int onGet(Operation op) {
        return super.onGet(op);
    }
}
