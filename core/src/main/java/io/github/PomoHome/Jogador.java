package io.github.PomoHome;

import java.util.ArrayList;
import java.util.Objects;

public class Jogador {
    public String username;
    public Casa casa;
    public ArrayList<Movel> inventario;

    public Jogador(String username) {
        this.username = username;
        this.casa = new Casa("Casa");
        this.inventario = new ArrayList<>();
        inventario.add(new Movel("sofá preto", "sofá"));
        inventario.add(new Movel("sofá branco", "sofá"));
        inventario.add(new Movel("mesa moderna", "mesa"));
        inventario.add(new Movel("mesa rústica", "mesa"));
        inventario.add(new Movel("tapete rosa", "tapete"));
    }

    public ArrayList<Movel> getInventario() {
        return inventario;
    }

    public Casa getCasa() {
        return casa;
    }

    public boolean moverMovelParaCasa(Movel movel) {
        if (!inventario.contains(movel)) return false;
        ArrayList<Movel> moveisCasa = casa.getMoveis();
        // Verifica se algum móvel de mesma categoria está na casa
        for (Movel m : moveisCasa) {
            if (Objects.equals(m.getCategoria(), movel.getCategoria())) return false;
        }
        inventario.remove(movel);
        casa.adicionarMovel(movel);
        return true;
    }
}
