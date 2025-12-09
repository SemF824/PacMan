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

    public GamePanel() {
        setBackground(Color.BLACK);
        gameMap = new GameMap();
        setPreferredSize(new Dimension(gameMap.getWidth(), gameMap.getHeight()));

        pacMan = new PacMan(gameMap);
        cherry = new Cherry(gameMap, pacMan);

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

        Timer timer = new Timer(16, this);
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (GameObject gameObject : gameObjects) {
            gameObject.update();
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