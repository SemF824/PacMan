package game_items;

import java.util.Arrays;
import java.util.Collections;

public class MazeGen {

    private final int width;
    private final int height;
    private final int[][] maze;
    private final boolean[][] visited;

    public MazeGen(int width, int height) {
        this.width = width;
        this.height = height;
        this.maze = new int[width][height];
        this.visited = new boolean[width][height];
    }

    public int[][] generate() {
        // Initialize maze with walls
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                maze[i][j] = 1; // 1 represents a wall
            }
        }

        // Start generation from a random cell
        int startX = (int) (Math.random() * width);
        int startY = (int) (Math.random() * height);

        generateRecursive(startX, startY);

        return maze;
    }

    private void generateRecursive(int x, int y) {
        visited[x][y] = true;
        maze[x][y] = 0; // 0 represents a path

        // Get neighbors in random order
        Integer[] directions = new Integer[]{0, 1, 2, 3}; // 0:N, 1:S, 2:E, 3:W
        Collections.shuffle(Arrays.asList(directions));

        for (int dir : directions) {
            int newX = x;
            int newY = y;
            int wallX = x;
            int wallY = y;

            switch (dir) {
                case 0: // North
                    newY = y - 2;
                    wallY = y - 1;
                    break;
                case 1: // South
                    newY = y + 2;
                    wallY = y + 1;
                    break;
                case 2: // East
                    newX = x + 2;
                    wallX = x + 1;
                    break;
                case 3: // West
                    newX = x - 2;
                    wallX = x - 1;
                    break;
            }

            if (newX >= 0 && newX < width && newY >= 0 && newY < height && !visited[newX][newY]) {
                maze[wallX][wallY] = 0; // Carve a path
                generateRecursive(newX, newY);
            }
        }
    }
}
