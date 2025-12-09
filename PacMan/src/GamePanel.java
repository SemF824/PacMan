import game_items.Cherry;
import game_items.GameObject;
import game_items.GameMap;
import game_items.Ghost;
import game_items.PacMan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private PacMan pacMan;
    private GameMap gameMap;
    private Cherry cherry;
    private ArrayList<Ghost> ghosts = new ArrayList<>();
    private final ArrayList<GameObject> gameObjects = new ArrayList<>();

    private Timer timer;

    public GamePanel() {
        setBackground(Color.BLACK);
        // On initialise le jeu pour la première fois
        initGame();

        timer = new Timer(16, this);
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    // --- INITIALISATION COMPLÈTE (Nouvelle partie) ---
    private void initGame() {
        // On vide tout pour repartir à zéro
        ghosts.clear();
        gameObjects.clear();

        // Nouvelle carte (les points réapparaissent)
        gameMap = new GameMap();
        setPreferredSize(new Dimension(gameMap.getWidth(), gameMap.getHeight()));

        // Nouveau PacMan (Score 0, Vies 3)
        pacMan = new PacMan(gameMap);
        cherry = new Cherry(gameMap, pacMan);

        // Nouveaux fantômes avec leurs personnalités
        int gridSize = 32;
        int yHouse = 9 * gridSize;
        int house1_x = 9 * gridSize;
        int house2_x = 28 * gridSize;

        // 1. Blinky (Rouge)
        ghosts.add(new Ghost(gameMap, pacMan, Color.RED, house1_x, yHouse, 0.75, 1));
        // 2. Pinky (Rose)
        ghosts.add(new Ghost(gameMap, pacMan, Color.PINK, house1_x + gridSize, yHouse, 0.60, 2));
        // 3. Inky (Cyan)
        ghosts.add(new Ghost(gameMap, pacMan, Color.CYAN, house2_x, yHouse, 0.40, 2));
        // 4. Clyde (Orange)
        ghosts.add(new Ghost(gameMap, pacMan, Color.ORANGE, house2_x + gridSize, yHouse, 0.20, 2));

        // On ajoute tout à la liste d'affichage
        gameObjects.add(gameMap);
        gameObjects.add(cherry);
        gameObjects.addAll(ghosts);
        gameObjects.add(pacMan);
    }

    // --- RESET DES POSITIONS (Après une mort, mais il reste des vies) ---
    private void resetPositions() {
        pacMan.resetPosition(); // PacMan retourne au départ
        for (Ghost ghost : ghosts) {
            ghost.reset(); // Les fantômes retournent dans la maison
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (GameObject gameObject : gameObjects) {
            gameObject.update();
        }

        int foodStatus = pacMan.checkFood();
        if (foodStatus == 1) {
            pacMan.addScore(10);
        } else if (foodStatus == 2) {
            pacMan.addScore(50);
            for (Ghost g : ghosts) g.startFrightened();
        }

        checkGhostCollisions();
        repaint();
    }

    private void checkGhostCollisions() {
        Rectangle pacManBounds = pacMan.getBounds();

        for (Ghost ghost : ghosts) {
            if (ghost.getBounds().intersects(pacManBounds)) {

                if (ghost.isFrightened()) {
                    // Manger le fantôme
                    System.out.println("Miam ! (+200)");
                    pacMan.addScore(200);
                    ghost.die();
                } else {
                    // --- MORT DE PACMAN ---
                    pacMan.loseLife();
                    System.out.println("Aie ! Vies restantes : " + pacMan.getLives());

                    if (pacMan.getLives() > 0) {
                        // Il reste des vies : on remet tout le monde à sa place
                        resetPositions();
                    } else {
                        // Plus de vie : GAME OVER -> On recommence tout
                        System.out.println("GAME OVER - RESET");
                        initGame();

                        // --- IMPORTANT : ARRÊTER LA BOUCLE ---
                        return; // Empêche le crash ConcurrentModificationException
                    }
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_UP: pacMan.keyUp(); break;
            case KeyEvent.VK_DOWN: pacMan.keyDown(); break;
            case KeyEvent.VK_LEFT: pacMan.keyLeft(); break;
            case KeyEvent.VK_RIGHT: pacMan.keyRight(); break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (GameObject gameObject : gameObjects) {
            gameObject.draw(g);
        }

        // Affichage Score et Vies
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + pacMan.getScore(), 20, 30);
        g.drawString("Vies: " + pacMan.getLives(), 150, 30);
    }
}