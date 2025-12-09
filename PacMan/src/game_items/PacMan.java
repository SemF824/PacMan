package game_items;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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

    public PacMan(){
        try {
            is = getClass().getResourceAsStream("/Pac_man.png");
            img = ImageIO.read(is);
        } catch (IOException e) {
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
        }
    }

    private void updatePosition(){
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawImage(img,x,y,gridSize,gridSize,null);
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