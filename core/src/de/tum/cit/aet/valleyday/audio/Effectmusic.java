package de.tum.cit.aet.valleyday.audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;


public enum Effectmusic {

    //PLANT("plant.wav", 0.2f),
    //SHOO("shoo.wav", 0.2f),
    // HARVEST("harvest.wav", 0.2f),
    //CLEAR_ROTTEN("clear.wav", 0.2f),
    Walking("Walking.ogg",3f);


    private final Sound sound;
    private final float volume;

    Effectmusic(String fileName, float volume) {
        this.sound = Gdx.audio.newSound(Gdx.files.internal("audio/" + fileName));
        this.volume = volume;
    }

    public void play() {
        sound.play(volume);
    }

    public void dispose() {
        sound.dispose();
    }
}
