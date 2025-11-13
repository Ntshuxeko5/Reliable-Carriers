package com.reliablecarriers.Reliable.Carriers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/echo-headers")
    public ResponseEntity<Map<String, String>> echoHeaders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(Map.of("Authorization", authorization == null ? "<none>" : authorization));
    }
}
