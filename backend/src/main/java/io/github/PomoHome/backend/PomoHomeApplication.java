package io.github.PomoHome.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the PomoHome REST server.
 *
 * <p>{@code @SpringBootApplication} is shorthand for three annotations:
 * <ul>
 *   <li>{@code @Configuration}         — this class can define Spring beans</li>
 *   <li>{@code @EnableAutoConfiguration} — Spring Boot guesses sensible
 *       defaults based on the dependencies on the classpath (e.g. seeing
 *       spring-boot-starter-web -> set up Tomcat on 8080)</li>
 *   <li>{@code @ComponentScan}          — scan THIS package and below for
 *       {@code @Component / @Service / @Repository / @RestController}.
 *       This is why every backend class lives under
 *       {@code io.github.PomoHome.backend.*} — keep it that way.</li>
 * </ul>
 *
 * <p>Run with: <code>./gradlew :backend:bootRun</code>
 */
@SpringBootApplication
public class PomoHomeApplication {

    public static void main(String[] args) {
        // TODO: That is literally all you need. Spring Boot will:
        //   1. Read application.properties
        //   2. Open the H2 connection
        //   3. Scan entities, build the schema (ddl-auto=update)
        //   4. Start Tomcat on the configured port
        //   5. Register every @RestController as an HTTP endpoint
        SpringApplication.run(PomoHomeApplication.class, args);
    }
}
