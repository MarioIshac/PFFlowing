package me.theeninja.pfflowing.flowingregions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.theeninja.pfflowing.speech.Side;

public class Card {
    @Expose
    @SerializedName("representation")
    private String representation;

    @Expose
    @SerializedName("htmlContent")
    private String htmlContent;

    public Card(String representation, String htmlContent) {
        this.representation = representation;
        this.htmlContent = htmlContent;
    }

    public String getHTMLContent() {
        return htmlContent;
    }

    public void setHTMLContent(String htmlCardContent) {
        this.htmlContent = htmlCardContent;
    }

    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {
        return getRepresentation();
    }
}
