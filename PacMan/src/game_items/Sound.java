package game_items;

import javax.sound.sampled.*;
import java.net.URL;

public class Sound {

    private Clip[] clips = new Clip[10];
    private URL[] soundURL = new URL[10];

    public Sound() {
        soundURL[0] = getClass().getResource("/sounds/intro.wav");
        soundURL[1] = getClass().getResource("/sounds/chomp.wav");
        soundURL[2] = getClass().getResource("/sounds/eatghost.wav");
        soundURL[3] = getClass().getResource("/sounds/death.wav");
        soundURL[4] = getClass().getResource("/sounds/fruit.wav");
        soundURL[5] = getClass().getResource("/sounds/siren.wav");
        soundURL[6] = getClass().getResource("/sounds/Power.wav");

        for (int i = 0; i < soundURL.length; i++) {
            if (soundURL[i] != null) {
                loadSound(i);
            }
        }
    }

    private void loadSound(int i) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clips[i] = AudioSystem.getClip();
            clips[i].open(ais);

            // OPTIMISATION : On charge un peu de données en avance
            // Cela réduit le lag au premier démarrage du son
            clips[i].start();
            clips[i].stop();
            clips[i].setFramePosition(0);

        } catch (Exception e) {
            System.out.println("Erreur chargement son index " + i);
        }
    }

    public void play(int i) {
        if (clips[i] == null) return;

        // Si le son est déjà en cours, on l'arrête pour le relancer
        if (clips[i].isRunning()) {
            clips[i].stop();
        }
        clips[i].setFramePosition(0);
        clips[i].start();
    }

    public void loop(int i) {
        if (clips[i] == null) return;
        clips[i].loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(int i) {
        if (clips[i] == null) return;
        if (clips[i].isRunning()) {
            clips[i].stop();
        }
    }

    public void stopAll() {
        for (Clip c : clips) {
            if (c != null && c.isRunning()) c.stop();
        }
    }

    // --- NOUVEAU : CONTRÔLE DU VOLUME ---
    // value est en décibels.
    // -80.0f = muet, 6.0f = max, -10.0f ou -20.0f = bien pour le fond
    public void setVolume(int i, float value) {
        try {
            if (clips[i] != null) {
                FloatControl gainControl = (FloatControl) clips[i].getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(value);
            }
        } catch (Exception e) {
        }
    }
}