package me.theeninja.pfflowing.gui;

import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import me.theeninja.pfflowing.Utils;
import me.theeninja.pfflowing.utils.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ColorUseManager implements Iterator<Pair<Color, Background>> {

    private static final List<Color> DEFAULT_COLOR_CHOICES = Arrays.asList(
        Color.rgb(2, 63, 165),
            Color.rgb(125, 135, 185),
            Color.rgb(190, 193, 212),
            Color.rgb(214, 188, 192),
            Color.rgb(187, 119, 132),
            Color.rgb(142, 6, 59),
            Color.rgb(74, 111, 227),
            Color.rgb(133, 149, 225),
            Color.rgb(181, 187, 227),
            Color.rgb(230, 175, 185),
            Color.rgb(224, 123, 145),
            Color.rgb(211, 63, 106),
            Color.rgb(17, 198, 56),
            Color.rgb(141, 213, 147),
            Color.rgb(198, 222, 199),
            Color.rgb(234, 211, 198),
            Color.rgb(240, 185, 141),
            Color.rgb(239, 151, 8),
            Color.rgb(15, 207, 192),
            Color.rgb(156, 222, 214),
            Color.rgb(213, 234, 231),
            Color.rgb(243, 225, 235),
            Color.rgb(246, 196, 225),
            Color.rgb(247, 156, 212)
    );
    private static final List<Background> DEFAULT_BACKGROUND_CHOICES = Arrays.asList(
            Background.EMPTY,
            Utils.generateBackgroundOfColor(Color.YELLOW),
            Utils.generateBackgroundOfColor(Color.LIGHTGRAY)

    );
    private final List<Color> colorChoices;
    private final List<Background> backgroundChoices;

    public ColorUseManager() {
        this(DEFAULT_COLOR_CHOICES, DEFAULT_BACKGROUND_CHOICES);
    }

    public ColorUseManager(List<Color> colorChoices, List<Background> backgroundChoices) {
        this.colorChoices = colorChoices;
        this.backgroundChoices = backgroundChoices;

        currentPair = new Pair<>(DEFAULT_COLOR_CHOICES.get(0), DEFAULT_BACKGROUND_CHOICES.get(0));
    }

    public List<Color> getColorChoices() {
        return colorChoices;
    }

    public List<Background> getBackgroundChoices() {
        return backgroundChoices;
    }

    private Pair<Color, Background> currentPair;

    @Override
    public boolean hasNext() {
        return !(Utils.isLastElement(colorChoices, currentPair.getFirst()) &&
                Utils.isLastElement(backgroundChoices, currentPair.getSecond()));
    }

    @Override
    public Pair<Color, Background> next() {
        Color currentColor = currentPair.getFirst();
        Background currentBackground = currentPair.getSecond();

        if (Utils.isLastElement(colorChoices, currentColor)) {
            currentColor = colorChoices.get(0);
            currentBackground = Utils.getNextElement(backgroundChoices, currentBackground);
        }
        else {
            currentColor = Utils.getNextElement(colorChoices, currentColor);
        }

        currentPair.setPair(currentColor, currentBackground);

        return new Pair<>(currentColor, currentBackground);
    }
}
