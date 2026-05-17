package io.github.PomoHome.backend.service;

import io.github.PomoHome.backend.entity.Casa;
import io.github.PomoHome.backend.entity.Jogador;
import io.github.PomoHome.backend.entity.Movel;
import io.github.PomoHome.backend.entity.Slot;
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
     * Register a new player.
     *
     * TODO (steps):
     *   1. Validate that 'username' is not blank.
     *   2. Use jogadorRepository.existsByUsername(username); if true, throw
     *      a clear exception (e.g. new IllegalArgumentException("Username já existe")
     *      or a custom UsernameAlreadyTakenException).
     *   3. Hash 'senha' before storing (BCrypt). Plain text is OK only for
     *      the very first prototype.
     *   4. Create a new Jogador, save it.
     *   5. (Recommended) Also create a default Casa with empty Slots and
     *      attach it via setCasa(...) BEFORE saving — cascade = ALL persists
     *      the Casa together.
     *   6. Return the saved Jogador.
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
        casa.addSlot(new Slot("sala-sofa", "sofa"));
        casa.addSlot(new Slot("sala-mesa", "mesa"));
        casa.addSlot(new Slot("quarto-cama", "cama"));

        Jogador jogador = new Jogador(username, passwordEncoder.encode(senha));
        jogador.setCasa(casa);
        casa.setDono(jogador);

        return jogadorRepository.save(jogador);
    }

    /**
     * Authenticate a player.
     *
     * TODO (steps):
     *   1. jogadorRepository.findByUsername(username) -> Optional<Jogador>.
     *   2. If empty, throw "credenciais inválidas" (do NOT reveal which one
     *      was wrong — username or password).
     *   3. Compare provided senha with stored hash (BCrypt.checkpw).
     *   4. If match, return the Jogador (consider returning a "session token"
     *      DTO later — for the prototype, returning the Jogador is fine).
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
    // Friends
    // -----------------------------------------------------------------

    /**
     * Add 'amigoId' to jogadorId's friend list.
     *
     * TODO (steps):
     *   1. Load the Jogador (throw if not found).
     *   2. Verify the amigoId actually exists in the DB (load it or use
     *      jogadorRepository.existsById(amigoId)).
     *   3. Reject self-friendship (jogadorId == amigoId).
     *   4. If the list already contains amigoId, do nothing (idempotent).
     *   5. Add it, save the Jogador.
     *   6. (Optional) make friendship bidirectional by also adding jogadorId
     *      to the friend's amigosIds list.
     */
    @Transactional
    public Jogador adicionarAmigo(Long jogadorId, Long amigoId) {
        if (jogadorId.equals(amigoId)) {
            throw new IllegalArgumentException("Jogador não pode se adicionar como amigo");
        }
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(NoSuchElementException::new);
        if (!jogadorRepository.existsById(amigoId)) {
            throw new NoSuchElementException();
        }
        if (!jogador.getAmigosIds().contains(amigoId)) {
            jogador.getAmigosIds().add(amigoId);
            jogadorRepository.save(jogador);
        }
        return jogador;
    }

    // -----------------------------------------------------------------
    // Economy (coins)
    // -----------------------------------------------------------------

    /**
     * Credit "minutosCompletos" coins to a player and increment their
     * total study time by the same amount. Called by SessaoEstudoService
     * after a Pomodoro session is registered.
     *
     * TODO (steps):
     *   1. Load the Jogador (throw if not found).
     *   2. jogador.setSaldo(jogador.getSaldo() + minutosCompletos);
     *   3. jogador.setTempoEstudado(jogador.getTempoEstudado() + minutosCompletos);
     *   4. save() — @Transactional means dirty-checking would auto-flush, but
     *      explicit save() makes intent obvious to the team.
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
     * Buy a Movel from the store and add it to the player's inventory.
     *
     * TODO (steps — the trickiest one, read carefully):
     *   1. Load the Jogador (throw if not found).
     *   2. Load the Movel  (throw if not found).
     *   3. If jogador.getSaldo() < movel.getPreco() -> throw
     *      new SaldoInsuficienteException(...).
     *   4. Debit: jogador.setSaldo(jogador.getSaldo() - movel.getPreco());
     *   5. Add to inventory: jogador.getInventario().add(movel);
     *      (no need to save the Movel — it already exists)
     *   6. Save the Jogador. Return it.
     *
     *   IMPORTANT: this whole sequence must be inside ONE @Transactional so
     *   that if step 5 fails the coin debit is rolled back automatically.
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
        jogador.setSaldo(jogador.getSaldo() - movel.getPreco());
        jogador.getInventario().add(movel);
        return jogadorRepository.save(jogador);
    }

    // -----------------------------------------------------------------
    // Ranking
    // -----------------------------------------------------------------

    /**
     * Returns all players sorted by total study time (highest first).
     * Backs GET /api/jogadores/ranking.
     *
     * TODO: just delegate to jogadorRepository.findAllByOrderByTempoEstudadoDesc().
     *       Later: paginate, or expose a "Top 10" version.
     */
    @Transactional(readOnly = true)
    public List<Jogador> listarRanking() {
        return jogadorRepository.findAllByOrderByTempoEstudadoDesc();
    }
}
