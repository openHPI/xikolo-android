package de.xikolo.util;

public enum PlaybackSpeed {

    x07(0.7f), x10(1.0f), x13(1.3f), x15(1.5f), x18(1.8f), x20(2.0f);

    private float speed;

    private PlaybackSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return "x" + String.valueOf(speed);
    }

    public static PlaybackSpeed get(String speed) {
        switch (speed) {
            case "x0.7":
                return PlaybackSpeed.x07;
            case "x1.0":
                return PlaybackSpeed.x10;
            case "x1.3":
                return PlaybackSpeed.x13;
            case "x1.5":
                return PlaybackSpeed.x15;
            case "x1.8":
                return PlaybackSpeed.x18;
            case "x2.0":
                return PlaybackSpeed.x20;
            default:
                return null;
        }

    }

}
