package me.theeninja.pfflowing.flowingregions;

import java.util.List;

public class CharacterFormatting implements Comparable<CharacterFormatting> {
    private final List<CharacterStyle> characterStyles;

    public CharacterFormatting(List<CharacterStyle> characterStyles) {
        this.characterStyles = characterStyles;
    }

    public boolean isStyled(CharacterStyle characterStyle) {
        return characterStyles.contains(characterStyle);
    }

    public List<CharacterStyle> getCharacterStyles() {
        return characterStyles;
    }

    @Override
    public int compareTo(CharacterFormatting characterFormatting) {
        return this.getCharacterStyles().contains(characterFormatting.getCharacterStyles()) ? 1 : -1;
    }


}
