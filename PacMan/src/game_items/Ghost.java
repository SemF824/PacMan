package game_items;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Ghost implements GameObject {

    private int x, y, startX, startY;
    private int dx, dy, speed;
    private Color color;
    private GameMap map;
    private PacMan pacMan;

    private final int gridSize = 32;
    private Random random = new Random();

    private boolean isInHouse = true;
    private int lastDirection = -1;
    private double aggressiveness;

    // --- MODE APEURÉ ---
    private boolean frightened = false;
    private int frightenedTimer = 0;
    private final int FRIGHTENED_DURATION = 600; // 10 secondes

    public Ghost(GameMap map, PacMan pacMan, Color color, int startX, int startY, double aggressiveness, int speed) {
        this.map = map;
        this.pacMan = pacMan;
        this.color = color;
        this.startX = startX;
        this.startY = startY;
        this.aggressiveness = aggressiveness;
        this.speed = speed;
        reset();
    }

    public void reset() {
        this.x = startX; this.y = startY;
        this.dx = 0; this.dy = 0;
        this.lastDirection = -1;
        this.isInHouse = true;
        this.frightened = false;
    }

    public void startFrightened() {
        if (isInHouse) return;
        this.frightened = true;
        this.frightenedTimer = FRIGHTENED_DURATION;
        // Demi-tour immédiat
        if (dx != 0 || dy != 0) {
            dx = -dx; dy = -dy;
            if(dx > 0) lastDirection = 3; else if(dx < 0) lastDirection = 2;
            else if(dy > 0) lastDirection = 1; else if(dy < 0) lastDirection = 0;
        }
    }

    public void die() { reset(); }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isFrightened() { return frightened; }

    @Override
    public void update() {
        if (frightened) {
            frightenedTimer--;
            if (frightenedTimer <= 0) frightened = false;
        }
        if (isInHouse) {
            handleHouseExit();
        } else {
            if (x % gridSize == 0 && y % gridSize == 0) chooseDirection();
            x += dx; y += dy;
            if (x < 0) x = map.getWidth();
            if (x >= map.getWidth()) x = -gridSize;
        }
    }

    private void handleHouseExit() {
        dy = -2; dx = 0; y += dy;
        if (y <= 7 * gridSize) { y = 7 * gridSize; isInHouse = false; }
    }

    private void chooseDirection() {
        ArrayList<Integer> possibleMoves = new ArrayList<>();
        int currentSpeed = frightened ? 1 : speed;

        if (!isWallCollision(x, y - currentSpeed)) possibleMoves.add(0);
        if (!isWallCollision(x, y + currentSpeed)) possibleMoves.add(1);
        if (!isWallCollision(x - currentSpeed, y)) possibleMoves.add(2);
        if (!isWallCollision(x + currentSpeed, y)) possibleMoves.add(3);

        if (possibleMoves.isEmpty()) return;
        if (possibleMoves.size() > 1 && lastDirection != -1) {
            possibleMoves.remove(Integer.valueOf(getOpposite(lastDirection)));
        }
        if (possibleMoves.isEmpty()) return;

        int bestMove = -1;

        // --- MODE FUITE ---
        if (frightened) {
            double panicChance = random.nextDouble();
            if (panicChance < 0.90) { // 90% Fuite intelligente
                bestMove = getFleeMove(possibleMoves, pacMan.getX(), pacMan.getY(), currentSpeed);
            } else { // 10% Panique hasard
                bestMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            }
        }
        // --- MODE NORMAL ---
        else {
            double chance = random.nextDouble();
            if (chance < aggressiveness) {
                int targetX = pacMan.getX();
                int targetY = pacMan.getY();

                // Cibles Spéciales
                if (color == Color.PINK) {
                    targetX += pacMan.getDx() * 32 * 4;
                    targetY += pacMan.getDy() * 32 * 4;
                } else if (color == Color.ORANGE) {
                    targetX -= pacMan.getDx() * 32 * 4;
                    targetY -= pacMan.getDy() * 32 * 4;
                }
                bestMove = getBestMoveToTarget(possibleMoves, targetX, targetY, currentSpeed);
            } else {
                bestMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            }
        }

        lastDirection = bestMove;
        switch (bestMove) {
            case 0: dx = 0; dy = -currentSpeed; break;
            case 1: dx = 0; dy = currentSpeed; break;
            case 2: dx = -currentSpeed; dy = 0; break;
            case 3: dx = currentSpeed; dy = 0; break;
        }
    }

    private int getBestMoveToTarget(ArrayList<Integer> moves, int targetX, int targetY, int spd) {
        int bestDir = -1; double minDistance = Double.MAX_VALUE;
        for (int move : moves) {
            int nx = x, ny = y;
            if (move == 0) ny -= spd; if (move == 1) ny += spd;
            if (move == 2) nx -= spd; if (move == 3) nx += spd;
            double dist = Math.sqrt(Math.pow(nx - targetX, 2) + Math.pow(ny - targetY, 2));
            if (dist < minDistance) { minDistance = dist; bestDir = move; }
        }
        return bestDir;
    }

    private int getFleeMove(ArrayList<Integer> moves, int targetX, int targetY, int spd) {
        int bestDir = -1; double maxDistance = -1.0;
        for (int move : moves) {
            int nx = x, ny = y;
            if (move == 0) ny -= spd; if (move == 1) ny += spd;
            if (move == 2) nx -= spd; if (move == 3) nx += spd;
            double dist = Math.sqrt(Math.pow(nx - targetX, 2) + Math.pow(ny - targetY, 2));
            if (dist > maxDistance) { maxDistance = dist; bestDir = move; }
        }
        return bestDir;
    }

    private int getOpposite(int dir) {
        if (dir == 0) return 1; if (dir == 1) return 0;
        if (dir == 2) return 3; if (dir == 3) return 2;
        return -1;
    }

    private boolean isWallCollision(int nextX, int nextY) {
        return map.isWall(nextX, nextY) || map.isWall(nextX+gridSize-1, nextY) ||
                map.isWall(nextX, nextY+gridSize-1) || map.isWall(nextX+gridSize-1, nextY+gridSize-1);
    }

    @Override
    public void draw(Graphics g) {
        if (frightened) {
            if (frightenedTimer < 120 && (frightenedTimer/10)%2 == 0) g.setColor(Color.WHITE);
            else g.setColor(new Color(50, 50, 255));
        } else {
            g.setColor(color);
        }
        g.fillArc(x, y, gridSize, gridSize, 0, 180);
        g.fillRect(x, y+gridSize/2, gridSize, gridSize/2);
        int fs = gridSize/3;
        g.fillOval(x, y+gridSize-fs/2, fs, fs);
        g.fillOval(x+fs, y+gridSize-fs/2, fs, fs);
        g.fillOval(x+fs*2, y+gridSize-fs/2, fs, fs);

        g.setColor(Color.WHITE); g.fillOval(x+6, y+8, 8, 8); g.fillOval(x+18, y+8, 8, 8);
        g.setColor(frightened ? new Color(255, 200, 200) : Color.BLUE);
        int ox = (dx > 0) ? 2 : (dx < 0) ? -2 : 0;
        int oy = (dy > 0) ? 2 : (dy < 0) ? -2 : 0;
        g.fillOval(x+8+ox, y+10+oy, 4, 4); g.fillOval(x+20+ox, y+10+oy, 4, 4);
    }

    public Rectangle getBounds() { return new Rectangle(x+4, y+4, gridSize-8, gridSize-8); }
    public String getColorName() {
        if (color == Color.RED) return "Blinky"; if (color == Color.PINK) return "Pinky";
        if (color == Color.CYAN) return "Inky"; if (color == Color.ORANGE) return "Clyde";
        return "Fantôme";
    }
}