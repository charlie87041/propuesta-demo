package com.cookiesstore.pos.auth;

public record PosAuthenticationResult(
    PosAuthenticationStatus status,
    PosPrincipal principal
) {
    public static PosAuthenticationResult success(PosPrincipal principal) {
        return new PosAuthenticationResult(PosAuthenticationStatus.SUCCESS, principal);
    }

    public static PosAuthenticationResult posDisabled() {
        return new PosAuthenticationResult(PosAuthenticationStatus.POS_DISABLED, null);
    }

    public static PosAuthenticationResult invalidCredentials() {
        return new PosAuthenticationResult(PosAuthenticationStatus.INVALID_CREDENTIALS, null);
    }
}

