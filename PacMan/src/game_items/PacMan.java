package game_items;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PacMan implements GameObject {

    // --- POSITION ET MOUVEMENT ---
    private int x = 32;
    private int y = 32;
    private int dx = 0;
    private int dy = 0;

    // Mouvement "futur" (Buffer d'entrée)
    private int futureDx = 0;
    private int futureDy = 0;

    private int speed = 4;
    private int gridSize = 32;

    // --- IMAGES ET ANIMATION ---
    private BufferedImage img1 = null; // Image 1 (Pacman_HD.png)
    private BufferedImage img2 = null; // Image 2 (Pacman2_HD.png)

    // Compteur pour l'animation
    private int animationCounter = 0;
    private boolean useImage1 = true;

    // Vitesse de l'animation
    // 15 updates ≈ 0.25 seconde.
    // Tu peux réduire ce chiffre (ex: 10 ou 15) pour que ça clignote plus vite.
    private int animationSpeed = 15;

    // Direction actuelle pour la rotation de l'image
    // 0: Droite, 1: Bas, 2: Gauche, 3: Haut
    private int currentDirection = 0;

    /**
     * Constructeur
     */
    public PacMan(){
        try {
            // CHARGEMENT DE L'IMAGE 1 : Pacman_HD.png
            InputStream is1 = getClass().getResourceAsStream("/Pacman_HD.png");
            if (is1 != null) img1 = ImageIO.read(is1);

            // CHARGEMENT DE L'IMAGE 2 : Pacman2_HD.png
            InputStream is2 = getClass().getResourceAsStream("/Pacman2_HD.png");
            if (is2 != null) img2 = ImageIO.read(is2);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur de chargement des images PacMan");
        }
    }

    /**
     * Mise à jour logique (appelée ~60 fois par seconde)
     */
    @Override
    public void update() {
        updateSpeed();      // Gère la direction sur la grille
        updatePosition();   // Applique le déplacement
        updateAnimation();  // Gère le changement d'image
    }

    private void updateAnimation() {
        animationCounter++;

        // Si le compteur dépasse la vitesse définie
        if (animationCounter >= animationSpeed) {
            useImage1 = !useImage1; // On bascule entre image 1 et image 2
            animationCounter = 0;   // On remet le compteur à 0
        }
    }

    private void updateSpeed(){
        // On ne change de direction que si on est pile sur une case
        if (isOnGrid()){
            dx = futureDx;
            dy = futureDy;

            // Détection de la direction pour l'orientation de l'image
            if (dx > 0) currentDirection = 0;      // Droite
            else if (dy > 0) currentDirection = 1; // Bas
            else if (dx < 0) currentDirection = 2; // Gauche
            else if (dy < 0) currentDirection = 3; // Haut
        }
    }

    private void updatePosition(){
        x += dx;
        y += dy;
    }

    /**
     * Affichage graphique avec rotations
     */
    @Override
    public void draw(Graphics g) {
        // Choisir quelle image dessiner selon l'animation
        BufferedImage imgToDraw = useImage1 ? img1 : img2;

        // Sécurité si les images n'ont pas chargé
        if (imgToDraw == null) {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, gridSize, gridSize);
            return;
        }

        // Conversion en Graphics2D pour les transformations
        Graphics2D g2d = (Graphics2D) g;

        // Sauvegarde de la position originale du "crayon"
        AffineTransform oldTransform = g2d.getTransform();

        // 1. Déplacer le point de pivot au CENTRE de PacMan
        g2d.translate(x + gridSize / 2, y + gridSize / 2);

        // 2. Appliquer la rotation ou le miroir selon la direction
        switch (currentDirection) {
            case 0: // Droite (Par défaut)
                break;
            case 1: // Bas
                g2d.rotate(Math.toRadians(90));
                break;
            case 2: // Gauche (Effet Miroir)
                g2d.scale(-1, 1);
                break;
            case 3: // Haut
                g2d.rotate(Math.toRadians(-90));
                break;
        }

        // 3. Dessiner l'image centrée sur le point de pivot
        // (On recule de la moitié de la taille)
        g2d.drawImage(imgToDraw, -gridSize / 2, -gridSize / 2, gridSize, gridSize, null);

        // 4. Restaurer la position originale du "crayon" pour les prochains objets
        g2d.setTransform(oldTransform);
    }

    // --- CONTROLES CLAVIER ---

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

    // --- UTILITAIRES ---

    private boolean isOnGrid(){
        return x % gridSize == 0 && y % gridSize == 0;
    }
}