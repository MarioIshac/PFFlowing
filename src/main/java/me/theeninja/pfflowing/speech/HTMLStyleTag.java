package me.theeninja.pfflowing.speech;

public class HTMLStyleTag {
    private final String startTag;
    private final String endTag;
    private final String tagContent;

    public HTMLStyleTag(String tagContent) {
        this.tagContent = tagContent;
        this.startTag = "<" + tagContent + ">";
        this.endTag = "</" + tagContent + ">";
    }

    public String getStartTag() {
        return startTag;
    }

    public String getEndTag() {
        return endTag;
    }

    public String getTagContent() {
        return tagContent;
    }
}
