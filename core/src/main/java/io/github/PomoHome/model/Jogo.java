package io.github.PomoHome.model;

/**
 * Top-level holder for the current game session.
 *
 * Why a class and not a bunch of static fields? Because we want a SINGLE
 * instance that any Screen can reach. Two common patterns:
 *
 *  (a) Pass it in the constructor of every Screen
 *       new TelaLoja(jogo);  -> screen stores 'jogo' as a field
 *
 *  (b) Singleton-ish access through Main:
 *       Main.getJogo();
 *
 * TODO (TEAM): pick (a) OR (b) and stay consistent. (a) is easier to test;
 * (b) is shorter to write.
 *
 * TODO (TEAM): nothing here is persistent — log out / app close means this
 * object is gone. Anything that must survive a restart lives in the H2
 * database on the server.
 */
public class Jogo {

    private Jogador jogadorLogado;
    private Loja loja;
    private Ranking ranking;
    private Timer timer;

    public Jogo() {
        // TODO: initialize the simple ones so callers don't NPE before
        //       the first server call:
        //   this.loja    = new Loja();
        //   this.ranking = new Ranking();
        //   this.timer   = new Timer();
        // 'jogadorLogado' stays null until login completes.
    }

    public Jogador getJogadorLogado() { return jogadorLogado; }
    public void setJogadorLogado(Jogador jogadorLogado) { this.jogadorLogado = jogadorLogado; }

    public Loja getLoja() { return loja; }
    public void setLoja(Loja loja) { this.loja = loja; }

    public Ranking getRanking() { return ranking; }
    public void setRanking(Ranking ranking) { this.ranking = ranking; }

    public Timer getTimer() { return timer; }
    public void setTimer(Timer timer) { this.timer = timer; }
}
