package game_items;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GameMap implements GameObject {

    private final int gridSize = 32;
    private final int[][] levelData;

    // --- OPTIMISATION : IMAGE EN MÉMOIRE ---
    // On va dessiner les murs ici une seule fois pour ne pas recalculer à chaque image
    private BufferedImage cachedWalls;

    // --- GESTION DU RESPAWN ---
    private class RespawnTask {
        int row, col, timer;
        public RespawnTask(int row, int col) {
            this.row = row;
            this.col = col;
            this.timer = 3750; // ~60 secondes
        }
    }
    private List<RespawnTask> respawnTasks = new ArrayList<>();

    public GameMap() {
        int repeat = 2;
        // 0=dot, 1=wall, 2=gate, 3=empty, 4=superDot
        int[][] base = {
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
                {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
                {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                {1,0,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,0,1},
                {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
                {1,1,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,1,1},
                {1,1,1,1,0,1,3,3,3,3,3,3,3,1,0,1,1,1,1},
                {1,1,1,1,0,1,3,1,1,2,1,1,3,1,0,1,1,1,1},
                {0,0,0,0,0,0,0,1,3,3,3,1,0,0,0,0,0,0,0},
                {1,1,1,1,0,1,3,1,1,1,1,1,3,1,0,1,1,1,1},
                {1,1,1,1,0,1,0,3,3,3,3,3,0,1,0,1,1,1,1},
                {1,1,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,1,1},
                {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
                {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
                {1,0,0,1,0,0,0,0,0,3,0,0,0,0,0,1,0,0,1},
                {1,1,0,1,0,1,0,1,1,1,1,1,0,1,0,1,0,1,1},
                {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
                {1,0,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,0,1},
                {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };

        int rows = base.length;
        int cols = base[0].length * repeat;
        levelData = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int rep = 0; rep < repeat; rep++) {
                for (int c = 0; c < base[0].length; c++) {
                    levelData[r][rep * base[0].length + c] = base[r][c];
                }
            }
        }

        int centerCol = cols / 2;
        levelData[15][centerCol] = 0; levelData[15][centerCol - 1] = 0;
        levelData[7][centerCol] = 3; levelData[7][centerCol - 1] = 3;
        levelData[8][centerCol] = 2; levelData[8][centerCol - 1] = 2;
        levelData[9][centerCol] = 3; levelData[9][centerCol - 1] = 3;

        for (int r = 0; r < rows; r++) {
            levelData[r][0] = 1;
            levelData[r][cols - 1] = 1;
        }

        levelData[1][1] = 4;
        levelData[1][cols - 2] = 4;
        levelData[rows - 2][1] = 4;
        levelData[rows - 2][cols - 2] = 4;

        // --- ETAPE IMPORTANTE : ON PRÉPARE L'IMAGE ---
        createCache();
    }

    // Cette méthode dessine les murs UNE SEULE FOIS dans une image
    private void createCache() {
        int width = getWidth();
        int height = getHeight();
        // Création d'une image vide de la taille de la carte
        cachedWalls = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = cachedWalls.createGraphics();

        // Optimisation qualité
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // On définit les couleurs une fois pour toutes pour éviter les "new" en boucle
        Color wallColor = new Color(10, 10, 60);
        Color neonColor = new Color(50, 100, 255);
        Color glowColor = new Color(100, 150, 255, 100);
        Color doorColor = Color.PINK;
        Stroke wallStroke = new BasicStroke(2);
        Stroke doorStroke = new BasicStroke(4);

        for (int row = 0; row < levelData.length; row++) {
            for (int col = 0; col < levelData[0].length; col++) {
                int tile = levelData[row][col];
                int x = col * gridSize;
                int y = row * gridSize;

                if (tile == 1) { // MUR
                    g2d.setColor(wallColor);
                    g2d.fillRoundRect(x + 2, y + 2, gridSize - 4, gridSize - 4, 12, 12);

                    g2d.setColor(neonColor);
                    g2d.setStroke(wallStroke);
                    g2d.drawRoundRect(x + 2, y + 2, gridSize - 4, gridSize - 4, 12, 12);

                    g2d.setColor(glowColor);
                    g2d.drawRoundRect(x + 6, y + 6, gridSize - 12, gridSize - 12, 8, 8);
                }
                else if (tile == 2) { // PORTE
                    g2d.setColor(doorColor);
                    g2d.setStroke(doorStroke);
                    g2d.drawLine(x, y + gridSize/2, x + gridSize, y + gridSize/2);
                }
            }
        }
        g2d.dispose(); // Libère la mémoire graphique
    }

    @Override
    public void update() {
        for (int i = respawnTasks.size() - 1; i >= 0; i--) {
            RespawnTask task = respawnTasks.get(i);
            task.timer--;
            if (task.timer <= 0) {
                levelData[task.row][task.col] = 4;
                respawnTasks.remove(i);
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // 1. D'ABORD : On colle l'image des murs (ultra rapide)
        g2d.drawImage(cachedWalls, 0, 0, null);

        // 2. ENSUITE : On dessine SEULEMENT les points (car ils changent/disparaissent)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color dotColor = new Color(255, 183, 174);
        g2d.setColor(dotColor);

        for (int row = 0; row < levelData.length; row++) {
            for (int col = 0; col < levelData[0].length; col++) {
                int tile = levelData[row][col];
                int x = col * gridSize;
                int y = row * gridSize;

                // On ne dessine que les points ici
                if (tile == 0) { // PETIT POINT
                    g2d.fillOval(x + 13, y + 13, 6, 6);
                }
                else if (tile == 4) { // SUPER POINT
                    long time = System.currentTimeMillis() / 150;
                    int size = (time % 2 == 0) ? 18 : 14;
                    int offset = (32 - size) / 2;
                    g2d.fillOval(x + offset, y + offset, size, size);
                }
            }
        }
    }

    // 0=rien, 1=point, 2=super
    public int tryEatDot(int x, int y) {
        int col = x / gridSize;
        int row = y / gridSize;
        if (col < 0 || col >= levelData[0].length || row < 0 || row >= levelData.length) return 0;

        int tile = levelData[row][col];
        if (tile == 0) {
            levelData[row][col] = 3;
            return 1;
        } else if (tile == 4) {
            levelData[row][col] = 3;
            respawnTasks.add(new RespawnTask(row, col));
            return 2;
        }
        return 0;
    }

    public boolean isWall(int x, int y) {
        int col = x / gridSize;
        int row = y / gridSize;
        if (col < 0 || col >= levelData[0].length || row < 0 || row >= levelData.length) return false;
        return levelData[row][col] == 1;
    }

    public int getGridSize() { return gridSize; }
    public int getWidth() { return levelData[0].length * gridSize; }
    public int getHeight() { return levelData.length * gridSize; }
}