package se.tmeit.app.services;

/**
 * Interface for a class that handles an authentication result.
 */
public interface AuthenticationResultHandler {
    void onAuthenticationError(int errorMessage);

    void onNetworkError(int errorMessage);

    void onProtocolError(int errorMessage);

    void onSuccess(String serviceAuth, String authenticatedUser);
}
