package me.theeninja.pfflowing.flowing;

import com.google.gson.annotations.Expose;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import me.theeninja.pfflowing.Duplicable;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.configuration.InternalConfiguration;
import me.theeninja.pfflowing.flowingregions.Card;
import me.theeninja.pfflowing.gui.FlowGrid;
import me.theeninja.pfflowing.gui.LengthLimitType;

import java.util.*;
import java.util.stream.Collectors;

public class FlowingRegion extends Label implements Duplicable<FlowingRegion> {

    @Expose private StringProperty fullText = new SimpleStringProperty();
    private StringProperty shortenedText = new SimpleStringProperty();

    private BooleanProperty expanded = new SimpleBooleanProperty();
    private final List<Card> associatedCards;

    protected FlowingRegion(String text) {
        this(text, Collections.emptyList());
    }

    protected FlowingRegion(String text, List<Card> associatedCards) {
        super();

        setWrapText(true);

        // Listener must be added first, before setting full text
        addFullTextListener();
        setFullText(text);

        fontProperty().bind(EFlow.getInstance().getConfiguration().getFont().valueProperty());

        setExpanded(false);

        this.associatedCards = associatedCards;
    }

    public void addFullTextListener() {
        fullTextProperty().addListener((observable, oldValue, newValue) -> {
            String[] seperatedStrings = getFullText().split(InternalConfiguration.LENGTH_LIMIT_TYPE.getSplit());

            List<String> limitedSeperatedStrings = Arrays.stream(seperatedStrings).limit(InternalConfiguration.LENGTH_LIMIT).collect(Collectors.toList());

            String shortenedString = String.join(InternalConfiguration.LENGTH_LIMIT_TYPE.getSplit(), limitedSeperatedStrings);

            if (seperatedStrings.length > InternalConfiguration.LENGTH_LIMIT) {
                shortenedString += "...";
            }

            setShortenedText(shortenedString);
        });
    }

    @Override
    public FlowingRegion duplicate() {
        FlowingRegion duplicate = new FlowingRegion(getText());
        duplicate.setStyle(this.getStyle());
        FlowGrid.setColumnIndex(duplicate, FlowGrid.getColumnIndex(this));
        FlowGrid.setRowIndex(duplicate, FlowGrid.getRowIndex(this));
        return duplicate;
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
        if (expanded)
            textProperty().bind(fullTextProperty());
        else
            textProperty().bind(shortenedTextProperty());
        this.expanded.set(expanded);
    }

    public List<Card> getAssociatedCards() {
        return associatedCards;
    }
}
