package io.github.PomoHome.model;

/**
 * Top-level holder for the current game session — a single instance owned by
 * {@link io.github.PomoHome.Main} and reached via {@code main.getJogo()}. In-memory
 * only; anything that must survive a restart lives in the server's database.
 */
public class Jogo {

    private Jogador jogadorLogado;
    private Loja loja;
    private Ranking ranking;
    private Timer timer;
    private CicloDiaNoite cicloDiaNoite;

    public Jogo() {
        // Ready before login so callers never NPE; jogadorLogado stays null until then.
        this.loja = new Loja();
        this.ranking = new Ranking();
        this.timer = new Timer();
        this.cicloDiaNoite = new CicloDiaNoite();
    }

    public Jogador getJogadorLogado() { return jogadorLogado; }
    public void setJogadorLogado(Jogador jogadorLogado) { this.jogadorLogado = jogadorLogado; }

    public Loja getLoja() { return loja; }
    public void setLoja(Loja loja) { this.loja = loja; }

    public Ranking getRanking() { return ranking; }
    public void setRanking(Ranking ranking) { this.ranking = ranking; }

    public Timer getTimer() { return timer; }
    public void setTimer(Timer timer) { this.timer = timer; }

    public CicloDiaNoite getCicloDiaNoite() { return cicloDiaNoite; }
    public void setCicloDiaNoite(CicloDiaNoite cicloDiaNoite) { this.cicloDiaNoite = cicloDiaNoite; }
}
