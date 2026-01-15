package de.tum.cit.aet.valleyday.audio;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;


public enum Effectmusic {

    Plant("plant.ogg", 2f),
    //SHOO("shoo.wav", 0.2f),
    Harvest("harvest.ogg", 2f),
    //CLEAR_ROTTEN("clear.wav", 0.2f),
    Walking("Walking.ogg",3f),
    DebrisDestory("debris_destory.ogg",2f),
    CollectItem("collect_items.ogg", 1.0f);
    //ChickenPickup("chicken_pickup",1.0f);

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
