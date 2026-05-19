package io.github.PomoHome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Historico {
    private FileHandle arquivo;

    public Historico() {
        // Cria ou aponta para o arquivo "historico.txt" na raiz do projeto
        this.arquivo = Gdx.files.local("historico.txt");
    }

    public void registrarCicloConcluido(int segundos) {
        int totalCiclos = 0;

        // 1. Se o arquivo já existir, vamos ler o que está escrito nele
        if (arquivo.exists()) {
            try {
                // Lê o texto do arquivo, remove espaços em branco e converte para número
                String conteudo = arquivo.readString().trim();
                if (!conteudo.isEmpty()) {
                    totalCiclos = Integer.parseInt(conteudo);
                }
            } catch (NumberFormatException e) {
                // Se o arquivo estiver corrompido ou modificado com texto inválido, recomeça do 0
                totalCiclos = 0;
            }
        }

        // 2. Incrementa o total de ciclos concluídos
        totalCiclos++;

        // 3. Grava o novo número de volta no arquivo .txt (isso cria o arquivo se ele não existir)
        // O segundo parâmetro 'false' significa: "sobrescreva o arquivo com o novo valor"
        arquivo.writeString(String.valueOf(totalCiclos), false);

        System.out.println("Arquivo atualizado! Total de ciclos salvos no txt: " + totalCiclos);
    }

    // Função caso você queira ler o número para mostrar na tela do jogo depois
    public int lerTotalCiclos() {
        if (arquivo.exists()) {
            try {
                return Integer.parseInt(arquivo.readString().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}