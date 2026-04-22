package io.github.PomoHome;

import java.util.ArrayList;
import java.util.Objects;

public class Casa {
    private String nome;
    private ArrayList<Movel> moveis;

    public Casa(String nome) {
        this.nome = nome;
        this.moveis = new ArrayList<>();
    }

    public ArrayList<Movel> getMoveis() {
        return moveis;
    }

    public String getNome() {
        return nome;
    }

    public void adicionarMovel(Movel movel) {
        moveis.add(movel);
    }
}
