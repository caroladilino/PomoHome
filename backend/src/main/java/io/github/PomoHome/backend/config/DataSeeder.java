package io.github.PomoHome.backend.config;

import io.github.PomoHome.backend.entity.Movel;
import io.github.PomoHome.backend.repository.MovelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds the global store catalog (the MOVEL table) on startup.
 *
 * <p>The "Loja" is just {@code SELECT * FROM MOVEL} (see {@link Movel}), and
 * the frontend fetches it from {@code GET /api/loja}. A fresh database has an
 * empty catalog, so the store would render nothing — this runner inserts a
 * starter set the first time the server boots.
 *
 * <p><b>Idempotent:</b> it only seeds when the table is empty
 * ({@code count() == 0}), so restarting the server never duplicates rows.
 * The categories double as the client-side furniture sizes (see the frontend
 * {@code Movel.tamanhoPara}).
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedCatalogo(MovelRepository movelRepository) {
        return args -> {
            if (movelRepository.count() > 0) {
                return; // already seeded — do nothing
            }
            movelRepository.saveAll(List.of(
                    new Movel("Cama", "cama", 150),
                    new Movel("Sofa", "sofa", 100),
                    new Movel("Mesa", "mesa", 120),
                    new Movel("Cadeira", "cadeira", 80),
                    new Movel("Tapete", "tapete", 50),
                    new Movel("Planta", "planta", 30)
            ));
        };
    }
}
