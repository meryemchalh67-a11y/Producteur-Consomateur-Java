package Main;

public class Producteur implements Runnable {

    private final TamponPartage tamponPartage;
    private final int identifiant;
    private final int articlesAProduire;
    private int dejaFait = 0;

    // ✅ Statistiques
    private int totalProduit = 0;

    public Producteur(TamponPartage tamponPartage, int identifiant,
                      int articlesAProduire) {
        this.tamponPartage     = tamponPartage;
        this.identifiant       = identifiant;
        this.articlesAProduire = articlesAProduire;
    }

    public Producteur(TamponPartage tamponPartage, int identifiant,
                      int articlesAProduire, int dejaFait) {
        this(tamponPartage, identifiant, articlesAProduire);
        this.dejaFait = dejaFait;
    }

    @Override
    public void run() {
        try {
            for (int i = dejaFait;
                 i < articlesAProduire && InterfaceGraphique.enCours; i++) {

                int article = identifiant * 100 + i;
                tamponPartage.produire(article, identifiant);
                dejaFait = i + 1;
                totalProduit++; // ✅

                // ✅ Producteur rapide (0-500ms)
                Thread.sleep((int)(Math.random() * 500));
            }
            // ✅ Signaler que ce producteur a terminé
            if (dejaFait >= articlesAProduire) {
                InterfaceGraphique.producteurTermine();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ✅ Getters
    public int getDejaFait()          { return dejaFait; }
    public int getIdentifiant()       { return identifiant; }
    public int getArticlesAProduire() { return articlesAProduire; }
    public int getTotalProduit()      { return totalProduit; }
}