package io.github.PomoHome;

import java.util.ArrayList;

public class Jogador {
    public String username;
    public int saldo;
    public int tempoEstudado;
    public Casa casa;
    public ArrayList<Movel> inventario;
    public ArrayList<Jogador> listaAmigos;

    public Jogador(String username, Casa casaIncial) {
        this.username = username;
        this.casa = casaIncial;
        this.saldo = 1110;
        this.tempoEstudado = 0;
        this.inventario = new ArrayList<>();
        this.listaAmigos = new ArrayList<>();
    }

    public void adicionarSaldo(int valor) {
        this.saldo += valor;
    }

    public boolean gastarSaldo(int valor) {
        if (this.saldo >= valor) {
            this.saldo -= valor;
            return true;
        }
        return false;
    }
}