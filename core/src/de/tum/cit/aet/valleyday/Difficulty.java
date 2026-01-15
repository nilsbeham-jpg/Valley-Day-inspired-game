package de.tum.cit.aet.valleyday;

public enum Difficulty {

    EASY(
            2,      // max wildlife
            1.2f,   // wildlife speed multiplier
            15       // exit quota
    ),

    NORMAL(
            3,
            1.0f,
            30
    ),

    HARD(
            5,
            0.75f,
            50
    );

    public final int maxWildlife;
    public final float speedMultiplier;
    public final int exitQuota;

    Difficulty(int maxWildlife, float speedMultiplier, int exitQuota) {
        this.maxWildlife = maxWildlife;
        this.speedMultiplier = speedMultiplier;
        this.exitQuota = exitQuota;
    }
}
