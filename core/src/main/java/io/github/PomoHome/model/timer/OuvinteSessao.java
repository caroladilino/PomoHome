package io.github.PomoHome.model.timer;

/**
 * Observer of the Pomodoro timer (GoF Observer).
 *
 * <p>Subscribers register with {@link ContextoTimer#adicionarOuvinte} and are
 * notified once each time a cycle reaches zero. This decouples the timer's
 * lifecycle from its consequences (crediting coins, refreshing history,
 * achievements, …) — the timer no longer needs to know who reacts.
 */
public interface OuvinteSessao {

    /**
     * A study cycle just completed.
     *
     * @param minutos the configured cycle length in minutes (= coins earned).
     */
    void cicloConcluido(int minutos);
}
