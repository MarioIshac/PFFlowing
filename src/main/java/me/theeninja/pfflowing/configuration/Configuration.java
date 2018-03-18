package me.theeninja.pfflowing.configuration;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Configuration {

    private final String DEFAULT_FONT_FAMILY = "Times New Roman";
    private final int DEFAULT_FONT_SIZE = 12;
    private final Color DEFAULT_AFF_COLOR = Color.BLACK;
    private final Color DEFAULT_NEG_COLOR = Color.RED;
    private final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    @Expose
    @SerializedName("affColor")
    private ObjectProperty<Color> affColor = new SimpleObjectProperty<>(DEFAULT_AFF_COLOR);

    @SerializedName("negColor")
    @Expose
    private ObjectProperty<Color> negColor = new SimpleObjectProperty<>(DEFAULT_NEG_COLOR);

    @SerializedName("backgroundColor")
    @Expose
    private ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(DEFAULT_BACKGROUND_COLOR);

    @SerializedName("font")
    @Expose
    private ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font(DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE));

    public Color getAffColor() {
        return affColor.get();
    }

    public ObjectProperty<Color> affColorProperty() {
        return affColor;
    }

    public void setAffColor(Color affColor) {
        this.affColor.set(affColor);
    }

    public Color getNegColor() {
        return negColor.get();
    }

    public ObjectProperty<Color> negColorProperty() {
        return negColor;
    }

    public void setNegColor(Color negColor) {
        this.negColor.set(negColor);
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public Font getFont() {
        return font.get();
    }

    public ObjectProperty<Font> fontProperty() {
        return font;
    }

    public void setFont(Font font) {
        this.font.set(font);
    }
}
