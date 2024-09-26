package org.example.auctionsniper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuctionServiceConfig {

    public static final String BIDDER_ENDPOINT = "/auction/bidder";
    @Value("${auction-service.url}")
    private String serviceUri;

    public String getServiceUri() {
        return serviceUri;
    }
}
