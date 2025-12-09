package game_items;

import javax.imageio.ImageIO;
import java.awt.*;
// IMPORT IMPORTANT pour les transformations d'image
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PacMan implements GameObject {
    private int x = 32;
    private int y = 32;
    private int dx = 0;
    private int dy = 0;
    private int futureDx = 0;
    private int futureDy = 0;
    private int speed = 4;
    private int gridSize = 32;
    private BufferedImage img = null;
    private InputStream is;

    // NOUVEAU : Variable pour mémoriser la direction actuelle du regard
    // 0: Droite (image par défaut), 1: Bas, 2: Gauche, 3: Haut
    private int currentDirection = 0;

    public PacMan(){
        try {
            // ON SUPPOSE QUE L'IMAGE "Pac_man.png" REGARDE VERS LA DROITE PAR DÉFAUT.
            is = getClass().getResourceAsStream("/Pacman_HD.png");
            if (is != null) img = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        updateSpeed();
        updatePosition();
    }

    private void updateSpeed(){
        if (isOnGrid()){
            dx = futureDx;
            dy = futureDy;

            // NOUVEAU : Mise à jour de la direction du regard
            // On ne change la direction que si PacMan bouge
            if (dx > 0) currentDirection = 0;      // Droite
            else if (dy > 0) currentDirection = 1; // Bas
            else if (dx < 0) currentDirection = 2; // Gauche (miroir)
            else if (dy < 0) currentDirection = 3; // Haut
        }
    }

    private void updatePosition(){
        x += dx;
        y += dy;
    }

    // --- C'EST ICI QUE TOUT CHANGE ---
    public void draw(Graphics g) {
        if (img == null) return;

        // 1. Conversion en Graphics2D pour des fonctionnalités avancées
        Graphics2D g2d = (Graphics2D) g;

        // 2. Sauvegarde de l'état actuel du "crayon" graphique
        // C'est essentiel pour ne pas affecter les autres objets du jeu après PacMan.
        AffineTransform oldTransform = g2d.getTransform();

        // 3. Déplacement du point de pivot au CENTRE de PacMan
        // Par défaut, les rotations se font autour du coin haut-gauche (0,0).
        // On déplace le repère graphique au milieu de la case de PacMan.
        g2d.translate(x + gridSize / 2, y + gridSize / 2);

        // 4. Application des transformations selon la direction
        switch (currentDirection) {
            case 0: // Droite (Image par défaut)
                // Rien à faire
                break;
            case 1: // Bas
                g2d.rotate(Math.toRadians(90)); // Tourne de 90 degrés horaires
                break;
            case 2: // Gauche -> C'est l'effet "inversé" (MIROIR) que tu voulais
                // scale(-1, 1) inverse l'axe horizontal (X), créant un effet miroir.
                g2d.scale(-1, 1);
                break;
            case 3: // Haut
                g2d.rotate(Math.toRadians(-90)); // Tourne de 90 degrés anti-horaires
                break;
        }

        // 5. Dessin de l'image
        // IMPORTANT : Comme on a déplacé le point de pivot au centre de l'image (étape 3),
        // on doit dessiner l'image en reculant de la moitié de sa taille pour qu'elle soit centrée.
        // On dessine en (-16, -16) par rapport au nouveau centre.
        g2d.drawImage(img, -gridSize / 2, -gridSize / 2, gridSize, gridSize, null);

        // 6. Restauration de l'état graphique original
        g2d.setTransform(oldTransform);
    }

    public void keyDown(){
        futureDy = speed;
        futureDx = 0;
    }
    public void keyUp(){
        futureDy = -speed;
        futureDx = 0;
    }

    public void keyRight(){
        futureDy = 0;
        futureDx = speed;
    }
    public void keyLeft(){
        futureDy = 0;
        futureDx = -speed;
    }

    private boolean isOnGrid(){
        return x%gridSize == 0 && y%gridSize == 0;
    }
}