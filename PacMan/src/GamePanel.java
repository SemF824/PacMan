import game_items.Cherry;
import game_items.GameObject;
import game_items.GameMap;
import game_items.Ghost;
import game_items.PacMan;
import game_items.Sounds;

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

    // 1. Déclarer le gestionnaire de son
    private Sounds soundEffect = new Sounds();

    // 2. Méthode utilitaire pour jouer un son (plus propre)
    public void playSE(int i) {
        soundEffect.setFile(i);
        soundEffect.play();
    }
    public void playMusic(int i) {
        soundEffect.setFile(i);
        soundEffect.loop(); // Utilise la méthode loop() de ta classe Sounds
    }


    public GamePanel() {
        setBackground(Color.BLACK);
        gameMap = new GameMap();
        setPreferredSize(new Dimension(gameMap.getWidth(), gameMap.getHeight()));

        pacMan = new PacMan(gameMap);
        cherry = new Cherry(gameMap, pacMan);

        gameObjects.addAll(ghosts);
        gameObjects.add(pacMan);

        // DÉMARRAGE DU MOTEUR AUDIO ICI
        playMusic(0); // 0 = index de ta musique de fond dans Sounds.java

        Timer timer = new Timer(16, this);
        timer.start();

        // --- PLACEMENT DES FANTOMES DANS LES 2 MAISONS ---

        int gridSize = 32;
        int yHouse = 9 * gridSize; // La ligne 9 est à l'intérieur de la maison

        // MAISON 1 (GAUCHE) - Autour de la colonne 9
        int house1_x = 9 * gridSize;
        Ghost red   = new Ghost(gameMap, Color.RED, house1_x, yHouse);
        Ghost pink  = new Ghost(gameMap, Color.PINK, house1_x + gridSize, yHouse); // Un peu à droite

        // MAISON 2 (DROITE) - Autour de la colonne 28 (19 + 9)
        int house2_x = 28 * gridSize;
        Ghost cyan   = new Ghost(gameMap, Color.CYAN, house2_x, yHouse);
        Ghost orange = new Ghost(gameMap, Color.ORANGE, house2_x + gridSize, yHouse); // Un peu à droite

        ghosts.add(red);
        ghosts.add(pink);
        ghosts.add(cyan);
        ghosts.add(orange);

        // -------------------------------------------------

        gameObjects.add(gameMap);
        gameObjects.add(cherry);
        gameObjects.addAll(ghosts);
        gameObjects.add(pacMan);


        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 1. On capture le score AVANT la mise à jour
        int oldScore = pacMan.getScore();

        // 2. Mise à jour de tous les objets (PacMan bouge et mange potentiellement ici)
        for (GameObject gameObject : gameObjects) {
            gameObject.update();
        }

        // 3. On compare : Si le score a augmenté, c'est qu'on a mangé !
        if (pacMan.getScore() > oldScore) {
            playSE(1); // 1 = index du bruitage "chomp"
        }

        checkGhostCollisions();
        repaint();
    }

    private void checkGhostCollisions() {
        Rectangle pacManBounds = pacMan.getBounds();
        for (Ghost ghost : ghosts) {
            if (ghost.getBounds().intersects(pacManBounds)) {
                System.out.println("Mort !");
                resetGame();
            }
        }
    }

    private void resetGame() {
        pacMan.reset();
        for (Ghost ghost : ghosts) {
            ghost.reset();
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

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + pacMan.getScore(), 20, 30);
    }
}