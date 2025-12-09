import game_items.GameObject;
import game_items.PacMan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private PacMan pacMan;
    private final ArrayList<GameObject> gameObjects = new ArrayList<>();

    public GamePanel() {
        setBackground(Color.BLACK);

        pacMan = new PacMan();
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

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (GameObject gameObject : gameObjects) {
            gameObject.draw(g);
        }
    }
}