import game_items.Cherry;
import game_items.GameObject;
import game_items.GameMap;
import game_items.Ghost;
import game_items.PacMan;
import game_items.Sound;

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
    private Sound sound = new Sound(); // Un seul objet Sound

    private boolean gameOver = false;
    private boolean isIntro = true;
    private int introTimer = 0;

    private JButton restartButton;

    public GamePanel() {
        setBackground(Color.BLACK);
        setLayout(null);

        initGame();

        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.setFocusable(false);
        restartButton.setVisible(false);
        restartButton.setBounds(100, 100, 140, 40);

        restartButton.addActionListener(e -> {
            sound.stopAll();
            initGame();
            gameOver = false;
            restartButton.setVisible(false);
            requestFocusInWindow();
        });
        add(restartButton);

        timer = new Timer(16, this);
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    private void initGame() {
        ghosts.clear();
        gameObjects.clear();

        // --- GESTION SONORE ---
        sound.stopAll();

        // BAISSER LE VOLUME DE LA SIRENE (Index 5)
        // -20.0f réduit beaucoup le volume. Ajuste selon tes goûts (-10.0f est plus fort)
        sound.setVolume(5, -20.0f);

        // On baisse aussi un peu l'intro si besoin
        sound.setVolume(0, -5.0f);

        sound.play(0); // Intro

        isIntro = true;
        introTimer = 260;

        gameMap = new GameMap();
        setPreferredSize(new Dimension(gameMap.getWidth(), gameMap.getHeight()));

        pacMan = new PacMan(gameMap);
        cherry = new Cherry(gameMap, pacMan);

        int gridSize = 32;
        int yHouse = 9 * gridSize;
        int house1_x = 9 * gridSize;
        int house2_x = 28 * gridSize;

        ghosts.add(new Ghost(gameMap, pacMan, Color.RED, house1_x, yHouse, 0.75, 1));
        ghosts.add(new Ghost(gameMap, pacMan, Color.PINK, house1_x + gridSize, yHouse, 0.60, 2));
        ghosts.add(new Ghost(gameMap, pacMan, Color.CYAN, house2_x, yHouse, 0.40, 2));
        ghosts.add(new Ghost(gameMap, pacMan, Color.ORANGE, house2_x + gridSize, yHouse, 0.20, 2));

        gameObjects.add(gameMap);
        gameObjects.add(cherry);
        gameObjects.addAll(ghosts);
        gameObjects.add(pacMan);
    }

    private void resetPositions() {
        pacMan.resetPosition();
        for (Ghost ghost : ghosts) ghost.reset();
        sound.play(5);
        sound.loop(5);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isIntro) {
            introTimer--;
            if (introTimer <= 0) {
                isIntro = false;
                sound.play(5);
                sound.loop(5);
            }
            repaint();
            return;
        }

        if (gameOver) {
            if (restartButton.isVisible()) {
                int btnW = 140, btnH = 40;
                restartButton.setBounds((getWidth()/2)-(btnW/2), (getHeight()/2)+50, btnW, btnH);
            }
            return;
        }

        for (GameObject gameObject : gameObjects) gameObject.update();

        int foodStatus = pacMan.checkFood();

        if (foodStatus == 1) {
            pacMan.addScore(10);
            sound.play(1); // CHOMP
        }
        else if (foodStatus == 2) {
            pacMan.addScore(50);
            sound.play(6); // POWER
            for (Ghost g : ghosts) g.startFrightened();
        }

        if (pacMan.getBounds().intersects(new Rectangle(cherry.getX(), cherry.getY(), 32, 32)) && cherry.isVisible()) {
            sound.play(4); // FRUIT
        }

        checkGhostCollisions();
        repaint();
    }

    private void checkGhostCollisions() {
        Rectangle pacManBounds = pacMan.getBounds();

        for (Ghost ghost : ghosts) {
            if (ghost.getBounds().intersects(pacManBounds)) {
                if (ghost.isDead()) continue;

                if (ghost.isFrightened()) {
                    pacMan.addScore(200);
                    sound.play(2); // EAT GHOST
                    ghost.die();
                }
                else {
                    pacMan.loseLife();
                    sound.stop(5);
                    sound.play(3); // DEATH

                    if (pacMan.getLives() > 0) {
                        resetPositions();
                    } else {
                        System.out.println("GAME OVER");
                        gameOver = true;
                        restartButton.setVisible(true);
                    }
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver || isIntro) return;
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
        for (GameObject gameObject : gameObjects) gameObject.draw(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + pacMan.getScore(), 20, 30);
        g.drawString("Vies: " + pacMan.getLives(), 150, 30);

        if (isIntro) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            String ready = "READY!";
            int w = g.getFontMetrics().stringWidth(ready);
            g.drawString(ready, (getWidth() - w)/2, getHeight()/2 + 15);
        }

        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            String text = "GAME OVER";
            FontMetrics m = g.getFontMetrics();
            g.drawString(text, (getWidth() - m.stringWidth(text))/2, getHeight()/2 - 20);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String scoreText = "Score Final : " + pacMan.getScore();
            m = g.getFontMetrics();
            g.drawString(scoreText, (getWidth() - m.stringWidth(scoreText))/2, getHeight()/2 + 30);
        }
    }
}