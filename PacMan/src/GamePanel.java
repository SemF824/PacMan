import game_items.GameObject;
import game_items.GameMap; // Import de la carte
import game_items.PacMan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    // The PacMan instance (player)
    private PacMan pacMan;
    // The game map which holds tiles and collision data
    private GameMap gameMap;
    // Ordered list of objects to update and draw (map first so PacMan is drawn on top)
    private final ArrayList<GameObject> gameObjects = new ArrayList<>();

    public GamePanel() {
        setBackground(Color.BLACK);

        // 1) Create the map object. The map builds its level data and exposes getWidth/getHeight.
        gameMap = new GameMap();

        // Set the panel preferred size to the map pixel size so the window can pack()
        setPreferredSize(new Dimension(gameMap.getWidth(), gameMap.getHeight()));

        // 2) Create PacMan and give it a reference to the map (needed for collision checks).
        pacMan = new PacMan(gameMap);

        // 3) Add objects in drawing order: map first, then PacMan on top.
        gameObjects.add(gameMap);
        gameObjects.add(pacMan);

        // 4) Start a timer for the game loop (about 60 FPS -> 16ms tick).
        Timer timer = new Timer(16, this);
        timer.start();

        // 5) Input: listen for arrow keys to control PacMan.
        setFocusable(true);
        addKeyListener(this);
    }

    // Game loop tick: update all objects then request repaint
    @Override
    public void actionPerformed(ActionEvent e) {
        for (GameObject gameObject : gameObjects) {
            gameObject.update();
        }
        repaint();
    }

    // Key input: forward to PacMan's convenience methods (they set a "future direction").
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

    // Draw all game objects in order (map then game entities)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (GameObject gameObject : gameObjects) {
            gameObject.draw(g);
        }
    }
}