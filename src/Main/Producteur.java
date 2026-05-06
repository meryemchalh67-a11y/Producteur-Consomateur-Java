package Main;

public class Producteur implements Runnable {
    private final TamponPartage tamponPartage;
    private final int identifiant;
    private final int articlesAProduire; // Nombre d'articles à produire

    public Producteur(TamponPartage tamponPartage, int identifiant, int articlesAProduire) {
        this.tamponPartage = tamponPartage;
        this.identifiant = identifiant;
        this.articlesAProduire = articlesAProduire;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < articlesAProduire && estEnCours(); i++) { // Utilisation de estEnCours()
                int article = identifiant * 100 + i;
                tamponPartage.produire(article);
                Thread.sleep((int) (Math.random() * 10000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean estEnCours() {
        return Main.enCours; // Utilise la méthode statique de Main pour vérifier si le programme est en cours d'exécution
    }
}
