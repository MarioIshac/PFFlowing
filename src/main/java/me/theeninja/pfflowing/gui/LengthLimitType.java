package me.theeninja.pfflowing.gui;

import me.theeninja.pfflowing.utils.Utils;

public enum LengthLimitType {
    WORD(" "),
    CHARACTER(Utils.ZERO_LENGTH_STRING),
    LINE("\n");

    private final String split;

    private LengthLimitType(String split) {
        this.split = split;
    }

    public String getSplit() {
        return split;
    }
}