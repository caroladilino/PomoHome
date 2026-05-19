package io.github.PomoHome;

import java.util.ArrayList;

public class Loja {
    // CORREÇÃO: Garanta o uso explícito de <ItemLoja> aqui
    public ArrayList<ItemLoja> itens; 

    public Loja() {
        this.itens = new ArrayList<ItemLoja>();
    }

    public boolean comprarItem(Jogador jogador, ItemLoja itemLoja) {
        if (itemLoja.disponivel && jogador.gastarSaldo(itemLoja.movel.preco)) {
            jogador.inventario.add(itemLoja.movel); 
            itemLoja.disponivel = false; 
            return true;
        }
        return false;
    }
}