package game_items;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Ghost implements GameObject {

    private int x, y;
    private int startX, startY;
    private int dx, dy;
    private int speed = 2; // Un peu plus lent que PacMan (qui est à 4)
    private Color color;
    private GameMap map;

    private final int gridSize = 32;
    private Random random = new Random();

    // Directions: 0=Haut, 1=Bas, 2=Gauche, 3=Droite
    private int lastDirection = -1;

    public Ghost(GameMap map, Color color, int startX, int startY) {
        this.map = map;
        this.color = color;
        this.startX = startX;
        this.startY = startY;
        reset();
    }

    public void reset() {
        this.x = startX;
        this.y = startY;
        // Au départ, ils sortent vers le haut ou bougent aléatoirement
        this.dx = 0;
        this.dy = 0;
        this.lastDirection = -1;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public void update() {
        // Les fantômes ne changent de direction que lorsqu'ils sont pile sur une case
        if (x % gridSize == 0 && y % gridSize == 0) {
            chooseDirection();
        }

        x += dx;
        y += dy;

        // Wrap around (tunnel) comme PacMan
        if (x < 0) x = map.getWidth();
        if (x >= map.getWidth()) x = -gridSize;
    }

    private void chooseDirection() {
        ArrayList<Integer> possibleMoves = new ArrayList<>();

        // Vérifier les 4 directions
        // 0: Haut, 1: Bas, 2: Gauche, 3: Droite

        // Haut
        if (!isWallCollision(x, y - speed)) possibleMoves.add(0);
        // Bas
        if (!isWallCollision(x, y + speed)) possibleMoves.add(1);
        // Gauche
        if (!isWallCollision(x - speed, y)) possibleMoves.add(2);
        // Droite
        if (!isWallCollision(x + speed, y)) possibleMoves.add(3);

        if (possibleMoves.isEmpty()) return;

        // IA SIMPLE : Ne pas faire demi-tour sauf si cul-de-sac
        // Si on a plusieurs choix, on retire la direction opposée à celle d'où on vient
        if (possibleMoves.size() > 1 && lastDirection != -1) {
            int opposite = getOpposite(lastDirection);
            // On enlève l'opposé de la liste (Integer object removal)
            possibleMoves.remove(Integer.valueOf(opposite));
        }

        // Choisir une direction au hasard parmi celles possibles
        int choice = possibleMoves.get(random.nextInt(possibleMoves.size()));

        lastDirection = choice;

        switch (choice) {
            case 0: dx = 0; dy = -speed; break; // Haut
            case 1: dx = 0; dy = speed; break;  // Bas
            case 2: dx = -speed; dy = 0; break; // Gauche
            case 3: dx = speed; dy = 0; break;  // Droite
        }
    }

    private int getOpposite(int dir) {
        if (dir == 0) return 1; // Haut -> Bas
        if (dir == 1) return 0; // Bas -> Haut
        if (dir == 2) return 3; // Gauche -> Droite
        if (dir == 3) return 2; // Droite -> Gauche
        return -1;
    }

    private boolean isWallCollision(int nextX, int nextY) {
        // On vérifie les 4 coins du futur sprite du fantôme
        // Note : Les fantômes peuvent traverser la porte (valeur 2) dans GameMap ?
        // Par défaut GameMap.isWall retourne true pour 2.
        // Pour faire simple ici, on utilise map.isWall. Si tu veux qu'ils sortent de la cage,
        // il faudra qu'ils ne considèrent pas la porte (rose) comme un mur.
        return map.isWall(nextX, nextY) ||
                map.isWall(nextX + gridSize - 1, nextY) ||
                map.isWall(nextX, nextY + gridSize - 1) ||
                map.isWall(nextX + gridSize - 1, nextY + gridSize - 1);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        // Corps du fantôme (rond en haut, carré en bas)
        g.fillArc(x, y, gridSize, gridSize, 0, 180); // Tête
        g.fillRect(x, y + gridSize/2, gridSize, gridSize/2); // Bas du corps

        // Pieds (petits triangles ou cercles)
        int footSize = gridSize / 3;
        g.fillOval(x, y + gridSize - footSize/2, footSize, footSize);
        g.fillOval(x + footSize, y + gridSize - footSize/2, footSize, footSize);
        g.fillOval(x + footSize * 2, y + gridSize - footSize/2, footSize, footSize);

        // Yeux
        g.setColor(Color.WHITE);
        g.fillOval(x + 6, y + 8, 8, 8);
        g.fillOval(x + 18, y + 8, 8, 8);

        // Pupilles (regardent dans la direction du mouvement)
        g.setColor(Color.BLUE);
        int pupilOffsetX = (dx > 0) ? 2 : (dx < 0) ? -2 : 0;
        int pupilOffsetY = (dy > 0) ? 2 : (dy < 0) ? -2 : 0;
        g.fillOval(x + 8 + pupilOffsetX, y + 10 + pupilOffsetY, 4, 4);
        g.fillOval(x + 20 + pupilOffsetX, y + 10 + pupilOffsetY, 4, 4);
    }

    // Pour la collision
    public Rectangle getBounds() {
        // On réduit un peu la zone de collision pour être gentil avec le joueur
        return new Rectangle(x + 4, y + 4, gridSize - 8, gridSize - 8);
    }
}