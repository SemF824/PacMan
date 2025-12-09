package game_items;

import javax.sound.sampled.*;
import java.net.URL;

public class Sounds {

    private Clip clip;
    // Tableau pour stocker les chemins (URL) vers tes fichiers audio
    private URL[] soundURL = new URL[5];

    public Sounds() {
        // CHARGEMENT DES SONS
        // Attention : Le chemin commence par "/" car "resources" est une racine (Root)
        // Indice 0 = Musique de fond (Intro)
        soundURL[0] = getClass().getResource("/sounds/the-pacman-variations-17844.wav");
        // Indice 1 = Bruitage Manger (Chomp)
        soundURL[1] = getClass().getResource("/sounds/manger.wav");

        // CRASH-TEST DEBUG : Si ça affiche null, le chemin est faux ou le fichier manque
        if (soundURL[0] == null) System.err.println("ERREUR CRITIQUE : Impossible de trouver intro.wav");
        if (soundURL[1] == null) System.err.println("ERREUR CRITIQUE : Impossible de trouver chomp.wav");
    }

    public void setFile(int i) {
        try {
            if (soundURL[i] != null) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
                clip = AudioSystem.getClip();
                clip.open(ais);
            }
        } catch (Exception e) {
            System.err.println("Erreur de lecture audio (Format incorrect ? MP3 ?)");
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip == null) return;
        clip.setFramePosition(0); // Rembobine au début
        clip.start();
    }

    public void loop() {
        if (clip == null) return;
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        if (clip == null) return;
        clip.stop();
    }
}