package game_items;

import java.awt.*;

public class Wall implements GameObject {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Wall(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }

    @Override
    public void update() {
        // Les murs sont statiques, donc pas de mise à jour nécessaire.
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}