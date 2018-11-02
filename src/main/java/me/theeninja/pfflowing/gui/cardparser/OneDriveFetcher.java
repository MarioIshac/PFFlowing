package me.theeninja.pfflowing.gui.cardparser;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class OneDriveFetcher extends OnlineFileFetcher {
    OneDriveFetcher() {
        super(null, null);
    }

    @Override
    protected Object newDummyFile() {
        return null;
    }

    @Override
    protected void setUpConnection() throws IOException {

    }

    @Override
    protected List getPossibleFiles() throws IOException {
        return null;
    }

    @Override
    protected String getHTMLOfFile(Object file) throws IOException {
        return null;
    }
}
