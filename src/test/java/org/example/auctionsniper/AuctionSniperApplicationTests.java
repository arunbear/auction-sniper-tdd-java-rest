package org.example.auctionsniper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.example.auctionsniper.config.RemoteAuctionServiceConfig;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({
        @ConfigureWireMock(name = "auction-service", property = "auction-service.url")
})
class AuctionSniperApplicationTests {
    @InjectWireMock("auction-service")
    private WireMockServer wiremock;

    @Value("${auction-service.url}")
    private String wiremockUrl; // injects the base URL of the WireMockServer instance

    @LocalServerPort
    private int localServerPort;

    @BeforeEach
    public void setUp() {
        RestAssured.port = localServerPort;
    }

    @Test
    void sniperJoinsAuctionUntilAuctionCloses() {
        auctionStartsSellingItem();
        startBiddingInAuction();
        auctionHasReceivedJoinRequestFromSniper();
    }

    @Test
    void sniperMakesAHigherBidButLoses() {
        auctionStartsSellingItem(1000, 98, "other bidder");
        startBiddingInAuction();
        auctionHasReceivedBid(1098);
    }

    private void startBiddingInAuction() {
        RestAssured
            .get("/")
            .then()
            .log().body()
            .statusCode(equalTo(HttpStatus.SC_OK))
            .body("status", Matchers.equalTo(AuctionStatus.LOST.toString()));
    }

    void auctionStartsSellingItem() {
        wiremock
            .stubFor(post(RemoteAuctionServiceConfig.BIDDER_ENDPOINT)
            .willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
    }

    @lombok.SneakyThrows
    private void auctionStartsSellingItem(int itemPrice, int priceIncrement, String winningBidder) {
        JSONObject itemStatus = new JSONObject();
        itemStatus
            .put("price", itemPrice)
            .put("increment", priceIncrement)
            .put("winningBidder", winningBidder)
            .put("biddingLink", wiremockUrl + RemoteAuctionServiceConfig.BIDDING_ENDPOINT)
        ;
        wiremock
            .stubFor(post(RemoteAuctionServiceConfig.BIDDER_ENDPOINT)
            .willReturn(ResponseDefinitionBuilder.okForJson(itemStatus.toString())));
    }

    void auctionHasReceivedJoinRequestFromSniper() {
        wiremock.verify(1,
            postRequestedFor(
                urlEqualTo(RemoteAuctionServiceConfig.BIDDER_ENDPOINT)));
    }

    private void auctionHasReceivedBid(int bid) {
        wiremock.verify(1,
            postRequestedFor(
                urlEqualTo(RemoteAuctionServiceConfig.BIDDING_ENDPOINT))
                .withRequestBody(
                    matchingJsonPath("$.price", equalTo(String.valueOf(bid)))));
    }
}
