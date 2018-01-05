package me.theeninja.pfflowing.flowingregions;

import me.theeninja.pfflowing.speech.HTMLStyleTag;

public enum CharacterStyle {
    BOLD(new HTMLStyleTag("b"), "bold", 0),
    ITALICS(new HTMLStyleTag("i"), "italics", 1),
    UNDERLINED(new HTMLStyleTag("u"), "underlined", 2);

    private final HTMLStyleTag representingTag;
    private final String cssClass;
    private final int priority;

    CharacterStyle(HTMLStyleTag representingTag, String cssClass, int priority) {
        this.representingTag = representingTag;
        this.cssClass = cssClass;
        this.priority = priority;
    }

    public HTMLStyleTag getRepresentingTag() {
        return representingTag;
    }

    public String getCssClass() {
        return cssClass;
    }

    public int getPriority() {
        return this.priority;
    }

    public static CharacterStyle findStyleWithTag(String tag) {
        for (CharacterStyle characterStyle : values()) {
            if (characterStyle.getRepresentingTag().getTagContent().equals(tag)) {
                return characterStyle;
            }
        }
        return null;
    }
}
