package org.dplay.server.domain.webhook.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.webhook.service.WebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional
public class WebhookServiceImpl implements WebhookService {
    @Value("${discord.webhook.url}")
    private String discordWebhookUrl;

    @Override
    public void sendDiscordNotification(String message) {
        if (discordWebhookUrl == null || discordWebhookUrl.isBlank()) {
            return;
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = new HashMap<>();
        body.put("content", message);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(discordWebhookUrl, requestEntity, String.class);
    }
}
