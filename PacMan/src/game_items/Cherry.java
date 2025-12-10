package game_items;

import java.awt.*;

public class Cherry implements GameObject {

    private int x, y;
    private boolean isVisible = false;

    // Timer pour le spawn
    private int timer = 0;
    private int spawnTime;

    // Références nécessaires
    private GameMap map;
    private PacMan pacMan;

    private final int gridSize = 32;

    public Cherry(GameMap map, PacMan pacMan) {
        this.map = map;
        this.pacMan = pacMan;
        resetTimer();
    }

    // Calcule un temps aléatoire entre 10s et 30s
    private void resetTimer() {
        isVisible = false;
        timer = 0;
        // Le jeu tourne à environ 60 images par seconde (16ms)
        // 600 frames = 10 sec, 1800 frames = 30 sec
        int minFrames = 600;
        int maxFrames = 1800;
        spawnTime = minFrames + (int)(Math.random() * (maxFrames - minFrames));
    }

    // Trouve une position aléatoire qui n'est pas un mur
    private void spawn() {
        int cols = map.getWidth() / gridSize;
        int rows = map.getHeight() / gridSize;

        boolean found = false;
        while (!found) {
            int randCol = (int)(Math.random() * cols);
            int randRow = (int)(Math.random() * rows);

            int pixelX = randCol * gridSize;
            int pixelY = randRow * gridSize;

            // On vérifie que ce n'est pas un mur
            if (!map.isWall(pixelX, pixelY)) {
                this.x = pixelX;
                this.y = pixelY;
                found = true;
                isVisible = true;
            }
        }
    }

    // Méthode publique appelée par le GamePanel quand le son est joué
    public void eat() {
        pacMan.addScore(100); // +100 points
        resetTimer();         // On cache la cerise et on relance le chrono
    }

    @Override
    public void update() {
        // Si la cerise est cachée, on fait avancer le chrono
        if (!isVisible) {
            timer++;
            if (timer >= spawnTime) {
                spawn();
            }
        }
        // Si elle est visible, on vérifie si PacMan la mange
        else {
            checkCollision();
        }
    }

    private void checkCollision() {
        Rectangle pacManBounds = pacMan.getBounds();
        Rectangle cherryBounds = new Rectangle(x, y, gridSize, gridSize);

        if (pacManBounds.intersects(cherryBounds)) {
            // Note : Le GamePanel gère le son, ici on gère le score et le reset
            eat();
        }
    }

    // --- GETTERS (Indispensables pour que GamePanel sache quand jouer le son) ---
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isVisible() { return isVisible; }

    @Override
    public void draw(Graphics g) {
        if (isVisible) {
            Graphics2D g2d = (Graphics2D) g;

            // Activation de l'antialiasing pour que la cerise soit jolie (pas pixellisée)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = x + gridSize / 2;
            int centerY = y + gridSize / 2;

            // Les fruits (2 ronds rouges)
            g2d.setColor(Color.RED);
            g2d.fillOval(centerX - 10, centerY, 10, 10);
            g2d.fillOval(centerX + 2, centerY - 2, 10, 10);

            // La queue (ligne verte)
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(centerX - 5, centerY, centerX, centerY - 10);
            g2d.drawLine(centerX + 7, centerY - 2, centerX, centerY - 10);
        }
    }
}