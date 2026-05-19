package io.github.PomoHome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Historico {
    private FileHandle arquivo;
    private DateTimeFormatter formatadorData;
    private DateTimeFormatter formatadorHora;

    public Historico() {
        //Aponta para o arquivo .txt na raiz do projeto
        this.arquivo = Gdx.files.local("historico.txt");
        
        //Formatadores para deixar a data no padrão brasileiro (DD/MM e HH:MM)
        this.formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.formatadorHora = DateTimeFormatter.ofPattern("HH:mm");
    }

    public void registrarCicloConcluido(int segundosTotais) {
        LocalDateTime agora = LocalDateTime.now(); //horário fim
        LocalDateTime inicio = agora.minusSeconds(segundosTotais); //horário inicio (fim - tempo decorrido)

        //formatar a data e tempo corretamente
        int minutos = segundosTotais / 60;
        String dataStr = agora.format(formatadorData);
        String horaInicioStr = inicio.format(formatadorHora);
        String horaFimStr = agora.format(formatadorHora);

        //formatação do output
        String novaLinha = "Ciclo de estudos de " + minutos + " min - dia " + dataStr + " " + horaInicioStr + " - " + horaFimStr + "\n";

        //lê o histórico antigo
        String historicoAntigo = "";
        if (arquivo.exists()) {
            String conteudoCompleto = arquivo.readString();
            //se essa for a parte das estatisticas, ignorar
            if (conteudoCompleto.contains("--- ESTATÍSTICAS ---")) {
                historicoAntigo = conteudoCompleto.split("--- ESTATÍSTICAS ---")[0];
            } else {
                historicoAntigo = conteudoCompleto;
            }
        }

        //adiciona a nova linha ao historico antigo
        String novoHistorico = historicoAntigo + novaLinha;

        //calcula as estatisticas incluindo a nova linha
        String estatisticasAtualizadas = calcularEstatisticas(novoHistorico);

        //salva no .txt
        arquivo.writeString(novoHistorico + estatisticasAtualizadas, false);

        //checagem pra ver se deu certo
        System.out.println("Histórico e estatísticas atualizados no arquivo .txt!");
    }

    //função calcular estatísticas
    private String calcularEstatisticas(String todoOHistorico) {
        int totalCiclos = 0;
        int somaMinutos = 0;

        //divide o texto linha por linha
        String[] linhas = todoOHistorico.split("\n");
        for (String linha : linhas) {
            if (linha.contains("Ciclo de estudos de")) {
                totalCiclos++;
                try {
                    //pega a info que importa (numero de minutos)
                    String parteMinutos = linha.substring(linha.indexOf("estudos de ") + 11, linha.indexOf(" min"));
                    somaMinutos += Integer.parseInt(parteMinutos.trim());
                } catch (Exception e) {
                    //se der erro, ignora
                }
            }
        }

        int tempoMedio = (totalCiclos > 0) ? (somaMinutos / totalCiclos) : 0;

        //exibir informações
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- ESTATÍSTICAS ---\n");
        sb.append("Total de ciclos concluídos: ").append(totalCiclos).append("\n");
        sb.append("Tempo total de foco: ").append(somaMinutos).append(" minutos\n");
        sb.append("Tempo médio por ciclo: ").append(tempoMedio).append(" minutos\n");

        return sb.toString();
    }
}