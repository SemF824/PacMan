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

    // --- NOUVEAU : GESTION DE L'INTERFACE DE FIN ---
    private boolean gameOver = false;
    private JButton restartButton;
    // -----------------------------------------------

    public GamePanel() {
        setBackground(Color.BLACK);
        // Important pour pouvoir placer le bouton manuellement au centre
        setLayout(null);

        // Initialisation du jeu
        initGame();

        // --- CRÉATION DU BOUTON RESTART ---
        restartButton = new JButton("Recommencer");
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.setFocusable(false); // Important : pour ne pas voler le focus du clavier
        restartButton.setVisible(false); // Caché au début

        // Calcul pour centrer le bouton (approximatif, sera ajusté si besoin)
        int btnW = 140;
        int btnH = 40;
        // La map fait environ 1200px de large
        restartButton.setBounds((gameMap.getWidth() / 2) - (btnW / 2), (gameMap.getHeight() / 2) + 50, btnW, btnH);

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Quand on clique : on relance tout
                initGame();
                gameOver = false;
                restartButton.setVisible(false);
                // On redonne le focus au panneau pour que les flèches remarchent
                requestFocusInWindow();
            }
        });

        add(restartButton);
        // -----------------------------------

        timer = new Timer(16, this);
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    private void initGame() {
        ghosts.clear();
        gameObjects.clear();

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
        for (Ghost ghost : ghosts) {
            ghost.reset();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // SI GAME OVER : On ne met plus rien à jour, le jeu est figé
        if (gameOver) {
            return;
        }

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
                    pacMan.addScore(200);
                    ghost.die();
                } else {
                    pacMan.loseLife();
                    System.out.println("Aie ! Vies restantes : " + pacMan.getLives());

                    if (pacMan.getLives() > 0) {
                        resetPositions();
                    } else {
                        // --- C'EST ICI QUE LE GAME OVER S'ACTIVE ---
                        System.out.println("GAME OVER");
                        gameOver = true; // On active l'état de fin
                        restartButton.setVisible(true); // On affiche le bouton
                    }
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // On bloque les touches si le jeu est fini
        if (gameOver) return;

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

        // Affichage HUD normal
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + pacMan.getScore(), 20, 30);
        g.drawString("Vies: " + pacMan.getLives(), 150, 30);

        // --- DESSIN DU GAME OVER ---
        if (gameOver) {
            // 1. Fond semi-transparent noir
            g.setColor(new Color(0, 0, 0, 150)); // Noir avec transparence
            g.fillRect(0, 0, getWidth(), getHeight());

            // 2. Texte "GAME OVER"
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            String text = "GAME OVER";
            FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(text)) / 2;
            int y = getHeight() / 2 - 20;
            g.drawString(text, x, y);

            // 3. Texte du Score Final
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String scoreText = "Score Final : " + pacMan.getScore();
            metrics = g.getFontMetrics();
            x = (getWidth() - metrics.stringWidth(scoreText)) / 2;
            y = getHeight() / 2 + 30;
            g.drawString(scoreText, x, y);

            // Le bouton se dessine tout seul par dessus car il est ajouté au panel
        }
    }
}