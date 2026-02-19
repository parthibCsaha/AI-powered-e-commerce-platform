package com.backend.controller;

import com.backend.dto.AiRequest;
import com.backend.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    @PostMapping("/ask")
    public String ask(@RequestBody AiRequest request) {
        System.out.println("Received prompt: " + request.prompt());
        return aiService.askAI(request.prompt());
    }

}
