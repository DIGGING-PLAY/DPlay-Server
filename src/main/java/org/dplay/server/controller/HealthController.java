package org.dplay.server.controller;

import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.dplay.server.global.response.ResponseError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/success/ok")
    public ResponseEntity<ApiResponse<Void>> success() {
        return ResponseBuilder.ok(null);
    }

    @GetMapping("/success/created")
    public ResponseEntity<ApiResponse<Void>> created() {
        return ResponseBuilder.created(null);
    }

    @GetMapping("/fail")
    public ResponseEntity<ApiResponse<Void>> getError() {
        throw new DPlayException(ResponseError.EXPIRED_ACCESS_TOKEN);
    }
}

