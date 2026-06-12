package io.github.PomoHome.network;

/**
 * Thrown by {@link ApiClient} when the server answers with a non-2xx status.
 *
 * The backend's GlobalExceptionHandler returns a plain-text body with the
 * reason (e.g. "Username já existe", "Credenciais inválidas"). That body is
 * carried here as the message so screens can show it to the user verbatim.
 */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    /** HTTP status code (400, 401, 404, ...). 0 if the request never reached the server. */
    public int getStatus() {
        return status;
    }
}
