package game_items;

import java.awt.*;

public class GameMap implements GameObject {

    private final int gridSize = 32;

    // 0 = Vide (Chemin avec gomme)
    // 1 = Mur (Bleu)
    // 2 = Porte Fantôme (Ligne blanche)
    // 3 = Vide (Sans gomme, ex: départ Pacman)
    private final int[][] levelData = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,0,1},
            {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
            {1,1,1,1,0,1,1,1,3,1,3,1,1,1,0,1,1,1,1}, // Passage vers le centre
            {1,1,1,1,0,1,3,3,3,3,3,3,3,1,0,1,1,1,1},
            {1,1,1,1,0,1,3,1,1,2,1,1,3,1,0,1,1,1,1}, // Maison fantômes (le 2)
            {3,3,3,3,0,3,3,1,3,3,3,1,3,3,0,3,3,3,3}, // Tunnel (gauche/droite)
            {1,1,1,1,0,1,3,1,1,1,1,1,3,1,0,1,1,1,1},
            {1,1,1,1,0,1,3,3,3,3,3,3,3,1,0,1,1,1,1},
            {1,1,1,1,0,1,0,1,1,1,1,1,0,1,0,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1},
            {1,0,1,1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
            {1,0,0,1,0,0,0,0,0,3,0,0,0,0,0,1,0,0,1}, // Le "3" au milieu est le départ PacMan
            {1,1,0,1,0,1,0,1,1,1,1,1,0,1,0,1,0,1,1},
            {1,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,1},
            {1,0,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    @Override
    public void update() {}

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        // Permet d'avoir des traits plus jolis (lissage)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2)); // Trait un peu plus épais

        for (int row = 0; row < levelData.length; row++) {
            for (int col = 0; col < levelData[0].length; col++) {
                int tile = levelData[row][col];
                int x = col * gridSize;
                int y = row * gridSize;

                if (tile == 1) { // MUR
                    // 1. On remplit en noir (pour cacher ce qu'il y a dessous)
                    g.setColor(Color.BLACK);
                    g.fillRect(x, y, gridSize, gridSize);

                    // 2. On dessine le contour Bleu "Néon"
                    g.setColor(new Color(33, 33, 255)); // Bleu Pacman classique
                    g.drawRect(x + 5, y + 5, gridSize - 10, gridSize - 10); // Petit carré intérieur
                    // (On ne dessine pas le grand carré extérieur pour donner l'effet "chemin")
                }
                else if (tile == 2) { // PORTE
                    g.setColor(Color.PINK);
                    g.drawLine(x, y + gridSize/2, x + gridSize, y + gridSize/2);
                }
                else if (tile == 0) { // GOMME
                    g.setColor(new Color(255, 183, 174)); // Couleur saumon
                    // Petit point centré
                    g.fillRect(x + 14, y + 14, 4, 4);
                }
            }
        }
    }

    // --- CORRECTION CRITIQUE DES COLLISIONS ---
    public boolean isWall(int x, int y) {
        // On divise par 32 pour avoir l'index de la case
        int col = x / gridSize;
        int row = y / gridSize;

        // Sécurité pour ne pas planter si on sort de l'écran
        if (col < 0 || col >= levelData[0].length || row < 0 || row >= levelData.length) {
            return true;
        }

        // Le 1 est un mur, le 2 est la porte (mur pour PacMan)
        return levelData[row][col] == 1 || levelData[row][col] == 2;
    }

    public int getGridSize() { return gridSize; }
    public int getWidth() { return levelData[0].length * gridSize; }
    public int getHeight() { return levelData.length * gridSize; }
}