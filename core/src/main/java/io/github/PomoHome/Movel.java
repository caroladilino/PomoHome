package io.github.PomoHome;

import java.util.Objects;

public class Movel {
    private String nome;
    private String categoria;

    public Movel(String nome, String categoria) {
        this.nome = nome;
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movel movel = (Movel) o;
        return (Objects.equals(nome, movel.nome)) && (Objects.equals(categoria, movel.categoria));
    }
}
