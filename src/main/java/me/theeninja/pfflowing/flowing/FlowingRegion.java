package me.theeninja.pfflowing.flowing;

import javafx.scene.control.Label;
import javafx.scene.text.TextFlow;
import me.theeninja.pfflowing.Duplicable;
import me.theeninja.pfflowing.StringSerializable;
import me.theeninja.pfflowing.configuration.GlobalConfiguration;
import me.theeninja.pfflowing.flowingregions.FlowingText;
import me.theeninja.pfflowing.gui.FlowingGrid;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.gui.LengthLimitType;
import me.theeninja.pfflowing.utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlowingRegion extends TextFlow implements Identifiable, Duplicable<FlowingRegion> {
    private static int currentID = 0;
    private static Map<Integer, FlowingRegion> idMap;
    private final FlowingGridController instance;
    private int id;

    public String rawText;

    public static FlowingRegion getFlowingRegion(int id) {
        return idMap.get(id);
    }

    static {
        idMap = new HashMap<>();
    }

    public FlowingRegion(FlowingGridController instance) {
        super();
        instance.implementListeners(this);
        this.instance = instance;
    }

    public FlowingRegion(FlowingText flowingText, FlowingGridController instance, LengthLimitType lengthLimitType, int limit) {
        this(Collections.singletonList(flowingText), instance, lengthLimitType, limit);
    }

    public FlowingRegion(List<FlowingText> flowingTexts, FlowingGridController instance, LengthLimitType lengthLimitType, int limit) {
        this.instance = instance;
        this.id = currentID++;
        flowingTexts.forEach(flowingText -> flowingText.setFont(GlobalConfiguration.FONT));
        this.getChildren().addAll(flowingTexts);
        instance.implementListeners(this);
        super(shorten(fullText, lengthLimitType, limit));
        this.fullText = fullText;
        this.lengthLimitType = lengthLimitType;
        this.limit = limit;
    }

    @Override
    public int getID() {
        return id;
    }

    public String getUnformattedText() {
        StringBuilder textBuilder = new StringBuilder();
        Utils.getOfType(getChildren(), FlowingText.class).forEach(flowingText -> textBuilder.append(flowingText.getText()));
        return textBuilder.toString();
    }

    public String getRawText() {
        return null;
    }

    @Override
    public FlowingRegion duplicate() {
        List<FlowingText> flowingTexts = Utils.getOfType(getChildren(), FlowingText.class);

        FlowingRegion flowingRegion = new FlowingRegion(flowingTexts.stream().map(FlowingText::duplicate)
                .collect(Collectors.toList()), getInstance());
        FlowingGrid.setColumnIndex(flowingRegion, FlowingGrid.getColumnIndex(this));
        FlowingGrid.setRowIndex(flowingRegion, FlowingGrid.getRowIndex(this));
        return flowingRegion;
    }

    public FlowingGridController getInstance() {
        return instance;
    }

    private String fullText;
    private LengthLimitType lengthLimitType;
    private int limit;

    private static List<FlowingText> setAndReturnShortened(List<FlowingText> flowingTexts, LengthLimitType lengthLimitType, int limit) {
        int limitTypeSatisfied;
        for (FlowingText flowingText : flowingTexts) {
            String flowingTextContent = flowingText.getText();
            for (int index = 0; index < flowingTextContent.length(); index++) {

            }
        }

        // "Hello my name is""The Mario Ishac
    }


    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
        this.setText(shorten(getFullText(), getLengthLimitType(), getLimit());
    }

    public LengthLimitType getLengthLimitType() {
        return lengthLimitType;
    }

    public void setLengthLimitType(LengthLimitType lengthLimitType) {
        this.lengthLimitType = lengthLimitType;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
