import game_items.GameObject;
import game_items.GameMap;
import game_items.PacMan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private PacMan pacMan;
    private GameMap gameMap;
    private final ArrayList<GameObject> gameObjects = new ArrayList<>();

    public GamePanel() {
        setBackground(Color.BLACK);
        gameMap = new GameMap();
        setPreferredSize(new Dimension(gameMap.getWidth(), gameMap.getHeight()));
        pacMan = new PacMan(gameMap);

        gameObjects.add(gameMap);
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
        repaint();
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

        // --- AFFICHER LE SCORE ---
        // On dessine le score par-dessus tout le reste (HUD)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        // On affiche le score en haut à gauche
        g.drawString("Score: " + pacMan.getScore(), 20, 30);
    }
}