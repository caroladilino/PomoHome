package io.github.PomoHome;

import java.util.ArrayList;

public class Jogador {
    public String username;
    public int saldo;
    public ArrayList<Movel> inventario;

    public Jogador(String username) {
        this.username = username;
        this.saldo = 100;
        this.inventario = new ArrayList<>();
    }

    public ArrayList<Movel> getMoveisAdquiridos() {
        return inventario;
    }

    public int getSaldo() {
        return saldo;
    }

    public void subtraiSaldo(int valor) {
        if (saldo >= valor)
            saldo -= valor;
    }

    public void adicionarMovel(Movel movel) {
        inventario.add(movel);
    }
}
