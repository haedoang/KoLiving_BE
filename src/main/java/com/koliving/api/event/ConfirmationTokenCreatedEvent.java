package com.koliving.api.event;

import com.koliving.api.token.confirmation.ConfirmationToken;
import lombok.Getter;

@Getter
public class ConfirmationTokenCreatedEvent  {

    private final String email;
    private final String token;
    private final String linkPathResource;

    public ConfirmationTokenCreatedEvent(ConfirmationToken savedToken) {
        this.email = savedToken.getEmail();
        this.token = savedToken.getToken();
        this.linkPathResource = savedToken.getTokenType().getLinkPathResource();
    }
}
