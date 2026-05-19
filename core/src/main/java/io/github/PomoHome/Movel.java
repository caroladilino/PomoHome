package io.github.PomoHome;

public class Movel {
    public String categoria;
    public String nome;
    public int preco;

    // Atributos extras que precisamos manter para o grid/engine funcionar depois
    public int widthInTiles = 1;
    public int heightInTiles = 1;
    public String assetId; 

    public Movel(String categoria, String nome, int preco, int widthInTiles, int heightInTiles, String assetId) {
        this.categoria = categoria;
        this.nome = nome;
        this.preco = preco;
        this.widthInTiles = widthInTiles;
        this.heightInTiles = heightInTiles;
        this.assetId = assetId;
    }
}