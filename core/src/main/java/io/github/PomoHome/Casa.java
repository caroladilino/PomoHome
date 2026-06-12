package io.github.PomoHome;

import java.util.ArrayList;

public class Casa {
    public String nome;
    public int numLikes;
    public ArrayList<Movel> moveis; // Lista de móveis que estão posicionados na casa

    public Casa(String nome) {
        this.nome = nome;
        this.numLikes = 0;
        this.moveis = new ArrayList<>();
    }

    public void adicionarMovel(Movel movel) {
        moveis.add(movel);
    }

    public void removerMovel(Movel movel) {
        moveis.remove(movel);
    }

    public void alterarNome(String novoNome) {
    this.nome = novoNome;
    }   
}