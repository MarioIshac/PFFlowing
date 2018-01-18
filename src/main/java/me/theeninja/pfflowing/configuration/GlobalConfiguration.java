package me.theeninja.pfflowing.configuration;

import javafx.scene.text.Font;
import me.theeninja.pfflowing.flowingregions.CharacterStyle;
import me.theeninja.pfflowing.flowing.DefensiveSpeech;

/**
 * Holds constants or options specified by the user. These constants will apply to all projects, and thus
 * are not project-specific.
 */
public class GlobalConfiguration {
    public static final CharacterStyle SPOKEN = CharacterStyle.BOLD;

    public static final double SPEECH_SEPERATION = 30;
    public static final int NUMBER_OF_SPEECHES_PER_DISPLAY = 8;

    public static final Font FONT = Font.font(12);

    public static final double ARROW_MARGIN = 2;
}
