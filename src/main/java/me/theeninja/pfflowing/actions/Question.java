package me.theeninja.pfflowing.actions;

import me.theeninja.pfflowing.flowing.FlowingRegion;
import me.theeninja.pfflowing.gui.FlowDisplayController;

public class Question extends FlowAction {
    private final FlowingRegion baseFlowingRegion;
    private final String questionMessage;

    public Question(FlowDisplayController flowDisplayController, FlowingRegion baseFlowingRegion, String questionMessage) {
        super(flowDisplayController);
        this.baseFlowingRegion = baseFlowingRegion;
        this.questionMessage = questionMessage;
    }

    @Override
    public void execute() {
        System.out.println("Executed");
        getBaseFlowingRegion().getAssociatedQuestions().add(getQuestionMessage());
    }

    @Override
    public void unexecute() {
        getBaseFlowingRegion().getAssociatedQuestions().remove(getQuestionMessage());
    }

    @Override
    public String getName() {
        return "Question \"" + getActionIdentifier(getBaseFlowingRegion()) + "\"";
    }

    private FlowingRegion getBaseFlowingRegion() {
        return this.baseFlowingRegion;
    }

    private String getQuestionMessage() {
        return this.questionMessage;
    }
}
