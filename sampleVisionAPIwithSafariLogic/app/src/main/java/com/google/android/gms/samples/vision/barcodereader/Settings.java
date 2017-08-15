package com.google.android.gms.samples.vision.barcodereader;

/**
 * Created by Katherine on 29/7/17.
 */

public class Settings {

    int durationTemp;
    int durationUrgent;

    public Settings(int durationTemp, int durationUrgent) {
        this.durationTemp = durationTemp;
        this.durationUrgent = durationUrgent;
    }

    public int getDurationTemp() {
        return durationTemp;
    }

    public int getDurationUrgent() {
        return durationUrgent;
    }
}
