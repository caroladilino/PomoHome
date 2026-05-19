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
        // Aponta para o arquivo .txt na raiz do projeto
        this.arquivo = Gdx.files.local("historico.txt");
        
        // Formatadores para deixar a data no padrão brasileiro (DD/MM e HH:MM)
        this.formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.formatadorHora = DateTimeFormatter.ofPattern("HH:mm");
    }

    public void registrarCicloConcluido(int segundosTotais) {
        // 1. Descobre os horários de início e fim
        LocalDateTime agora = LocalDateTime.now();
        // Subtrai os segundos do timer para saber quando ele começou
        LocalDateTime inicio = agora.minusSeconds(segundosTotais); 

        int minutos = segundosTotais / 60;
        String dataStr = agora.format(formatadorData);
        String horaInicioStr = inicio.format(formatadorHora);
        String horaFimStr = agora.format(formatadorHora);

        // 2. Monta a nova linha que será adicionada
        // Exemplo: "Ciclo de estudos de 25 min - dia 19/05/2026 19:30 - 19:55"
        String novaLinha = "Ciclo de estudos de " + minutos + " min - dia " + dataStr + " " + horaInicioStr + " - " + horaFimStr + "\n";

        // 3. Lê o histórico antigo (sem as estatísticas antigas) para reconstruir o arquivo
        String historicoAntigo = "";
        if (arquivo.exists()) {
            String conteudoCompleto = arquivo.readString();
            // Removemos a seção de estatísticas antiga para não ficar duplicando no final
            if (conteudoCompleto.contains("--- ESTATÍSTICAS ---")) {
                historicoAntigo = conteudoCompleto.split("--- ESTATÍSTICAS ---")[0];
            } else {
                historicoAntigo = conteudoCompleto;
            }
        }

        // 4. Junta o histórico antigo com a nova linha
        String novoHistorico = historicoAntigo + novaLinha;

        // 5. Calcula as novas estatísticas baseadas em todo o texto
        String estatisticasAtualizadas = calcularEstatisticas(novoHistorico);

        // 6. Grava tudo de volta no arquivo (.txt completo com o rodapé atualizado)
        arquivo.writeString(novoHistorico + estatisticasAtualizadas, false);

        System.out.println("Histórico e estatísticas atualizados no arquivo .txt!");
    }

    private String calcularEstatisticas(String todoOHistorico) {
        int totalCiclos = 0;
        int somaMinutos = 0;

        // Divide o texto linha por linha para analisar
        String[] linhas = todoOHistorico.split("\n");
        for (String linha : linhas) {
            if (linha.contains("Ciclo de estudos de")) {
                totalCiclos++;
                try {
                    // Extrai o número de minutos que está entre "estudos de " e " min"
                    String parteMinutos = linha.substring(linha.indexOf("estudos de ") + 11, linha.indexOf(" min"));
                    somaMinutos += Integer.parseInt(parteMinutos.trim());
                } catch (Exception e) {
                    // Ignora se der algum erro de leitura na linha
                }
            }
        }

        int tempoMedio = (totalCiclos > 0) ? (somaMinutos / totalCiclos) : 0;

        // Monta o bloco de texto que vai ficar fixo no final do arquivo
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- ESTATÍSTICAS ---\n");
        sb.append("Total de ciclos concluídos: ").append(totalCiclos).append("\n");
        sb.append("Tempo total de foco: ").append(somaMinutos).append(" minutos\n");
        sb.append("Tempo médio por ciclo: ").append(tempoMedio).append(" minutos\n");

        return sb.toString();
    }
}