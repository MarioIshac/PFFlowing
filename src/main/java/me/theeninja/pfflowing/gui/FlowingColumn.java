package me.theeninja.pfflowing.gui;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import me.theeninja.pfflowing.Configuration;
import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.Utils;
import me.theeninja.pfflowing.card.Card;
import me.theeninja.pfflowing.card.CharacterFormatting;
import me.theeninja.pfflowing.card.CharacterStyle;
import me.theeninja.pfflowing.card.DefensiveReasoning;
import me.theeninja.pfflowing.flowing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class FlowingColumn extends VBox implements Bindable<Speech> {
    private final Speech speech;
    private final Label label;
    private final VBox container;
    private Speech bindedSpeech;

    private Color color;

    private final boolean managesOpposite;

    public FlowingColumn(Speech speech) {
        this.speech = speech;

        this.label = new Label(speech.getLabelText());

        this.container = new VBox();

        Bindable.bind(this, speech);

        getChildren().add(getLabel());
        getChildren().add(getContainer());

        setPrefWidth(FlowingColumnsController.getFXMLInstance().getCorrelatingView().getPrefWidth() / 8);
        HBox.setHgrow(this, Priority.ALWAYS);

        managesOpposite = getBinded() instanceof DefensiveSpeech;
    }

    public static List<FlowingColumn> of(SpeechList speechList) {
        List<FlowingColumn> flowingColumns = new ArrayList<>();
        for (Speech speech : speechList.getSpeeches()) {
            FlowingColumn flowColumn = speech.getSide() == speechList.getSide() ?
                    new DefensiveFlowingColumn(speech) :
                    new RefutationFlowingColumn(speech);
        }
        return flowingColumns;
    }

    public Label getLabel() {
        return label;
    }

    public Speech getSpeech() {
        return speech;
    }


    /**
     * Adds a {@link TextArea} (the flowing region writer) to the flowing column. This flowing region writer
     * is designed so that on the user hitting enter, the text entered into the flowing region writer
     * would be used to create a flowing region representing what the user typed.
     */
    public abstract void addFlowingRegionWriter(boolean createNewOne, Consumer<String> postEnterAction);

    /**
     * Defaul post-enter specification for the above method
     * @param createNewOne
     */
    public void addFlowingRegionWriter(boolean createNewOne) {
        addFlowingRegionWriter(createNewOne, text -> {
            DefensiveReasoning defensiveReasoning = new DefensiveReasoning(text);
            addDefensiveFlowingRegion(defensiveReasoning);
        });

    }
    public <T extends FlowingRegion & Offensive> void addOffensiveFlowingRegion(T offensiveRegion) {
        addFlowingRegion(offensiveRegion);
        drawArrow(offensiveRegion);
    }

    private <T extends FlowingRegion & Offensive> void drawArrow(T offensiveRegion) {
        List<FlowingRegion> starters = offensiveRegion.getTargetRegions();
        for (FlowingRegion starter : starters) {
            Bounds starterBounds = starter.localToScene(starter.getLayoutBounds());
            double startX = starterBounds.getMaxX() + Configuration.ARROW_MARGIN;
            double startY = (starterBounds.getMinY() + starterBounds.getMaxY()) / 2;

            Bounds finishBounds = offensiveRegion.localToScene(offensiveRegion.getLayoutBounds());
            double finishX = finishBounds.getMinX() - Configuration.ARROW_MARGIN;
            double finishY = (finishBounds.getMinY() + finishBounds.getMaxY()) / 2;

            Line line = new Line(startX, startY, finishX, finishY);

            starter.layoutXProperty().addListener(changeListener -> {
                System.out.println("CHANGED");
            });

            PFFlowingApplicationController.getFXMLInstance().getCorrelatingView().getChildren().add(line);
        }
    }

    public <T extends FlowingRegion & Defensive> void addDefensiveFlowingRegion(T defensiveRegion) {
        defensiveRegion.setBackground(Utils.generateBackgroundOfColor(Color.RED));
        addFlowingRegion(defensiveRegion);
    }

    public <T extends FlowingRegion> void addFlowingRegion(T flowingRegion) {
        FlowingColumnsController.getFXMLInstance().implementListeners(flowingRegion);
        if (flowingRegion instanceof Card) {
            Card flowingCard = (Card) flowingRegion;

            CharacterFormatting characterFormatting = new CharacterFormatting(Arrays.asList(
                    Configuration.SPOKEN
            ));

            String tooltipText = flowingCard.getCardContent().getContent(characterFormatting);
            System.out.println("Tool tip text: " + tooltipText);

            Tooltip flowingRegionTooltip = new Tooltip(tooltipText);

            characterFormatting.getCharacterStyles().stream()
                    .map(CharacterStyle::getCssClass)
                    .forEach(flowingRegionTooltip.getStyleClass()::add);

            Tooltip.install(flowingRegion, flowingRegionTooltip);
        }

        flowingRegion.setWrapText(true);
        flowingRegion.setTextFill(color);

        if (isManagesOpposite())
            getContainer().getChildren().add(flowingRegion);
        else {
            Group group = new Group(flowingRegion);

            group.setManaged(false);
            group.setVisible(true);
            FlowingRegion a = ((Offensive) flowingRegion).getTargetRegions().get(0);
            System.out.println(a.getBoundsInParent().getMinY());
            System.out.println(a.getBoundsInLocal().getMinY());
            System.out.println(a.getLayoutBounds().getMinY());
            System.out.println(a.localToParent(a.getBoundsInParent()).getMinY());
            System.out.println(a.getLayoutY());
            group.setLayoutY(a.getLayoutY());
            System.out.println(group.getLayoutY());
            flowingRegion.setBackground(Utils.generateBackgroundOfColor(Color.GREEN));

            getContainer().getChildren().add(group);
        }

        // User actions
        flowingRegion.prefWidthProperty().bind(this.widthProperty());


        System.out.println(flowingRegion);
    }


    public void removeAllFlowingRegionWriters() {
        getChildren().removeIf(node -> node instanceof TextArea);
    }

    public VBox getContainer() {
        return container;
    }

    @Override
    public void setBinded(Speech speech) {
        this.bindedSpeech = speech;
        this.color = speech.getSide() == Side.AFFIRMATIVE ? Color.BLACK : Color.RED;
        this.label.setTextFill(this.color);
    }

    @Override
    public Speech getBinded() {
        return bindedSpeech;
    }

    private FlowingColumn getOpposingFlowingColumn() {
        FlowingColumns flowingColumns = getParentFlowingColumns();
        SpeechListManager speechListManager = flowingColumns.getBinded();
        return speechListManager.getSpeechList(this.getBinded()).getOpposite(this.getBinded()).getBinded();
    }

    public boolean isManagesOpposite() {
        return managesOpposite;
    }

    public FlowingColumns getParentFlowingColumns() {
        return (FlowingColumns) getParent();
    }
}
