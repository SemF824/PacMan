import game_items.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements KeyListener, ActionListener {

    private Sound sound = new Sound();
    private boolean isFrightenedMode = false;
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

        sound.stopAll(); // Coupe les anciens sons si on redémarre
        sound.loop(5); // 5 = Siren.wav (Ambiance de fond)
        this.isFrightenedMode = false;
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

        if (cherry.isVisible() && pacMan.getBounds().intersects(new Rectangle(cherry.getX(), cherry.getY(), 32, 32))) {
                cherry.eat();   // La cerise gère son score et son reset
                sound.play(4);  // <-- Le GamePanel joue le son "Fruit"
        }

        int foodStatus = pacMan.checkFood();
        if (foodStatus == 1) {
            pacMan.addScore(10);
            sound.play(1); // <-- Son "Chomp" pour un petit point
        } else if (foodStatus == 2) {
            pacMan.addScore(50);
            for (Ghost g : ghosts) g.startFrightened();
            if (!isFrightenedMode) {
                sound.stop(5); // On coupe la sirène
                sound.loop(6); // On lance la musique "Power"
                isFrightenedMode = true;
            }
        }

        if (isFrightenedMode) {
            boolean ghostsAreSafe = true;
            for (Ghost g : ghosts) {
                if (g.isFrightened()) {
                    ghostsAreSafe = false; // Il en reste au moins un qui a peur
                    break;
                }
            }

            // Si plus aucun fantôme n'a peur, on remet l'ambiance normale
            if (ghostsAreSafe) {
                sound.stop(6); // Stop Power
                sound.loop(5); // Retour de la Sirène
                isFrightenedMode = false;
            }
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
                    sound.play(2); // <-- Son "Eat Ghost"
                } else {
                    pacMan.loseLife();
                    System.out.println("Aie ! Vies restantes : " + pacMan.getLives());
                    // COUPURE SON IMMÉDIATE
                    sound.stop(5);
                    sound.stop(6);

                    if (pacMan.getLives() > 0) {
                        resetPositions();
                        sound.play(3); // <-- Son "Death" (ou un petit bruit de dégât)

                        sound.loop(5);
                        isFrightenedMode = false;

                    } else {
                        // --- C'EST ICI QUE LE GAME OVER S'ACTIVE ---
                        System.out.println("GAME OVER");
                        gameOver = true; // On active l'état de fin
                        sound.stopAll(); // On arrête tout le reste
                        sound.play(3);   // <-- Son de mort finale
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