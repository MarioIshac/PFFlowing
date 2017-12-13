package me.theeninja.pfflowing;

import javafx.scene.text.Font;
import me.theeninja.pfflowing.card.CharacterStyle;
import me.theeninja.pfflowing.flowing.Speech;

public class Configuration {
    public static final Speech MY_SPEECH = Speech.NEG_2;

    public static final CharacterStyle SPOKEN = CharacterStyle.BOLD;

    public static final double SPEECH_SEPERATION = 50;
    public static final int NUMBER_OF_SPEECHES_PER_DISPLAY = 8;

    public static final Font FONT = Font.font(12);
}
