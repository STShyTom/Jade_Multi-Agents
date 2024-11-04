package Utils;

import gui.CaillouxGui;

import java.awt.*;

public class ClasseUtils {
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void deplacement(Point position, Point destination, CaillouxGui carteGUI) {
        // Droite
        if (position.x < destination.x) {
            position.x++;
        // Gauche
        } else if (position.x > destination.x) {
            position.x--;
        // Bas
        } else if (position.y < destination.y) {
            position.y++;
        // Haut
        } else if (position.y > destination.y) {
            position.y--;
        }
    }
}
