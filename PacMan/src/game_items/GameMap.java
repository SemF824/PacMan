package game_items;

import java.awt.*;

public class GameMap implements GameObject {

    // Size of each grid cell in pixels
    private final int gridSize = 32;

    // levelData holds the full map (rows x cols). We build it by repeating a base pattern.
    private final int[][] levelData;

    public GameMap() {
        // repeat factor: how many times the base pattern is concatenated horizontally
        int repeat = 2;

        // base pattern: the original map layout (0=dot,1=wall,2=gate,3=empty)
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

        // Opening center wall for spawn
        int centerCol = cols / 2;
        levelData[15][centerCol] = 0;
        levelData[15][centerCol - 1] = 0;

        for (int r = 0; r < rows; r++) {
            levelData[r][0] = 1;
            levelData[r][cols - 1] = 1;
        }
    }

    @Override
    public void update() {}

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

                if (tile == 1) { // WALL
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, gridSize, gridSize);
                    g.setColor(new Color(33, 33, 255));
                    g.drawRect(x + 5, y + 5, gridSize - 10, gridSize - 10);
                }
                else if (tile == 2) { // GATE
                    g.setColor(Color.PINK);
                    g.drawLine(x, y + gridSize/2, x + gridSize, y + gridSize/2);
                }
                else if (tile == 0) { // DOT
                    g.setColor(new Color(255, 183, 174));
                    // Dessiner un petit carré centré
                    g.fillRect(x + 14, y + 14, 4, 4);
                }
            }
        }
    }

    // --- NOUVELLE MÉTHODE POUR MANGER LES POINTS ---
    // Vérifie si la position x,y correspond à un point.
    // Si oui, le point disparaît (devient 3) et on renvoie true.
    public boolean tryEatDot(int x, int y) {
        int col = x / gridSize;
        int row = y / gridSize;

        // Vérification des limites pour éviter les erreurs
        if (col < 0 || col >= levelData[0].length || row < 0 || row >= levelData.length) {
            return false;
        }

        // Si c'est un point (0)
        if (levelData[row][col] == 0) {
            levelData[row][col] = 3; // On le remplace par 3 (vide)
            return true; // Miam !
        }
        return false;
    }
    // ------------------------------------------------

    public boolean isWall(int x, int y) {
        int col = x / gridSize;
        int row = y / gridSize;
        if (col < 0 || col >= levelData[0].length || row < 0 || row >= levelData.length) {
            return false;
        }
        return levelData[row][col] == 1 || levelData[row][col] == 2;
    }

    public int getGridSize() { return gridSize; }
    public int getWidth() { return levelData[0].length * gridSize; }
    public int getHeight() { return levelData.length * gridSize; }
}