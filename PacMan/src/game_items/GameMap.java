package game_items;

import java.awt.*;

public class GameMap implements GameObject {

    // Size of each grid cell in pixels
    private final int gridSize = 32;

    // levelData holds the full map (rows x cols). We build it by repeating a base pattern.
    private final int[][] levelData;

    public GameMap() {
        // repeat factor: how many times the base pattern is concatenated horizontally
        int repeat = 2; // increase to make the map wider

        // base pattern: the original map layout (0=dot,1=wall,2=gate,3=empty)
        int[][] base = {
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
                {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
                {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
                {1,0,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,0,1},
                {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
                {1,1,1,1,0,1,1,1,3,1,3,1,1,1,0,1,1,1,1},
                {1,1,1,1,0,1,3,3,3,3,3,3,3,1,0,1,1,1,1},
                {1,1,1,1,0,1,3,1,1,2,1,1,3,1,0,1,1,1,1},
                {3,3,3,3,0,3,3,1,3,3,3,1,3,3,0,3,3,3,3},
                {1,1,1,1,0,1,3,1,1,1,1,1,3,1,0,1,1,1,1},
                {1,1,1,1,0,1,3,3,3,3,3,3,3,1,0,1,1,1,1},
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

        // Fill levelData by copying the base pattern repeat times horizontally
        for (int r = 0; r < rows; r++) {
            for (int rep = 0; rep < repeat; rep++) {
                for (int c = 0; c < base[0].length; c++) {
                    levelData[r][rep * base[0].length + c] = base[r][c];
                }
            }
        }

        // Ensure boundaries: leftmost and rightmost columns are walls so the map edges remain solid
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
        // Enable anti-aliasing for nicer lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2)); // Slightly thicker strokes for walls

        // Draw each tile according to its value: wall, gate, dot
        for (int row = 0; row < levelData.length; row++) {
            for (int col = 0; col < levelData[0].length; col++) {
                int tile = levelData[row][col];
                int x = col * gridSize;
                int y = row * gridSize;

                if (tile == 1) { // WALL
                    // Fill black then draw a small neon-blue rectangle to represent the wall
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, gridSize, gridSize);

                    g.setColor(new Color(33, 33, 255));
                    g.drawRect(x + 5, y + 5, gridSize - 10, gridSize - 10);
                }
                else if (tile == 2) { // GATE (ghost house door)
                    g.setColor(Color.PINK);
                    g.drawLine(x, y + gridSize/2, x + gridSize, y + gridSize/2);
                }
                else if (tile == 0) { // DOT (pellet)
                    g.setColor(new Color(255, 183, 174));
                    g.fillRect(x + 14, y + 14, 4, 4);
                }
            }
        }
    }

    // isWall: returns true if the pixel coordinate corresponds to a wall or gate.
    // Note: if the coordinate is outside the map bounds, we return false so wrap-around can work.
    public boolean isWall(int x, int y) {
        int col = x / gridSize;
        int row = y / gridSize;

        // Out-of-bounds -> not a wall (allows wrap)
        if (col < 0 || col >= levelData[0].length || row < 0 || row >= levelData.length) {
            return false;
        }

        return levelData[row][col] == 1 || levelData[row][col] == 2;
    }

    // Helper getters used by other classes
    public int getGridSize() { return gridSize; }
    public int getWidth() { return levelData[0].length * gridSize; }
    public int getHeight() { return levelData.length * gridSize; }
}