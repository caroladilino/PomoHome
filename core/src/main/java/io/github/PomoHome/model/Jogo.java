package io.github.PomoHome.model;

import io.github.PomoHome.model.timer.ContextoTimer;

/**
 * Top-level holder for the current game session — a single instance owned by
 * {@link io.github.PomoHome.Main} and reached via {@code main.getJogo()}. In-memory
 * only; anything that must survive a restart lives in the server's database.
 */
public class Jogo {

    private Jogador jogadorLogado;
    private Loja loja;
    private Ranking ranking;
    private ContextoTimer contextoTimer;
    private CicloDiaNoite cicloDiaNoite;

    public Jogo() {
        // Ready before login so callers never NPE; jogadorLogado stays null until then.
        this.loja = new Loja();
        this.ranking = new Ranking();
        this.contextoTimer = new ContextoTimer();
        this.cicloDiaNoite = new CicloDiaNoite();
    }

    public Jogador getJogadorLogado() { return jogadorLogado; }
    public void setJogadorLogado(Jogador jogadorLogado) { this.jogadorLogado = jogadorLogado; }

    public Loja getLoja() { return loja; }
    public void setLoja(Loja loja) { this.loja = loja; }

    public Ranking getRanking() { return ranking; }
    public void setRanking(Ranking ranking) { this.ranking = ranking; }

    /** The timer state machine + observer subject (GoF State/Observer). */
    public ContextoTimer getContextoTimer() { return contextoTimer; }

    /** Convenience access to the underlying countdown model (e.g. for the ring actor). */
    public Timer getTimer() { return contextoTimer.getTimer(); }

    public CicloDiaNoite getCicloDiaNoite() { return cicloDiaNoite; }
    public void setCicloDiaNoite(CicloDiaNoite cicloDiaNoite) { this.cicloDiaNoite = cicloDiaNoite; }
}
