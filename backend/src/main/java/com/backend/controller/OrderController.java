package com.backend.controller;


import com.backend.dto.BuyProductRequest;
import com.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyProducts(@RequestBody BuyProductRequest buyProductRequest, Principal principal) {

        String response = orderService.buyProducts(buyProductRequest, principal.getName());
        return ResponseEntity.ok(response);

    }

}
