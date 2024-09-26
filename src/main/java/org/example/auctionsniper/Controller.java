package org.example.auctionsniper;

import org.example.auctionsniper.config.AuctionServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
public class Controller {

    private final AuctionServiceConfig auctionServiceConfig;

    @Autowired
    public Controller(AuctionServiceConfig auctionServiceConfig) {
        this.auctionServiceConfig = auctionServiceConfig;
    }

    @GetMapping(value = "/")
    public @ResponseBody Map<String, String> defaultRoute() throws IOException, InterruptedException {
        joinAuction();
        return Map.of("status", AuctionStatus.LOST.toString());
    }

    private void joinAuction() throws IOException, InterruptedException {
        String joinAuctionUri = "%s%s".formatted(
            auctionServiceConfig.getServiceUri(),
            AuctionServiceConfig.BIDDER_ENDPOINT
        );

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(joinAuctionUri))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
    }
}
