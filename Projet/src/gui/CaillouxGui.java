package gui;

import Case.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CaillouxGui extends JFrame {
    private final int width;
    private final int height;
    private final int cellSize = 50;
    private final Case[][] grid;
    private final List<Point> explorers = new ArrayList<>();
    private Point supervisor;
    private BufferedImage mapLayer;  // Calque de fond

    public CaillouxGui(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Case[width][height];
        initializeGrid();
        initializeMapLayer();

        setTitle("Carte des Robots et des Cailloux");
        setSize(width * cellSize, height * cellSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();  // Rafraîchit l'affichage toutes les 100 ms
            }
        });
        timer.start();
    }

    private void initializeGrid() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = Math.random() < 0.3 ? new Pierre() : new Case(true, false, false);
            }
        }
        supervisor = new Point(width - 1, height - 1);
        explorers.add(new Point(0, 0));
    }

    private void initializeMapLayer() {
        mapLayer = new BufferedImage(width * cellSize, height * cellSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mapLayer.createGraphics();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int x = i * cellSize;
                int y = j * cellSize;

                if (grid[i][j].isCaillou() == true) {
                    g2d.setColor(Color.GRAY);  // Case avec un caillou
                } else {
                    g2d.setColor(Color.WHITE); // Case vide
                }
                g2d.fillRect(x, y, cellSize, cellSize);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, cellSize, cellSize);
            }
        }
        g2d.dispose();  // Libère les ressources graphiques
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(mapLayer, 0, 0, null);


        g2d.setColor(Color.PINK);
        int supX = supervisor.x * cellSize + cellSize / 4;
        int supY = supervisor.y * cellSize + cellSize / 4;
        g2d.fillOval(supX, supY, cellSize / 2, cellSize / 2);

        g2d.setColor(Color.GREEN);
        for (Point explorer : explorers) {
            int expX = explorer.x * cellSize + cellSize / 4;
            int expY = explorer.y * cellSize + cellSize / 4;
            g2d.fillOval(expX, expY, cellSize / 2, cellSize / 2);
        }

    }

    public void moveExplorer(int explorerIndex, int newX, int newY) {
        if (explorerIndex < explorers.size()) {
            explorers.get(explorerIndex).move(newX, newY);
        }
    }

    public void moveSupervisor(int newX, int newY) {
        supervisor.move(newX, newY);
        repaint();
    }

    public int getCellSize() {
        return cellSize;
    }

    public Case getGridElement(int x, int y) {
        return grid[x][y];
    }


}
