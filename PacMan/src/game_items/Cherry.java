package game_items;

import java.awt.*;

public class Cherry implements GameObject {

    private int x, y;
    private boolean isVisible = false;

    // Timer pour le spawn
    private int timer = 0;
    private int spawnTime; // Le moment où elle va apparaître (en frames)

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
        // 10 secondes = 600 frames, 30 secondes = 1800 frames
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

    @Override
    public void update() {
        if (!isVisible) {
            timer++;
            if (timer >= spawnTime) {
                spawn();
            }
        } else {
            checkCollision();
        }
    }

    private void checkCollision() {
        // On récupère la position de PacMan
        int px = pacMan.getX();
        int py = pacMan.getY();

        // Calcul simple de distance (si les centres sont proches)
        // On considère mangé si la distance est inférieure à la taille d'une case
        double distance = Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2));

        if (distance < gridSize) {
            // MIAM !
            pacMan.addScore(100); // +100 points
            resetTimer(); // On relance le compteur pour la prochaine cerise
        }
    }

    @Override
    public void draw(Graphics g) {
        if (isVisible) {
            // --- CORRECTION ICI ---
            // On convertit 'g' en 'Graphics2D' pour avoir accès aux options avancées
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
            // Maintenant setStroke fonctionne car on utilise g2d
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(centerX - 5, centerY, centerX, centerY - 10);
            g2d.drawLine(centerX + 7, centerY - 2, centerX, centerY - 10);
        }
    }
}