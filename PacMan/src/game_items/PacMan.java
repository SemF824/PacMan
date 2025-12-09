package game_items;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PacMan implements GameObject {
    // --- STARTING POSITION (pixel coordinates) ---
    // This correlates to the '3' cell in the base map (col 9, row 15)
    private int x = 9 * 32;
    private int y = 15 * 32;

    // Current velocity in pixels per update (dx, dy), and the future requested direction
    private int dx = 0;
    private int dy = 0;
    private int futureDx = 0;
    private int futureDy = 0;

    // Movement parameters: speed must divide the grid size cleanly to stay aligned
    private int speed = 4; // pixels per tick
    private int gridSize = 32;

    // Animation images and counters
    private BufferedImage img1 = null;
    private BufferedImage img2 = null;
    private int animationCounter = 0;
    private boolean useImage1 = true;
    private int animationSpeed = 10;
    private int currentDirection = 0; // 0=right,1=down,2=left,3=up
    private GameMap map; // reference to the map for collision checks

    public PacMan(GameMap map) {
        this.map = map;
        // Load two PacMan frames from resources (if available)
        try {
            InputStream is1 = getClass().getResourceAsStream("/Pacman_HD.png");
            if (is1 != null) img1 = ImageIO.read(is1);
            InputStream is2 = getClass().getResourceAsStream("/Pacman2_HD.png");
            if (is2 != null) img2 = ImageIO.read(is2);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void update() {
        // 1) Decide speed/direction based on map collisions and requested future direction
        updateSpeed();
        // 2) Apply movement and wrap-around
        updatePosition();
        // 3) Update animation frame
        updateAnimation();
    }

    // Simple two-frame animation toggle
    private void updateAnimation() {
        animationCounter++;
        if (animationCounter >= animationSpeed) {
            useImage1 = !useImage1;
            animationCounter = 0;
        }
    }

    // Movement/collision decision logic
    private void updateSpeed() {
        // Only change directions when perfectly aligned on the grid to avoid clipping through walls
        if (x % gridSize == 0 && y % gridSize == 0) {

            // Current tile indexes
            int col = x / gridSize;
            int row = y / gridSize;

            // If the player requested a new direction (futureDx/futureDy), check if that neighbor cell is free.
            int nextColFuture = col + (futureDx / speed); // will be -1/0/1
            int nextRowFuture = row + (futureDy / speed);

            // Use wrapped coordinates when querying the map so tunnel wrap works.
            int wrappedFutureX = wrapIndex(nextColFuture * gridSize, map.getWidth());
            int wrappedFutureY = wrapIndex(nextRowFuture * gridSize, map.getHeight());

            if (!map.isWall(wrappedFutureX, wrappedFutureY)) {
                // Accept the queued direction
                dx = futureDx;
                dy = futureDy;
            }

            // Check forward collision in current direction
            int nextColCurrent = col + (dx / speed);
            int nextRowCurrent = row + (dy / speed);

            int wrappedCurrentX = wrapIndex(nextColCurrent * gridSize, map.getWidth());
            int wrappedCurrentY = wrapIndex(nextRowCurrent * gridSize, map.getHeight());

            if (map.isWall(wrappedCurrentX, wrappedCurrentY)) {
                // Wall ahead: stop movement
                dx = 0;
                dy = 0;
            }
        }

        // Update facing direction for rendering
        if (dx > 0) currentDirection = 0;
        else if (dy > 0) currentDirection = 1;
        else if (dx < 0) currentDirection = 2;
        else if (dy < 0) currentDirection = 3;
    }

    // Wrap a pixel coordinate into the [0, limit-1] range
    private int wrapIndex(int coord, int limit) {
        int result = coord % limit;
        if (result < 0) result += limit;
        return result;
    }

    // Apply movement and perform wrap-around on both axes
    private void updatePosition(){
        x += dx;
        y += dy;

        int mapW = map.getWidth();
        int mapH = map.getHeight();

        // Horizontal wrap
        if (x < 0) x = x + mapW;
        if (x >= mapW) x = x - mapW;

        // Vertical wrap
        if (y < 0) y = y + mapH;
        if (y >= mapH) y = y - mapH;
    }

    @Override
    public void draw(Graphics g) {
        // Draw PacMan centered in its grid cell, rotated depending on movement direction
        BufferedImage imgToDraw = useImage1 ? img1 : img2;
        if (imgToDraw == null) return; // nothing to draw if resources missing
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x + gridSize / 2, y + gridSize / 2);
        switch (currentDirection) {
            case 0: break; // facing right
            case 1: g2d.rotate(Math.toRadians(90)); break; // facing down
            case 2: g2d.scale(-1, 1); break; // facing left
            case 3: g2d.rotate(Math.toRadians(-90)); break; // facing up
        }
        g2d.drawImage(imgToDraw, -gridSize / 2, -gridSize / 2, gridSize, gridSize, null);
        g2d.setTransform(oldTransform);
    }

    // Convenience key methods that set the "future" direction to be applied once aligned with the grid
    public void keyDown(){ futureDy = speed; futureDx = 0; }
    public void keyUp(){ futureDy = -speed; futureDx = 0; }
    public void keyRight(){ futureDy = 0; futureDx = speed; }
    public void keyLeft(){ futureDy = 0; futureDx = -speed; }
}