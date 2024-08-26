package org.example.auctionsniper;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuctionSniperApplicationTests {
    @LocalServerPort
    private int localServerPort;

    @BeforeEach
    public void setUp() {
        RestAssured.port = localServerPort;
    }

    @Test
    void sniper_joins_auction_until_auction_closes() {
        startBiddingInAuction();
    }

    private void startBiddingInAuction() {
        RestAssured
            .get("/")
            .then()
            .statusCode(equalTo(HttpStatus.SC_OK))
            .body("status", equalTo("JOINING"));
    }
}
