package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.FlowDisplayController;

public class ModifyCard extends Action<FlowingRegion> {
    private final FlowingRegion targetFlowingRegion;
    private final Card card;

    public ModifyCard(FlowingRegion flowingRegion, Card card) {
        super(flowingRegion);
        this.targetFlowingRegion = flowingRegion;
        this.card = card;
    }

    @Override
    public void execute() {
        getTargetFlowingRegion().getAssociatedCards().add(card);
    }

    @Override
    public void unexecute() {
        getTargetFlowingRegion().getAssociatedCards().remove(card);
    }

    @Override
    public String getName() {
        return "Card(s) Change";
    }

    public FlowingRegion getTargetFlowingRegion() {
        return targetFlowingRegion;
    }

    public Card getCard() {
        return card;
    }
}