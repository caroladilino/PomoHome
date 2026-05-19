package io.github.PomoHome;

public class Timer {
    public int tempoCiclo; // Tempo total configurado (em segundos)
    public float tempoAtual; // Usamos float internamente para descontar os milissegundos precisos da engine
    public boolean rodando;

    private Historico historico;

    public Timer(int tempoCiclo, Historico historico) {
        this.tempoCiclo = tempoCiclo;
        this.tempoAtual = tempoCiclo;
        this.rodando = false;
        this.historico = historico;
    }

    // A engine vai chamar isso todo frame
    public boolean atualizar(float delta) {
        if (rodando && tempoAtual > 0) {
            tempoAtual -= delta;
            if (tempoAtual <= 0) {
                tempoAtual = 0;
                rodando = false;
                if (historico != null) {
                    historico.registrarCicloConcluido(tempoCiclo);
                }
                return true; // Retorna true para avisar que o Pomodoro acabou!
            }
        }
        return false;
    }

    public void iniciarOuPausar() {
        rodando = !rodando;
    }

    public void resetar() {
        tempoAtual = tempoCiclo;
        rodando = false;
    }
}