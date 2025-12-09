package game_items;

import java.awt.*;
import java.util.ArrayList; // Nécessaire pour la liste d'attente
import java.util.List;

public class GameMap implements GameObject {

    private final int gridSize = 32;
    private final int[][] levelData;

    // --- GESTION DU RESPAWN ---
    // Une petite classe interne pour mémoriser quel point doit réapparaître et quand
    private class RespawnTask {
        int row;
        int col;
        int timer;

        public RespawnTask(int row, int col) {
            this.row = row;
            this.col = col;
            // Le jeu tourne à ~60 images/seconde (16ms par tick)
            // 60 secondes * 62.5 ticks = 3750 frames environ
            this.timer = 3750;
        }
    }

    // La liste des points en attente de réapparition
    private List<RespawnTask> respawnTasks = new ArrayList<>();
    // ---------------------------

    public GameMap() {
        int repeat = 2;

        // 0=dot, 1=wall, 2=gate, 3=empty
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

        // Murs spéciaux et spawn
        int centerCol = cols / 2;
        levelData[15][centerCol] = 0;
        levelData[15][centerCol - 1] = 0;
        levelData[7][centerCol] = 3;
        levelData[7][centerCol - 1] = 3;
        levelData[8][centerCol] = 2;
        levelData[8][centerCol - 1] = 2;
        levelData[9][centerCol] = 3;
        levelData[9][centerCol - 1] = 3;

        for (int r = 0; r < rows; r++) {
            levelData[r][0] = 1;
            levelData[r][cols - 1] = 1;
        }

        // Super Pac-Gommes aux coins
        levelData[1][1] = 4;
        levelData[1][cols - 2] = 4;
        levelData[rows - 2][1] = 4;
        levelData[rows - 2][cols - 2] = 4;
    }

    @Override
    public void update() {
        // --- MISE A JOUR DU RESPAWN ---
        // On parcourt la liste à l'envers pour pouvoir supprimer des éléments sans bug
        for (int i = respawnTasks.size() - 1; i >= 0; i--) {
            RespawnTask task = respawnTasks.get(i);

            // On diminue le temps
            task.timer--;

            // Si le temps est écoulé (1 minute passée)
            if (task.timer <= 0) {
                // On fait réapparaître le super point sur la carte
                levelData[task.row][task.col] = 4;

                // On retire la tâche de la liste
                respawnTasks.remove(i);

                System.out.println("Super Point a respawn !");
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));

        for (int row = 0; row < levelData.length; row++) {
            for (int col = 0; col < levelData[0].length; col++) {
                int tile = levelData[row][col];
                int x = col * gridSize;
                int y = row * gridSize;

                if (tile == 1) { // MUR
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, gridSize, gridSize);
                    g.setColor(new Color(33, 33, 255));
                    g.drawRect(x + 5, y + 5, gridSize - 10, gridSize - 10);
                }
                else if (tile == 2) { // PORTE
                    g.setColor(Color.PINK);
                    g.drawLine(x, y + gridSize/2, x + gridSize, y + gridSize/2);
                }
                else if (tile == 0) { // PETIT POINT
                    g.setColor(new Color(255, 183, 174));
                    g.fillRect(x + 14, y + 14, 4, 4);
                }
                else if (tile == 4) { // SUPER POINT
                    g.setColor(new Color(255, 183, 174));
                    // Animation simple : ça grossit et rapetisse un peu selon le temps
                    // (Utilisation du temps système pour faire battre le point)
                    long time = System.currentTimeMillis() / 200;
                    int size = (time % 2 == 0) ? 16 : 14;
                    int offset = (32 - size) / 2;

                    g.fillOval(x + offset, y + offset, size, size);
                }
            }
        }
    }

    // Renvoie 0 si rien, 1 si point normal, 2 si super point
    public int tryEatDot(int x, int y) {
        int col = x / gridSize;
        int row = y / gridSize;
        if (col < 0 || col >= levelData[0].length || row < 0 || row >= levelData.length) {
            return 0;
        }

        int tile = levelData[row][col];

        if (tile == 0) { // Petit point
            levelData[row][col] = 3;
            return 1;
        }
        else if (tile == 4) { // Super point
            levelData[row][col] = 3; // On l'enlève de la carte

            // --- ON LANCE LE CHRONO POUR LE RESPAWN ---
            respawnTasks.add(new RespawnTask(row, col));

            return 2;
        }
        return 0;
    }

    public boolean isWall(int x, int y) {
        int col = x / gridSize;
        int row = y / gridSize;
        if (col < 0 || col >= levelData[0].length || row < 0 || row >= levelData.length) {
            return false;
        }
        return levelData[row][col] == 1;
    }

    public int getGridSize() { return gridSize; }
    public int getWidth() { return levelData[0].length * gridSize; }
    public int getHeight() { return levelData.length * gridSize; }
}