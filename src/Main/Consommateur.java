package Main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Consommateur implements Runnable {

    // Référence vers le tampon partagé
    private final TamponPartage tamponPartage;

    // Identifiant du consommateur (1, 2, 3...)
    private final int identifiant;

    // Statistiques
    private int totalConsomme = 0;

    // Format de l'horodatage 
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // Constructeur
    public Consommateur(TamponPartage tamponPartage, int identifiant) {
        this.tamponPartage = tamponPartage;
        this.identifiant   = identifiant;
        journaliser("INIT", "Consommateur " + identifiant + " créé");
    }

    // La mission du thread consommateur
    @Override
    public void run() {
        journaliser("INFO", "Consommateur " + identifiant + " démarré");
        while (Main.enCours) {
            try {
                // Retirer un article du tampon (bloque si tampon vide)
                Object article = tamponPartage.retirer();
                totalConsomme++;
                journaliser("CONS", "Consommé : " + article
                        + " | Total consommé : " + totalConsomme);

                // Simuler le temps de traitement
                Thread.sleep((int)(Math.random() * 1000));

            } catch (InterruptedException e) {
                // Sortir proprement si le thread est interrompu
                Thread.currentThread().interrupt();
                break;
            }
        }
        // Rapport final quand le thread s'arrête
        journaliser("STOP", "Consommateur " + identifiant
                + " arrêté — Total consommé : " + totalConsomme);
    }

    // Journalisation — même méthode que TamponPartage
    private void journaliser(String niveau, String message) {
        String heure = LocalDateTime.now().format(FORMAT);
        System.out.println("[" + heure + "] [" + niveau + "] " + message);
    }

    // Getters pour les statistiques
    public int getTotalConsomme() { return totalConsomme; }
    public int getIdentifiant()   { return identifiant; }
}