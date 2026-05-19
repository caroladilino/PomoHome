package io.github.PomoHome;

public class Jogo {
    public Jogador jogadorLogado;
    public Loja loja;
    public Ranking ranking;
    public Timer timer;
    public Historico historico;

    // Construtor principal que monta o estado inicial do jogo
    public Jogo(Jogador jogador) {
        this.jogadorLogado = jogador;
        this.loja = new Loja();
        this.ranking = new Ranking();
        this.historico = new Historico();
        
        // Regra antiga mantida: 53 minutos * 60 segundos
        this.timer = new Timer(53 * 60, historico); 
        
        inicializarCatalogoLoja();
    }

    private void inicializarCatalogoLoja() {
        // Criamos os móveis (Dados)
        Movel m1 = new Movel("Cama", "Cama Casal", 250, 2, 3, "cama_casal_asset");
        Movel m2 = new Movel("Mesa", "Mesa Grande", 200, 2, 2, "mesa_asset");
        Movel m3 = new Movel("Cadeira", "Cadeira Gamer", 80, 1, 1, "cadeira_asset");
        
        // Colocamos eles à venda na loja como ItemLoja (Disponível = true)
        // Dentro de Jogo.java, verifique se está assim:
        loja.itens.add(new ItemLoja(m1, true)); // Correto: Movel m1 primeiro, boolean true depois
        loja.itens.add(new ItemLoja(m2, true));
        loja.itens.add(new ItemLoja(m3, true));
            }

    // Método principal de atualização do jogo
    public void atualizarLogica(float delta) {
        // A regra de ganhar moedas estava na Main, agora fica aqui no núcleo do Jogo!
        boolean pomodoroAcabou = timer.atualizar(delta);
        
        if (pomodoroAcabou) {
            // Regra antiga mantida: 1 moeda por minuto configurado
            int moedasGanhas = timer.tempoCiclo / 60;
            jogadorLogado.adicionarSaldo(moedasGanhas);
            jogadorLogado.tempoEstudado += timer.tempoCiclo;
        }
    }
}