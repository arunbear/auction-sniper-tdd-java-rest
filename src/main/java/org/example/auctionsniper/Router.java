package org.example.auctionsniper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class Router {

    @GetMapping(value = "/")
    public @ResponseBody Map<String, String> defaultRoute() {
        return Map.of("status", "JOINING");
    }
}
