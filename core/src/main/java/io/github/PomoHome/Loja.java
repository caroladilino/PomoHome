package io.github.PomoHome;

import java.util.ArrayList;

public class Loja {
    public ArrayList<Movel> moveis;

    public Loja() {
        moveis = new ArrayList<>();
        moveis.add(new Movel("sofá preto", "sofá", 20));
        moveis.add(new Movel("cadeira madeira", "cadeira", 30));
        moveis.add(new Movel("mesa moderna", "mesa", 15));
        moveis.add(new Movel("cama prata", "cama", 50));
        moveis.add(new Movel("abajur", "iluminação", 10));
    }

    public ArrayList<Movel> getMoveis() {
        return moveis;
    }

    public boolean comprarMovel(Jogador jogador, Movel movel) {
        if (!moveis.contains(movel)) return false;
        if (jogador.getSaldo() >= movel.getPreco()) {
            jogador.subtraiSaldo(movel.getPreco());
            jogador.adicionarMovel(movel);
            moveis.remove(movel);
            return true;
        }
        return false;
    }
}
