package io.github.PomoHome;

public class Timer {
    public int tempoCiclo; // Tempo total configurado (em segundos)
    public float tempoAtual; // Usamos float internamente para descontar os milissegundos precisos da engine
    public boolean rodando;

    public Timer(int tempoCiclo) {
        this.tempoCiclo = tempoCiclo;
        this.tempoAtual = tempoCiclo;
        this.rodando = false;
    }

    // A engine vai chamar isso todo frame
    public boolean atualizar(float delta) {
        if (rodando && tempoAtual > 0) {
            tempoAtual -= delta;
            if (tempoAtual <= 0) {
                tempoAtual = 0;
                rodando = false;
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