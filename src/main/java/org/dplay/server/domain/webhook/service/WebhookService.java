package org.dplay.server.domain.webhook.service;

public interface WebhookService {
    void sendDiscordNotification(String message);
}
