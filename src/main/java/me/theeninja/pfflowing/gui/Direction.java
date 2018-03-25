package me.theeninja.pfflowing.gui;

public enum Direction {
    UP(0, 1),
    RIGHT(-1, 0),
    DOWN(0, -1),
    LEFT(-1, 0);

    private final int xAxisDirection;
    private final int yAxisDirection;

    Direction(int xAxisDirection, int yAxisDirection) {
        this.xAxisDirection = xAxisDirection;
        this.yAxisDirection = yAxisDirection;
    }

    public int getXAxisDirection() {
        return xAxisDirection;
    }
    public int getYAxisDirection() {
        return yAxisDirection;
    }
}
