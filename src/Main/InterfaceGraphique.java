package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class InterfaceGraphique {

    // ✅ Variable globale volatile
    public static volatile boolean enCours = false;

    private static TamponPartage tampon;
    private static JLabel tailleTamponLabel;
    private static JLabel totalProduitsLabel;
    private static JLabel totalConsommesLabel;
    private static JTextArea articlesProduitsTextArea;
    private static JTextArea articlesConsommesTextArea;
    private static JTextArea rapportTextArea;

    // ✅ Listes pour statistiques
    private static ArrayList<Producteur>   listeProducteurs   = new ArrayList<>();
    private static ArrayList<Consommateur> listeConsommateurs = new ArrayList<>();
    private static ArrayList<Thread>       listeThreads       = new ArrayList<>();

    // ✅ Compteurs pour détecter la fin automatique
    private static int nbProducteurs         = 0;
    private static int producteursTermines   = 0;
    private static int nbConsommateurs       = 0;
    private static int consommateursTermines = 0;

    // Boutons
    private static JButton demarrerButton;
    private static JButton arreterButton;
    private static JButton reinitialiserButton;
    private static JButton enregistrerButton;

    private static JFrame frame;

    public InterfaceGraphique() {
        frame = new JFrame("Producteur-Consommateur");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 800);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // ── Champs de saisie ──
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nombre de producteurs:"), gbc);
        JTextField producteursInput = new JTextField("2");
        gbc.gridx = 1; panel.add(producteursInput, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nombre de consommateurs:"), gbc);
        JTextField consommateursInput = new JTextField("2");
        gbc.gridx = 1; panel.add(consommateursInput, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Taille du tampon:"), gbc);
        JTextField tailleTamponInput = new JTextField("5");
        gbc.gridx = 1; panel.add(tailleTamponInput, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Nombre d'articles par producteur:"), gbc);
        JTextField articlesAProduireInput = new JTextField("10");
        gbc.gridx = 1; panel.add(articlesAProduireInput, gbc);

        // ── Statistiques en temps réel ──
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Taille actuelle du tampon:"), gbc);
        tailleTamponLabel = new JLabel("0");
        tailleTamponLabel.setForeground(Color.BLUE);
        gbc.gridx = 1; panel.add(tailleTamponLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Total articles produits:"), gbc);
        totalProduitsLabel = new JLabel("0");
        totalProduitsLabel.setForeground(new Color(0, 150, 0));
        gbc.gridx = 1; panel.add(totalProduitsLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Total articles consommes:"), gbc);
        totalConsommesLabel = new JLabel("0");
        totalConsommesLabel.setForeground(Color.RED);
        gbc.gridx = 1; panel.add(totalConsommesLabel, gbc);

        // ── Journal produits ──
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Journal des produits:"), gbc);
        articlesProduitsTextArea = new JTextArea(8, 30);
        articlesProduitsTextArea.setEditable(false);
        articlesProduitsTextArea.setFont(
                new Font("Monospaced", Font.PLAIN, 11));
        gbc.gridx = 1;
        panel.add(new JScrollPane(articlesProduitsTextArea), gbc);

        // ── Journal consommations ──
        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(new JLabel("Journal des consommations:"), gbc);
        articlesConsommesTextArea = new JTextArea(8, 30);
        articlesConsommesTextArea.setEditable(false);
        articlesConsommesTextArea.setFont(
                new Font("Monospaced", Font.PLAIN, 11));
        gbc.gridx = 1;
        panel.add(new JScrollPane(articlesConsommesTextArea), gbc);

        // ── Rapport final ──
        gbc.gridx = 0; gbc.gridy = 9;
        panel.add(new JLabel("Rapport final:"), gbc);
        rapportTextArea = new JTextArea(6, 30);
        rapportTextArea.setEditable(false);
        rapportTextArea.setFont(
                new Font("Monospaced", Font.PLAIN, 11));
        gbc.gridx = 1;
        panel.add(new JScrollPane(rapportTextArea), gbc);

        // ── Boutons ──
        JPanel panneauBoutons = new JPanel(new FlowLayout());
        demarrerButton      = new JButton("Démarrer");
        arreterButton       = new JButton("Arrêter");
        reinitialiserButton = new JButton("Réinitialiser");
        enregistrerButton   = new JButton("Enregistrer rapport");

        demarrerButton.setBackground(new Color(0, 150, 0));
        demarrerButton.setForeground(Color.WHITE);
        arreterButton.setBackground(Color.RED);
        arreterButton.setForeground(Color.WHITE);
        enregistrerButton.setBackground(Color.BLUE);
        enregistrerButton.setForeground(Color.WHITE);

        arreterButton.setEnabled(false);
        reinitialiserButton.setEnabled(false);
        enregistrerButton.setEnabled(false);

        panneauBoutons.add(demarrerButton);
        panneauBoutons.add(arreterButton);
        panneauBoutons.add(reinitialiserButton);
        panneauBoutons.add(enregistrerButton);

        gbc.gridx = 0; gbc.gridy = 10;
        gbc.gridwidth = 2;
        panel.add(panneauBoutons, gbc);

        // ── Action Démarrer ──
        demarrerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int nbProd   = Integer.parseInt(
                            producteursInput.getText());
                    int nbCons   = Integer.parseInt(
                            consommateursInput.getText());
                    int taille   = Integer.parseInt(
                            tailleTamponInput.getText());
                    int articles = Integer.parseInt(
                            articlesAProduireInput.getText());

                    if (nbProd <= 0 || nbCons <= 0
                            || taille <= 0 || articles <= 0) {
                        JOptionPane.showMessageDialog(frame,
                                "Toutes les valeurs doivent être positives !");
                        return;
                    }

                    // ✅ Initialisation
                    tampon = new TamponPartage(taille);
                    enCours = true;
                    listeProducteurs.clear();
                    listeConsommateurs.clear();
                    listeThreads.clear();

                    // ✅ Initialiser compteurs
                    nbProducteurs         = nbProd;
                    nbConsommateurs       = nbCons;
                    producteursTermines   = 0;
                    consommateursTermines = 0;

                    // ✅ Créer producteurs
                    for (int i = 1; i <= nbProd; i++) {
                        Producteur p = new Producteur(tampon, i, articles);
                        listeProducteurs.add(p);
                        Thread t = new Thread(p);
                        listeThreads.add(t);
                        t.start();
                    }

                    // ✅ Créer consommateurs
                    for (int i = 1; i <= nbCons; i++) {
                        Consommateur c = new Consommateur(tampon, i);
                        listeConsommateurs.add(c);
                        Thread t = new Thread(c);
                        listeThreads.add(t);
                        t.start();
                    }

                    demarrerButton.setEnabled(false);
                    arreterButton.setEnabled(true);
                    reinitialiserButton.setEnabled(false);
                    enregistrerButton.setEnabled(false);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Veuillez entrer des nombres entiers valides !");
                }
            }
        });

        // ── Action Arrêter ──
        arreterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                arreter();
            }
        });

        // ── Action Réinitialiser ──
        reinitialiserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reinitialiser();
            }
        });

        // ── Action Enregistrer ──
        enregistrerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enregistrerRapport();
            }
        });

        frame.getContentPane().add(new JScrollPane(panel));
        frame.setVisible(true);
    }

    // ✅ Arrêter manuellement
    private static void arreter() {
        enCours = false;
        for (Thread t : listeThreads) {
            t.interrupt();
        }
        demarrerButton.setEnabled(false);
        arreterButton.setEnabled(false);
        reinitialiserButton.setEnabled(true);
        enregistrerButton.setEnabled(true);
        afficherRapport();
    }

    // ✅ Appelé quand un producteur termine
    public static synchronized void producteurTermine() {
        producteursTermines++;
    }

    // ✅ Vérifie si tous les producteurs ont terminé
    public static synchronized boolean tousProducteursTermines() {
        return producteursTermines >= nbProducteurs;
    }

    // ✅ Appelé quand un consommateur termine
    public static synchronized void consommateurTermine() {
        consommateursTermines++;
        // Si tous les consommateurs ont fini → rapport automatique
        if (consommateursTermines >= nbConsommateurs) {
            enCours = false;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    afficherRapport();
                    demarrerButton.setEnabled(false);
                    arreterButton.setEnabled(false);
                    reinitialiserButton.setEnabled(true);
                    enregistrerButton.setEnabled(true);
                }
            });
        }
    }

    // ✅ Rapport final
    private static void afficherRapport() {
        if (tampon == null) return;
        String heure = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss"));
        StringBuilder rapport = new StringBuilder();
        rapport.append("================================\n");
        rapport.append("  RAPPORT FINAL — " + heure + "\n");
        rapport.append("================================\n");
        rapport.append("Total produit  : "
                + tampon.getTotalDepose() + "\n");
        rapport.append("Total consommé : "
                + tampon.getTotalRetire() + "\n");
        rapport.append("Restant tampon : "
                + tampon.getCount() + "\n");
        rapport.append("--------------------------------\n");
        for (Producteur p : listeProducteurs) {
            rapport.append("Producteur  " + p.getIdentifiant()
                    + " → " + p.getTotalProduit() + " articles\n");
        }
        for (Consommateur c : listeConsommateurs) {
            rapport.append("Consommateur " + c.getIdentifiant()
                    + " → " + c.getTotalConsomme() + " articles\n");
        }
        rapport.append("================================\n");
        rapportTextArea.setText(rapport.toString());
    }

    // ✅ Enregistrer rapport dans fichier .txt
    private static void enregistrerRapport() {
        String heure = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH-mm-ss"));
        String nomFichier = "rapport_" + heure + ".txt";
        try (FileWriter fw = new FileWriter(nomFichier)) {
            fw.write("=== JOURNAL DES PRODUCTIONS ===\n");
            fw.write(articlesProduitsTextArea.getText());
            fw.write("\n=== JOURNAL DES CONSOMMATIONS ===\n");
            fw.write(articlesConsommesTextArea.getText());
            fw.write("\n=== RAPPORT FINAL ===\n");
            fw.write(rapportTextArea.getText());
            JOptionPane.showMessageDialog(frame,
                    "Rapport enregistré : " + nomFichier + " ✅");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame,
                    "Erreur lors de l'enregistrement !");
        }
    }

    // ✅ Réinitialiser
    private static void reinitialiser() {
        enCours = false;
        tampon  = null;
        listeProducteurs.clear();
        listeConsommateurs.clear();
        listeThreads.clear();
        producteursTermines   = 0;
        consommateursTermines = 0;

        articlesProduitsTextArea.setText("");
        articlesConsommesTextArea.setText("");
        rapportTextArea.setText("");
        tailleTamponLabel.setText("0");
        totalProduitsLabel.setText("0");
        totalConsommesLabel.setText("0");

        demarrerButton.setEnabled(true);
        arreterButton.setEnabled(false);
        reinitialiserButton.setEnabled(false);
        enregistrerButton.setEnabled(false);
    }

    // ✅ Mise à jour taille tampon + statistiques
    public static void mettreAJourTailleTampon() {
        if (tampon != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tailleTamponLabel.setText(
                            String.valueOf(tampon.getCount()));
                    totalProduitsLabel.setText(
                            String.valueOf(tampon.getTotalDepose()));
                    totalConsommesLabel.setText(
                            String.valueOf(tampon.getTotalRetire()));
                }
            });
        }
    }

    // ✅ Ajouter article produit
    public static void ajouterArticleProduit(String article,
                                              int idProducteur) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                articlesProduitsTextArea.append(article + "\n");
            }
        });
    }

    // ✅ Ajouter article consommé
    public static void ajouterArticleConsomme(String article,
                                               int idConsommateur) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                articlesConsommesTextArea.append(article + "\n");
            }
        });
    }
}