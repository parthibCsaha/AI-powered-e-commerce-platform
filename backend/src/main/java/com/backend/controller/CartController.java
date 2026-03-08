package com.backend.controller;

import com.backend.dto.CartRequest;
import com.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> getAllCart(Principal principal) {
        String response = cartService.getAllCart(principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequest cartRequest, Principal principal) {

        String response = cartService.addToCart(cartRequest.productId(), cartRequest.quantity(), principal.getName());
        return ResponseEntity.ok(response);
    }



}
