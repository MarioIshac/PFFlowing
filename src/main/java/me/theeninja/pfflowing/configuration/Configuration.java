package me.theeninja.pfflowing.configuration;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.*;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import me.theeninja.pfflowing.utils.Utils;

public class Configuration {

    private static final String AFF_COLOR_PROPERTY_LABEL = "Affirmative Color";
    private static final String NEG_COLOR_PROPERTY_LABEL = "Negation Color";

    private final String DEFAULT_FONT_FAMILY = "Times New Roman";
    private final int DEFAULT_FONT_SIZE = 12;
    private final Color DEFAULT_AFF_COLOR = Color.BLACK;
    private final Color DEFAULT_NEG_COLOR = Color.RED;
    private final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    @Expose
    @SerializedName("affColor")
    private Configurable<Color> affColor = new Configurable<>(
            new Descriptor("Color", "Aff Color", "Aff Color"),
            DEFAULT_AFF_COLOR
    );

    @SerializedName("negColor")
    @Expose
    private Configurable<Color> negColor = new Configurable<>(
            new Descriptor("Color", "Neg Color", "Neg Color"),
            DEFAULT_NEG_COLOR
    );

    @SerializedName("backgroundColor")
    @Expose
    private Configurable<Color> backgroundColor = new Configurable<>(
            new Descriptor("Color", "Background Color", "Background Color"),
            DEFAULT_BACKGROUND_COLOR
    );

    @SerializedName("font")
    @Expose
    private Configurable<Font> font = new Configurable<>(
            new Descriptor("Font", "Font", "Font"),
            Font.font(DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE)
    );

    public Configurable<Color> getAffColor() {
        return affColor;
    }

    public Configurable<Color> getNegColor() {
        return negColor;
    }

    public Configurable<Color> getBackgroundColor() {
        return backgroundColor;
    }

    public Configurable<Font> getFont() {
        return font;
    }
}
