package de.tum.cit.aet.valleyday;

/**
 * Defines the difficulty levels available in the game.
 *
 * Each difficulty bundles a set of gameplay parameters that directly
 * influence challenge and pacing. These values are read by the game
 * during initialization and applied consistently across the map,
 * wildlife behavior, and win conditions.
 *
 * Difficulty affects:
 * - how many wildlife entities can exist at the same time
 * - how fast wildlife moves
 * - how many crops must be harvested to unlock the exit
 */

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


    /**
     * Creates a difficulty configuration with fixed gameplay parameters.
     *
     * @param maxWildlife     maximum simultaneous wildlife
     * @param speedMultiplier wildlife speed multiplier
     * @param exitQuota      required harvest count to finish the level
     */
    Difficulty(int maxWildlife, float speedMultiplier, int exitQuota) {
        this.maxWildlife = maxWildlife;
        this.speedMultiplier = speedMultiplier;
        this.exitQuota = exitQuota;
    }
}
