package gui;

import Case.Case;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe qui gère l'affichage de la carte, des robots et des cailloux
 */
public class CaillouxGui extends JPanel {
    private final int longueur;
    private final int hauteur;
    private final int tailleCellule = 40;
    private final Case[][] grille;
    // Calque de fond
    private BufferedImage mapLayer;
    // Liste des cases recouvertes par le vaisseau
    private final List<Point> casesVaisseau = new ArrayList<>();

    // Liste des agents (explorateurs, ramasseurs, chargeurs, superviseur)
    private final List<Point> explorateurs = new ArrayList<>();
    private final List<Point> ramasseurs = new ArrayList<>();
    private final List<Point> superChargeurs = new ArrayList<>();
    private Point superviseur;

    // Images
    private BufferedImage imageVaisseau;

    /**
     * Crée une nouvelle carte de la taille spécifiée
     * @param longueur Largeur de la carte
     * @param hauteur Hauteur de la carte
     */
    public CaillouxGui(int longueur, int hauteur) {
        this.longueur = longueur;
        this.hauteur = hauteur;
        this.grille = new Case[longueur][hauteur];
        initialiseGrille();
        initialiseMapLayer();

        setPreferredSize(new Dimension(longueur * tailleCellule, hauteur * tailleCellule)); // Ajuste la taille de la carte

        JFrame frame = new JFrame("Carte des Robots et des Cailloux");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(this); // Ajout du JPanel au JFrame
        frame.pack(); // Ajuste la taille de la fenêtre automatiquement
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Timer timer = new Timer(200, e -> {
            repaint(); // Rafraîchit l'affichage toutes les 200 ms
        });
        timer.start();

        // Chargement des images
        try {
            imageVaisseau = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Images/vaisseau.png")));
            // Redimensionne l'image du vaisseau
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialise la grille de la carte avec des cases vides et des cases avec cailloux
     */
    private void initialiseGrille() {
        casesVaisseau.add(new Point(longueur / 2, hauteur / 2));
        casesVaisseau.add(new Point(longueur / 2 - 1, hauteur / 2));
        casesVaisseau.add(new Point(longueur / 2, hauteur / 2 - 1));
        casesVaisseau.add(new Point(longueur / 2 - 1, hauteur / 2 - 1));

        for (int i = 0; i < longueur; i++) {
            for (int j = 0; j < hauteur; j++) {
                // Vérifie si la case est recouverte par le vaisseau
                if (casesVaisseau.contains(new Point(i, j))) {
                    grille[i][j] = new Case(true, 0, false); // Case vide
                // Choisit au hasard si la case contient des cailloux
                } else {
                    int nbCailloux = (int) (Math.random() * 9) + 1;
                    grille[i][j] = Math.random() < 0.3 ? new Case(false, nbCailloux, false) : new Case(false, 0, false);
                }
            }
        }
        // Positionne le superviseur au centre de la carte
        superviseur = new Point(longueur / 2, hauteur / 2);
        // Positionne les explorateurs au centre de la carte
        for (int i = 0; i < 3; i++) {
            explorateurs.add(new Point(longueur / 2, (hauteur / 2) - 1));
        }
        // Positionne les ramasseurs au centre de la carte
        for (int i = 0; i < 5; i++) {
            ramasseurs.add(new Point((longueur / 2) - 1, (hauteur / 2) - 1));
        }
        // Positionne les chargeurs au centre de la carte
        for (int i = 0; i < 2; i++) {
            superChargeurs.add(new Point((longueur / 2) - 1, (hauteur / 2)));
        }
    }

    /**
     * Initialise le calque de la carte
     */
    private void initialiseMapLayer() {
        mapLayer = new BufferedImage(longueur * tailleCellule, hauteur * tailleCellule, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mapLayer.createGraphics();
        g2d.dispose();  // Libère les ressources graphiques
    }

    /**
     * Dessine les éléments de la carte
     * @param g the <code>Graphics</code>
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(mapLayer, 0, 0, null);

        // Dessine les cases de la grille
        for (int i = 0; i < longueur; i++) {
            for (int j = 0; j < hauteur; j++) {
                int x = i * tailleCellule;
                int y = j * tailleCellule;

                if (grille[i][j].getNbCailloux() > 0) {
                    g2d.setColor(Color.GRAY);  // Case avec un caillou
                } else if (grille[i][j].isCaseVaisseau()) {
                    g2d.setColor(Color.BLACK); // Case recouverte par le vaisseau
                } else {
                    g2d.setColor(Color.WHITE); // Case vide
                }
                g2d.fillRect(x, y, tailleCellule, tailleCellule);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, tailleCellule, tailleCellule);
            }
        }

        // Dessine le vaisseau au milieu en le redimensionnant
        int centreX = (longueur / 2 - 1) * tailleCellule;
        int centreY = (hauteur / 2 - 1) * tailleCellule;
        BufferedImage vaisseau = new BufferedImage(2* tailleCellule, 2* tailleCellule, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dVaisseau = vaisseau.createGraphics();
        g2dVaisseau.drawImage(imageVaisseau, 0, 0, 2* tailleCellule, 2* tailleCellule, null);
        g2dVaisseau.dispose();
        g2d.drawImage(vaisseau, centreX, centreY, null);

        // Dessine le superviseur
        dessinerAgent(g2d, superviseur, Color.PINK, "S");

        // Dessine les explorateurs
        for(int i = 0; i < explorateurs.size(); i++){
            dessinerAgent(g2d, explorateurs.get(i), Color.GREEN, "E" + i);
        }
        // Dessine les ramasseurs
        for(int i = 0; i< ramasseurs.size(); i++){
            dessinerAgent(g2d, ramasseurs.get(i), Color.BLUE, "R" + i);
        }
        // Dessine les chargeurs
        for(int i = 0; i< superChargeurs.size(); i++){
            dessinerAgent(g2d, superChargeurs.get(i), Color.ORANGE, "C" + i);
        }
    }

    /**
     * Méthode permettant de dessiner un agent sur la carte
     * @param g2d Graphics2D
     * @param agent Point de l'agent
     * @param couleur donnée
     * @param nom de l'agent
     */
    private void dessinerAgent(Graphics2D g2d, Point agent, Color couleur, String nom) {
        g2d.setColor(couleur);
        int x = agent.x * tailleCellule + tailleCellule / 4;
        int y = agent.y * tailleCellule + tailleCellule / 4;
        g2d.fillOval(x, y, tailleCellule / 2, tailleCellule / 2);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (tailleCellule / 2 - fm.stringWidth(nom)) / 2;
        int textY = y + (tailleCellule / 2 + fm.getAscent()) / 2;
        g2d.drawString(nom, textX, textY);
    }

    /**
     * Déplace les explorateurs sur la carte
     * @param newX Nouvelle position en x.
     * @param newY Nouvelle position en y.
     */
    public void deplaceExplorateur(int explorerIndex, int newX, int newY) {
        explorateurs.get(explorerIndex).move(newX, newY);
    }

    /**
     * Déplace les ramasseurs sur la carte
     * @param newX Nouvelle position en x.
     * @param newY Nouvelle position en y.
     */
    public void deplaceRamasseur(int ramasseurIndex, int newX, int newY) {
        ramasseurs.get(ramasseurIndex).move(newX, newY);
    }

    /**
     * Déplace les chargeurs sur la carte
     * @param newX Nouvelle position en x.
     * @param newY Nouvelle position en y.
     */
    public void deplaceChargeur(int chargeurIndex, int newX, int newY) {
        superChargeurs.get(chargeurIndex).move(newX, newY);
    }

    /**
     * Retourne la longueur de la carte
     */
    public int getLongueur() {
        return longueur;
    }

    /**
     * Retourne la hauteur de la carte
     */
    public int getHauteur() {
        return hauteur;
    }

    /**
     * Retourne la taille d'une case
     */
    public int getTailleCellule() {
        return tailleCellule;
    }

    /**
     * Retourne la grille des cases
     */
    public Case getGrille(int x, int y) {
        return grille[x][y];
    }

    /**
     * Retourne les cases recouvertes par le vaisseau
     */
    public List<Point> getCasesVaisseau() {
        return casesVaisseau;
    }

}
