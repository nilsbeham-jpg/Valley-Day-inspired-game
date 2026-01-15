package de.tum.cit.aet.valleyday.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * This enum is used to manage the music tracks in the game.
 * Currently, only one track is used, but this could be extended to include multiple tracks.
 * Using an enum for this purpose is a good practice, as it allows for easy management of the music tracks
 * and prevents the same track from being loaded into memory multiple times.
 * See the assets/audio folder for the actual music files.
 * Feel free to add your own music tracks and use them in the game!
 */
public enum MusicTrack {
    
    BACKGROUND("background.mp3", 0.1f), // is literally a constructor call- Eqivalent to public static final MusicTrack BACKGROUND = new MusicTrack("background.mp3", 0.2f);
    MENU("thetreadofwarmix.ogg",0.2f),
    VICTORY("victory.ogg", 0.4f),
    GAME_OVER("losetrumpet.ogg", 0.2f);
    /** The music file owned by this variant. */
    private final Music music; // music is a field inside the enum instance MusicTrack.BACKGROUND.
    private static MusicTrack current;


    MusicTrack(String fileName, float volume) {
        this.music = Gdx.audio.newMusic(Gdx.files.internal("audio/" + fileName));
        this.music.setLooping(true);
        this.music.setVolume(volume);
    }

    public void play() {
        if (!music.isPlaying()) {
            music.play();
        }
    }

    public void stop() {
        if (music.isPlaying()) {
            music.stop();
        }
    }
    
    /**
     * Play this music track.
     * This will not stop other music from playing - if you add more tracks, you will have to handle that yourself.
     */

    public static void playExclusive(MusicTrack track) {
        if (current == track) return;

        if (current != null) {
            current.stop();
        }

        current = track;
        current.play();
    }

}
