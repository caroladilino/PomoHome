package io.github.PomoHome.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around the list of SessaoEstudo records returned by
 * GET /api/sessoes/jogador/{id}. Already ordered by dataHora DESC.
 *
 * TODO (TEAM): consider adding helper methods like:
 *   - int totalMinutosNoMes(int ano, int mes)
 *   - int diasConsecutivosEstudando()
 * Those keep TelaPomodoro / TelaRanking clean.
 */
public class Historico {

    private List<SessaoEstudo> sessoes = new ArrayList<>();

    public Historico() { }

    public List<SessaoEstudo> getSessoes() { return sessoes; }
    public void setSessoes(List<SessaoEstudo> sessoes) { this.sessoes = sessoes; }
}
