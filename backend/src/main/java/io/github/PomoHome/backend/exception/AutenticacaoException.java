package io.github.PomoHome.backend.exception;

public class AutenticacaoException extends RuntimeException {

    public AutenticacaoException() {
        super("Credenciais inválidas");
    }
}
