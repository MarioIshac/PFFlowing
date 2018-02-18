package me.theeninja.pfflowing.flowing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.Expose;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import me.theeninja.pfflowing.Duplicable;
import me.theeninja.pfflowing.StringSerializable;
import me.theeninja.pfflowing.configuration.GlobalConfiguration;
import me.theeninja.pfflowing.gui.FlowingGrid;
import me.theeninja.pfflowing.gui.FlowingGridController;
import me.theeninja.pfflowing.gui.LengthLimitType;

import com.google.gson.Gson;
import org.hildan.fxgson.FxGson;

import java.util.*;
import java.util.stream.Collectors;

public class FlowingRegion extends Label implements Duplicable<FlowingRegion> {

    @Expose private StringProperty fullText = new SimpleStringProperty();
    private StringProperty shortenedText = new SimpleStringProperty();

    private BooleanProperty expanded = new SimpleBooleanProperty();

    public FlowingRegion(String text) {
        super();

        setWrapText(true);

        // Listener must be added first, before setting full text
        addFullTextListener();
        setFullText(text);

        setFont(GlobalConfiguration.FONT);

        setExpanded(false);
    }

    public void addFullTextListener() {
        fullTextProperty().addListener((observable, oldValue, newValue) -> {
            String[] seperatedStrings = getFullText().split(GlobalConfiguration.LENGTH_LIMIT_TYPE.getSplit());

            List<String> limitedSeperatedStrings = Arrays.stream(seperatedStrings).limit(GlobalConfiguration.LENGTH_LIMIT).collect(Collectors.toList());

            String shortenedString = String.join(GlobalConfiguration.LENGTH_LIMIT_TYPE.getSplit(), limitedSeperatedStrings);

            if (seperatedStrings.length > GlobalConfiguration.LENGTH_LIMIT) {
                shortenedString += "...";
            }

            setShortenedText(shortenedString);
        });
    }

    @Override
    public FlowingRegion duplicate() {
        FlowingRegion duplicate = new FlowingRegion(getText());
        duplicate.setStyle(this.getStyle());
        FlowingGrid.setColumnIndex(duplicate, FlowingGrid.getColumnIndex(this));
        FlowingGrid.setRowIndex(duplicate, FlowingGrid.getRowIndex(this));
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
}
