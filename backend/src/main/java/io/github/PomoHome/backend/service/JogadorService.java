package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.entity.Casa;
import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.Movel;
import io.github.PomoHome.backend.exception.AutenticacaoException;
import io.github.PomoHome.backend.repository.JogadorRepository;
import io.github.PomoHome.backend.repository.MovelRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Business logic for Jogador. This is where the GAME RULES live —
 * never in the controller, never in the entity.
 *
 * Layering reminder:
 *   Controller ---calls---> Service ---calls---> Repository ---> DB
 *
 * Why @Service and not just any class? Spring scans for this annotation
 * and creates a singleton bean we can inject elsewhere.
 *
 * Constructor injection (preferred over @Autowired on fields) lets us
 *   - keep fields final
 *   - write unit tests without Spring (just `new JogadorService(mockRepo, ...)`).
 */
@Service
public class JogadorService {

    private final JogadorRepository jogadorRepository;
    private final MovelRepository movelRepository;
    private final PasswordEncoder passwordEncoder;

    public JogadorService(JogadorRepository jogadorRepository,
                          MovelRepository movelRepository,
                          PasswordEncoder passwordEncoder) {
        this.jogadorRepository = jogadorRepository;
        this.movelRepository = movelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -----------------------------------------------------------------
    // Account
    // -----------------------------------------------------------------

    /**
     * Register a new player: validates the username is free, hashes the
     * password with BCrypt, and attaches a default Casa.
     *
     * <p>The house starts with <b>zero</b> slots. The frontend uses a free
     * 8×8 isometric grid and persists the whole layout via
     * {@code PUT /api/casas/{id}/layout} (see {@link CasaService#salvarLayout}),
     * which (re)creates one Slot per placed móvel — {@code Slot.nomePosicao}
     * holds the grid tile name (e.g. "L3C5"). The old fixed sofa/mesa/cama
     * slots were removed because the grid is now the source of truth.
     */
    @Transactional
    public Jogador cadastrar(String username, String senha) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio");
        }
        if (jogadorRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username já existe");
        }

        Casa casa = new Casa(username + "'s Home");

        Jogador jogador = new Jogador(username, passwordEncoder.encode(senha));
        jogador.setCasa(casa);
        casa.setDono(jogador);

        return jogadorRepository.save(jogador);
    }

    /**
     * Authenticate a player. Throws AutenticacaoException on either an
     * unknown username or a wrong password — the message never reveals
     * which one was wrong.
     */
    @Transactional(readOnly = true)
    public Jogador autenticar(String username, String senha) {
        Jogador jogador = jogadorRepository.findByUsername(username)
                .orElseThrow(AutenticacaoException::new);
        if (!passwordEncoder.matches(senha, jogador.getSenha())) {
            throw new AutenticacaoException();
        }
        return jogador;
    }

    @Transactional(readOnly = true)
    public Optional<Jogador> buscarPorId(Long id) {
        return jogadorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Jogador> buscarPorUsername(String username) {
        return jogadorRepository.findByUsername(username);
    }

    // -----------------------------------------------------------------
    // Economy (coins)
    // -----------------------------------------------------------------

    /**
     * Credit "minutosCompletos" coins to a player and increment their
     * total study time by the same amount. Called by SessaoEstudoService
     * after a Pomodoro session is registered.
     */
    @Transactional
    public Jogador creditarMoedas(Long jogadorId, int minutosCompletos) {
        if (minutosCompletos <= 0) {
            throw new IllegalArgumentException("Minutos concluídos deve ser maior que zero");
        }
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(NoSuchElementException::new);
        jogador.setSaldo(jogador.getSaldo() + minutosCompletos);
        jogador.setTempoEstudado(jogador.getTempoEstudado() + minutosCompletos);
        return jogadorRepository.save(jogador);
    }

    /**
     * Admin/debug: add {@code valor} coins to a player's balance WITHOUT
     * touching their study time (so it doesn't pollute the ranking). Used to
     * top up a test account.
     */
    @Transactional
    public Jogador creditarSaldo(Long jogadorId, int valor) {
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(NoSuchElementException::new);
        jogador.setSaldo(jogador.getSaldo() + valor);
        return jogadorRepository.save(jogador);
    }

    /**
     * Buy a Movel from the store and add it to the player's inventory.
     * Debit + inventory add happen in one @Transactional so a failure
     * rolls the coin debit back automatically.
     */
    @Transactional
    public Jogador comprarMovel(Long jogadorId, Long movelId) {
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(NoSuchElementException::new);
        Movel movel = movelRepository.findById(movelId)
                .orElseThrow(NoSuchElementException::new);
        if (jogador.getSaldo() < movel.getPreco()) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        // Each catalog móvel can be owned at most once — no duplicate purchases.
        boolean jaPossui = jogador.getInventario().stream()
                .anyMatch(m -> m.getId().equals(movel.getId()));
        if (jaPossui) {
            throw new IllegalArgumentException("Móvel já comprado");
        }
        jogador.setSaldo(jogador.getSaldo() - movel.getPreco());
        jogador.getInventario().add(movel);
        return jogadorRepository.save(jogador);
    }

    /** Admin/debug: empty a player's furniture inventory (keeps coins). */
    @Transactional
    public Jogador limparInventario(Long jogadorId) {
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(NoSuchElementException::new);
        jogador.getInventario().clear();
        return jogadorRepository.save(jogador);
    }

    // -----------------------------------------------------------------
    // Ranking
    // -----------------------------------------------------------------

    /**
     * Returns all players sorted by total study time (highest first).
     * Backs GET /api/jogadores/ranking.
     */
    @Transactional(readOnly = true)
    public List<Jogador> listarRanking() {
        return jogadorRepository.findAllByOrderByTempoEstudadoDesc();
    }
}
