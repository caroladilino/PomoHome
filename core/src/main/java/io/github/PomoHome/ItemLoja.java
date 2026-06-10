package io.github.PomoHome;

public class ItemLoja {
    public boolean disponivel;
    public Movel movel;

    // O construtor DEVE receber exatamente o Movel primeiro e o boolean depois
    public ItemLoja(Movel movel, boolean disponivel) {
        this.movel = movel;
        this.disponivel = disponivel;
    }
}