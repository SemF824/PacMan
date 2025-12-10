package game_items;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList; // Nécessaire pour le GPS
import java.util.Queue;      // Nécessaire pour le GPS
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

    private boolean frightened = false;
    private int frightenedTimer = 0;
    private final int FRIGHTENED_DURATION = 600;

    private boolean dead = false;
    private int returnSpeed = 4;
    private int doorY;

    public Ghost(GameMap map, PacMan pacMan, Color color, int startX, int startY, double aggressiveness, int speed) {
        this.map = map;
        this.pacMan = pacMan;
        this.color = color;
        this.startX = startX;
        this.startY = startY;
        this.doorY = 7 * gridSize;
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
        this.dead = false;
    }

    public void startFrightened() {
        if (isInHouse || dead) return;
        this.frightened = true;
        this.frightenedTimer = FRIGHTENED_DURATION;
        if (dx != 0 || dy != 0) {
            dx = -dx; dy = -dy;
            if(dx > 0) lastDirection = 3; else if(dx < 0) lastDirection = 2;
            else if(dy > 0) lastDirection = 1; else if(dy < 0) lastDirection = 0;
        }
    }

    public void die() {
        this.frightened = false;
        this.dead = true;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isFrightened() { return frightened; }
    public boolean isDead() { return dead; }

    @Override
    public void update() {
        // --- LOGIQUE RETOUR MAISON (YEUX) ---
        if (dead) {
            // Alignement avec la porte
            if (y == doorY && Math.abs(x - startX) < returnSpeed) {
                x = startX;
                y += returnSpeed;
                return;
            }
            // Descente finale dans la maison
            if (y > doorY && y < startY && x == startX) {
                y += returnSpeed;
                if (y >= startY) {
                    y = startY;
                    dead = false; isInHouse = true; frightened = false;
                    dx = 0; dy = 0;
                }
                return;
            }
        }

        if (frightened && !dead) {
            frightenedTimer--;
            if (frightenedTimer <= 0) frightened = false;
        }

        if (isInHouse) {
            handleHouseExit();
            return;
        }

        if (x % gridSize == 0 && y % gridSize == 0) {
            chooseDirection();
        }

        x += dx;
        y += dy;

        if (x < 0) x = map.getWidth();
        if (x >= map.getWidth()) x = -gridSize;
    }

    private void handleHouseExit() {
        dy = -2; dx = 0; y += dy;
        if (y <= doorY) { y = doorY; isInHouse = false; }
    }

    private void chooseDirection() {
        ArrayList<Integer> possibleMoves = new ArrayList<>();
        int moveSpeed = speed;
        if (dead) moveSpeed = returnSpeed;
        else if (frightened) moveSpeed = 1;

        if (!isWallCollision(x, y - moveSpeed)) possibleMoves.add(0);
        if (!isWallCollision(x, y + moveSpeed)) possibleMoves.add(1);
        if (!isWallCollision(x - moveSpeed, y)) possibleMoves.add(2);
        if (!isWallCollision(x + moveSpeed, y)) possibleMoves.add(3);

        if (possibleMoves.isEmpty()) return;

        if (!dead && possibleMoves.size() > 1 && lastDirection != -1) {
            possibleMoves.remove(Integer.valueOf(getOpposite(lastDirection)));
        }
        if (possibleMoves.isEmpty()) { // Sécurité
            if (!isWallCollision(x, y - moveSpeed)) possibleMoves.add(0);
            if (!isWallCollision(x, y + moveSpeed)) possibleMoves.add(1);
            if (!isWallCollision(x - moveSpeed, y)) possibleMoves.add(2);
            if (!isWallCollision(x + moveSpeed, y)) possibleMoves.add(3);
        }

        int bestMove = -1;

        // --- CAS 1 : MORT (GPS INTELLIGENT) ---
        if (dead) {
            // On utilise un vrai algorithme de chemin (BFS) au lieu de la distance simple
            // On vise la porte (startX, doorY)
            bestMove = getSmartMove(startX, doorY);
        }

        // --- CAS 2 : APEURÉ ---
        else if (frightened) {
            double panicChance = random.nextDouble();
            if (panicChance < 0.90) bestMove = getFleeMove(possibleMoves, pacMan.getX(), pacMan.getY(), moveSpeed);
            else bestMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
        }

        // --- CAS 3 : NORMAL ---
        else {
            double chance = random.nextDouble();
            if (chance < aggressiveness) {
                int targetX = pacMan.getX();
                int targetY = pacMan.getY();
                if (color == Color.PINK) {
                    targetX += pacMan.getDx() * 32 * 4; targetY += pacMan.getDy() * 32 * 4;
                } else if (color == Color.ORANGE) {
                    targetX -= pacMan.getDx() * 32 * 4; targetY -= pacMan.getDy() * 32 * 4;
                }
                bestMove = getBestMoveToTarget(possibleMoves, targetX, targetY, moveSpeed);
            } else {
                bestMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            }
        }

        lastDirection = bestMove;
        switch (bestMove) {
            case 0: dx = 0; dy = -moveSpeed; break;
            case 1: dx = 0; dy = moveSpeed; break;
            case 2: dx = -moveSpeed; dy = 0; break;
            case 3: dx = moveSpeed; dy = 0; break;
        }
    }

    // --- ALGORITHME GPS (BFS) ---
    // Trouve le chemin réel contournant les murs
    private int getSmartMove(int targetPixelX, int targetPixelY) {
        int startGridX = x / gridSize;
        int startGridY = y / gridSize;
        int targetGridX = targetPixelX / gridSize;
        int targetGridY = targetPixelY / gridSize;

        // Si on est déjà dessus
        if (startGridX == targetGridX && startGridY == targetGridY) return -1;

        // Structure pour explorer : {x, y, premièreDirectionPrise}
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[map.getWidth()/gridSize][map.getHeight()/gridSize];

        // Ajouter les voisins initiaux
        // 0:Haut, 1:Bas, 2:Gauche, 3:Droite
        int[] dX = {0, 0, -1, 1};
        int[] dY = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) {
            int nextX = startGridX + dX[i];
            int nextY = startGridY + dY[i];

            // Si c'est un mur ou hors map, on ignore
            if (nextX < 0 || nextX >= visited.length || nextY < 0 || nextY >= visited[0].length) continue;
            if (map.isWall(nextX * gridSize, nextY * gridSize)) continue;

            // On ajoute à la file d'attente
            queue.add(new int[]{nextX, nextY, i});
            visited[nextX][nextY] = true;
        }

        // Exploration
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];
            int firstDir = current[2];

            // A-t-on trouvé la cible ?
            if (cx == targetGridX && cy == targetGridY) {
                return firstDir; // On renvoie la direction qu'il fallait prendre au début
            }

            // Sinon on continue d'explorer les voisins
            for (int i = 0; i < 4; i++) {
                int nx = cx + dX[i];
                int ny = cy + dY[i];

                if (nx >= 0 && nx < visited.length && ny >= 0 && ny < visited[0].length && !visited[nx][ny]) {
                    if (!map.isWall(nx * gridSize, ny * gridSize)) {
                        visited[nx][ny] = true;
                        // On garde la 'firstDir' d'origine pour savoir par où commencer
                        queue.add(new int[]{nx, ny, firstDir});
                    }
                }
            }
        }
        return -1; // Pas de chemin trouvé (ne devrait pas arriver)
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
        if (!dead) {
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
        }

        g.setColor(Color.WHITE); g.fillOval(x+6, y+8, 8, 8); g.fillOval(x+18, y+8, 8, 8);
        g.setColor(frightened && !dead ? new Color(255, 200, 200) : Color.BLUE);
        int ox = 0, oy = 0;
        int lookDx = dx; int lookDy = dy;

        if (dead && lookDx == 0 && lookDy == 0) lookDy = -1;
        if (lookDx > 0) ox = 2; else if (lookDx < 0) ox = -2;
        if (lookDy > 0) oy = 2; else if (lookDy < 0) oy = -2;

        g.fillOval(x+8+ox, y+10+oy, 4, 4); g.fillOval(x+20+ox, y+10+oy, 4, 4);
    }

    public Rectangle getBounds() { return new Rectangle(x+4, y+4, gridSize-8, gridSize-8); }
    public String getColorName() {
        if (color == Color.RED) return "Blinky"; if (color == Color.PINK) return "Pinky";
        if (color == Color.CYAN) return "Inky"; if (color == Color.ORANGE) return "Clyde";
        return "Fantôme";
    }
}