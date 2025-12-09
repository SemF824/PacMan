import game_items.GameObject;
import game_items.PacMan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * GamePanel est le conteneur principal du jeu.
 * Il gère la boucle de jeu, l'affichage graphique et les entrées clavier.
 */
public class GamePanel extends JPanel implements KeyListener, ActionListener {

    // L'instance spécifique du joueur (PacMan) pour pouvoir le contrôler directement
    private PacMan pacMan;

    // Une liste contenant tous les objets du jeu (PacMan, Fantômes, Murs, etc.)
    // Cela permet d'utiliser le polymorphisme pour les mettre à jour et les dessiner tous ensemble.
    private final ArrayList<GameObject> gameObjects = new ArrayList<>();

    /**
     * Constructeur : Initialise la fenêtre de jeu et démarre la boucle.
     */
    public GamePanel() {
        // Définit la couleur de fond du panneau en noir
        setBackground(Color.BLACK);

        // Création et ajout de PacMan à la liste des objets à gérer
        pacMan = new PacMan();
        gameObjects.add(pacMan);

        // Configuration de la boucle de jeu (Game Loop)
        // Le Timer déclenche un événement "ActionEvent" toutes les 16 millisecondes.
        // Calcul : 1000 ms / 16 ms ≈ 62.5 images par seconde (FPS).
        Timer timer = new Timer(16, this);
        timer.start();

        // Indispensable pour que le panneau puisse recevoir les événements clavier.
        // Sans cela, les touches pressées ne seraient pas détectées.
        setFocusable(true);
        addKeyListener(this);
    }

    /**
     * Cette méthode est appelée par le Timer toutes les ~16ms.
     * C'est le CŒUR de la logique du jeu (Mise à jour).
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // 1. Mise à jour de la logique (Position, Collisions, État)
        for (GameObject gameObject : gameObjects) {
            gameObject.update();
        }

        // 2. Demande de rafraîchissement de l'écran
        // Cela va indirectement appeler la méthode paintComponent(Graphics g)
        repaint();
    }

    /**
     * Gestion des entrées utilisateur (Appui sur une touche).
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // On vérifie quel code de touche a été pressé et on dirige PacMan
        switch(e.getKeyCode()) {
            case KeyEvent.VK_UP: {
                pacMan.keyUp();
                break;
            }
            case KeyEvent.VK_DOWN: {
                pacMan.keyDown();
                break;
            }
            case KeyEvent.VK_LEFT: {
                pacMan.keyLeft();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                pacMan.keyRight();
                break;
            }
        }
    }

    // Méthodes requises par l'interface KeyListener mais non utilisées ici
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}


    /**
     * Cette méthode gère le RENDU graphique.
     * Elle est appelée automatiquement par Swing après un appel à repaint().
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Appelle la méthode de la classe parente (JPanel) pour nettoyer l'écran
        // et dessiner la couleur de fond (noir). Si on l'oublie, l'écran ne s'efface pas (effet de traînée).
        super.paintComponent(g);

        // Dessine tous les objets du jeu par-dessus le fond
        for (GameObject gameObject : gameObjects) {
            gameObject.draw(g);
        }
    }
}