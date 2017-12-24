package me.theeninja.pfflowing.gui;

import me.theeninja.pfflowing.flowing.Speech;

import java.util.function.Consumer;

public class RefutationFlowingColumn extends FlowingColumn {
    public RefutationFlowingColumn(Speech speech) {
        super(speech);
    }

    @Override
    public void addFlowingRegionWriter(boolean createNewOne, Consumer<String> postEnterAction) {

    }
}
