package me.theeninja.pfflowing.flowing;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import me.theeninja.pfflowing.Duplicable;
import me.theeninja.pfflowing.configuration.GlobalConfiguration;
import me.theeninja.pfflowing.gui.FlowingGrid;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.gui.LengthLimitType;

import java.util.*;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

public class FlowingRegion extends Label implements Identifiable, Duplicable<FlowingRegion> {
    private static int currentID = 0;
    private static Map<Integer, FlowingRegion> idMap = new HashMap<>();;
    private final FlowingGridController instance;
    private int id;

    private BooleanProperty expanded = new SimpleBooleanProperty();

    private StringProperty shortenedText = new SimpleStringProperty();
    private StringProperty fullText = new SimpleStringProperty();

    public static FlowingRegion getFlowingRegion(int id) {
        return idMap.get(id);
    }

    public FlowingRegion(String text, FlowingGridController instance, LengthLimitType lengthLimitType, int limit) {
        super();

        this.lengthLimitType = lengthLimitType;
        this.limit = limit;

        setFullText(text);
        bindShortenedText();

        this.instance = instance;
        this.id = currentID++;

        setFont(GlobalConfiguration.FONT);
        instance.implementListeners(this);

        System.out.println("F" + getFullText());
        System.out.println("S" + getShortenedText());

        /*expandedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue)
                setText(getFullText());
            else
                setText(getShortenedText());
        }));*/

        setExpanded(false);
    }

    public void bindShortenedText() {
        shortenedText.bind(new StringBinding() {
            {
                super.bind(fullTextProperty());
            }

            @Override
            protected String computeValue() {
                String[] seperatedStrings = getFullText().split(getLengthLimitType().getSplit());
                List<String> limitedSeperatedStrings = Arrays.stream(seperatedStrings).limit(getLimit()).collect(Collectors.toList());
                String shortenedString = String.join(getLengthLimitType().getSplit(), limitedSeperatedStrings);
                shortenedString += "...";
                return shortenedString;
            }
        });
    }

    @Override
    public int getID() {
        return id;
    }

    public String getRawText() {
        return null;
    }

    @Override
    public FlowingRegion duplicate() {
        FlowingRegion duplicate = new FlowingRegion(getText(), getInstance(), getLengthLimitType(), getLimit());
        duplicate.setStyle(this.getStyle());
        FlowingGrid.setColumnIndex(duplicate, FlowingGrid.getColumnIndex(this));
        FlowingGrid.setRowIndex(duplicate, FlowingGrid.getRowIndex(this));
        return duplicate;
    }

    public FlowingGridController getInstance() {
        return instance;
    }

    private LengthLimitType lengthLimitType;
    private int limit;

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

    public String getShortenedText() {
        return shortenedText.get();
    }

    public StringProperty shortenedTextProperty() {
        return shortenedText;
    }

    public void setShortenedText(String shortenedText) {
        this.shortenedText.set(shortenedText);
    }

    public String getFullText() {
        return fullText.get();
    }

    public StringProperty fullTextProperty() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText.set(fullText);
    }

    public boolean getExpanded() {
        return expanded.get();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
        if (getExpanded())
            setText(getFullText());
        else
            setText(getShortenedText());
    }
}
