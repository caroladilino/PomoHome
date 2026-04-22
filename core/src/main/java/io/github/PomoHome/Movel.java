package io.github.PomoHome;

import java.util.Objects;

public class Movel {
    private String nome;
    private String categoria;
    private int preco;

    public Movel(String nome, String categoria, int preco) {
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
    }

    public int getPreco() {
        return preco;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movel movel = (Movel) o;
        return (Objects.equals(nome, movel.nome)) && (Objects.equals(categoria, movel.categoria));
    }
}
