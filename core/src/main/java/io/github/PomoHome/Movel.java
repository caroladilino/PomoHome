package io.github.PomoHome;

import com.badlogic.gdx.graphics.Color;

public class Movel {
    public String nome;
    public int preco;
    public Color corRepresentativa; // Para diferenciar os quadrados enquanto não temos as artes

    public Movel(String nome, int preco, Color cor) {
        this.nome = nome;
        this.preco = preco;
        this.corRepresentativa = cor;
    }
}