package game_items;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PacMan implements GameObject {
    private int x, y, startX, startY;
    private int score = 0;

    // --- NOUVEAU : SYSTÈME DE VIES ---
    private int lives = 3;

    private int dx = 0, dy = 0, futureDx = 0, futureDy = 0;
    private int speed = 4;
    private int gridSize = 32;

    private BufferedImage img1 = null, img2 = null;
    private int animationCounter = 0;
    private boolean useImage1 = true;
    private int animationSpeed = 10;
    private int currentDirection = 0;
    private GameMap map;

    public PacMan(GameMap map) {
        this.map = map;
        this.startY = 15 * gridSize;
        this.startX = (map.getWidth() / 2);
        resetPosition(); // On utilise une méthode dédiée pour la position

        try {
            InputStream is1 = getClass().getResourceAsStream("/Pacman_HD.png");
            if (is1 != null) img1 = ImageIO.read(is1);
            InputStream is2 = getClass().getResourceAsStream("/Pacman2_HD.png");
            if (is2 != null) img2 = ImageIO.read(is2);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Remet PacMan au point de départ (sans toucher au score ni aux vies)
    public void resetPosition() {
        this.x = startX; this.y = startY;
        this.dx = 0; this.dy = 0;
        this.futureDx = 0; this.futureDy = 0;
        this.currentDirection = 0;
    }

    // --- GESTION DES VIES ---
    public int getLives() { return lives; }
    public void loseLife() { lives--; }
    // ------------------------

    @Override
    public void update() {
        updateSpeed();
        updatePosition();
        updateAnimation();
    }

    public int checkFood() {
        int centerX = x + gridSize / 2;
        int centerY = y + gridSize / 2;
        return map.tryEatDot(centerX, centerY);
    }

    public void addScore(int points) { this.score += points; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getScore() { return score; }

    public Rectangle getBounds() {
        return new Rectangle(x + 4, y + 4, gridSize - 8, gridSize - 8);
    }

    private void updateAnimation() {
        animationCounter++;
        if (animationCounter >= animationSpeed) {
            useImage1 = !useImage1;
            animationCounter = 0;
        }
    }

    private void updateSpeed() {
        if (x % gridSize == 0 && y % gridSize == 0) {
            int col = x / gridSize;
            int row = y / gridSize;

            int nextColFuture = col + (futureDx / speed);
            int nextRowFuture = row + (futureDy / speed);
            int wrappedFutureX = wrapIndex(nextColFuture * gridSize, map.getWidth());
            int wrappedFutureY = wrapIndex(nextRowFuture * gridSize, map.getHeight());

            if (!map.isWall(wrappedFutureX, wrappedFutureY)) {
                dx = futureDx; dy = futureDy;
            }

            int nextColCurrent = col + (dx / speed);
            int nextRowCurrent = row + (dy / speed);
            int wrappedCurrentX = wrapIndex(nextColCurrent * gridSize, map.getWidth());
            int wrappedCurrentY = wrapIndex(nextRowCurrent * gridSize, map.getHeight());

            if (map.isWall(wrappedCurrentX, wrappedCurrentY)) {
                dx = 0; dy = 0;
            }
        }
        if (dx > 0) currentDirection = 0;
        else if (dy > 0) currentDirection = 1;
        else if (dx < 0) currentDirection = 2;
        else if (dy < 0) currentDirection = 3;
    }

    private int wrapIndex(int coord, int limit) {
        int result = coord % limit;
        if (result < 0) result += limit;
        return result;
    }

    private void updatePosition(){
        x += dx; y += dy;
        int mapW = map.getWidth(); int mapH = map.getHeight();
        if (x < 0) x = x + mapW; if (x >= mapW) x = x - mapW;
        if (y < 0) y = y + mapH; if (y >= mapH) y = y - mapH;
    }

    @Override
    public void draw(Graphics g) {
        BufferedImage imgToDraw = useImage1 ? img1 : img2;
        if (imgToDraw == null) return;
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x + gridSize / 2, y + gridSize / 2);
        switch (currentDirection) {
            case 1: g2d.rotate(Math.toRadians(90)); break;
            case 2: g2d.scale(-1, 1); break;
            case 3: g2d.rotate(Math.toRadians(-90)); break;
        }
        g2d.drawImage(imgToDraw, -gridSize / 2, -gridSize / 2, gridSize, gridSize, null);
        g2d.setTransform(oldTransform);
    }

    public void keyDown(){ futureDy = speed; futureDx = 0; }
    public void keyUp(){ futureDy = -speed; futureDx = 0; }
    public void keyRight(){ futureDy = 0; futureDx = speed; }
    public void keyLeft(){ futureDy = 0; futureDx = -speed; }
}