package game_items;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PacMan implements GameObject {
    // --- POSITION DE DÉPART PRÉCISE ---
    // Correspond au "3" isolé au milieu bas de ma nouvelle carte (colonne 9, ligne 15)
    private int x = 9 * 32;
    private int y = 15 * 32;

    private int dx = 0;
    private int dy = 0;
    private int futureDx = 0;
    private int futureDy = 0;

    private int speed = 4; // DOIT être un diviseur de 32 (4*8 = 32) sinon bugs garantis
    private int gridSize = 32;

    // ... (Tes variables d'images ne changent pas) ...
    private BufferedImage img1 = null;
    private BufferedImage img2 = null;
    private int animationCounter = 0;
    private boolean useImage1 = true;
    private int animationSpeed = 10;
    private int currentDirection = 0;
    private GameMap map;

    public PacMan(GameMap map) {
        this.map = map;
        // ... (Chargement des images comme avant) ...
        try {
            InputStream is1 = getClass().getResourceAsStream("/Pacman_HD.png");
            if (is1 != null) img1 = ImageIO.read(is1);
            InputStream is2 = getClass().getResourceAsStream("/Pacman2_HD.png");
            if (is2 != null) img2 = ImageIO.read(is2);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void update() {
        updateSpeed();
        updatePosition();
        updateAnimation();
    }

    // ... (updateAnimation ne change pas) ...
    private void updateAnimation() {
        animationCounter++;
        if (animationCounter >= animationSpeed) {
            useImage1 = !useImage1;
            animationCounter = 0;
        }
    }

    // --- C'EST ICI QUE TU DOIS MODIFIER POUR LES MURS ---
    private void updateSpeed() {
        // On ne décide QUE si on est parfaitement aligné sur la grille
        if (x % gridSize == 0 && y % gridSize == 0) {

            // 1. Calculer la case actuelle
            int col = x / gridSize;
            int row = y / gridSize;

            // 2. Vérifier si on peut tourner vers la "Future Direction"
            // On regarde la case voisine dans la direction souhaitée
            int nextColFuture = col + (futureDx / speed); // +1, -1 ou 0
            int nextRowFuture = row + (futureDy / speed);

            if (!map.isWall(nextColFuture * gridSize, nextRowFuture * gridSize)) {
                // Voie libre pour tourner !
                dx = futureDx;
                dy = futureDy;
            }

            // 3. Vérifier si on va percuter un mur dans la "Direction Actuelle"
            int nextColCurrent = col + (dx / speed);
            int nextRowCurrent = row + (dy / speed);

            if (map.isWall(nextColCurrent * gridSize, nextRowCurrent * gridSize)) {
                // Mur devant ! STOP IMMÉDIAT
                dx = 0;
                dy = 0;
            }
        }

        // Mise à jour de l'image (direction)
        if (dx > 0) currentDirection = 0;
        else if (dy > 0) currentDirection = 1;
        else if (dx < 0) currentDirection = 2;
        else if (dy < 0) currentDirection = 3;
    }

    private void updatePosition(){
        x += dx;
        y += dy;

        // CORRECTION TUNNEL (Optionnel mais cool)
        // Si PacMan sort à gauche, il revient à droite
        if (x < -gridSize) x = map.getWidth();
        if (x > map.getWidth()) x = -gridSize;
    }

    // ... (Le draw et les KeyListeners restent identiques) ...
    @Override
    public void draw(Graphics g) {
        BufferedImage imgToDraw = useImage1 ? img1 : img2;
        if (imgToDraw == null) return;
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x + gridSize / 2, y + gridSize / 2);
        switch (currentDirection) {
            case 0: break;
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