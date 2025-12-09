package game_items;

import javax.imageio.ImageIO;
import java.awt.*;

public class Points implements GameObject {
    private int x, y;
    private int width = 16;
    private int height = 16;
    private static Image dotImage; // Static pour économiser la mémoire

    public Points(int x, int y) {
        this.x = x;
        this.y = y;
        loadResource();
    }

    private void loadResource() {
        if (dotImage == null) {
            try {
                // Assure-toi que "dot.png" est bien dans le dossier resources
                dotImage = ImageIO.read(getClass().getResourceAsStream("/dot.png"));
            } catch (Exception e) {
                // Pas d'erreur fatale, on utilisera le carré jaune
            }
        }
    }

    @Override
    public void update() {
        // Immobile
    }

    @Override
    public void draw(Graphics g) {
        if (dotImage != null) {
            g.drawImage(dotImage, x, y, width, height, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect(x, y, width, height);
        }
    }

    // Indispensable pour la collision
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}