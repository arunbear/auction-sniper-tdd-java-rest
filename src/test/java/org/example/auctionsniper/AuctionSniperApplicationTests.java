package org.example.auctionsniper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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

    private void startBiddingInAuction() {
        RestAssured
            .get("/")
            .then()
            .log().body()
            .statusCode(equalTo(HttpStatus.SC_OK))
            .body("status", equalTo(AuctionStatus.LOST.toString()));
    }

    void auctionStartsSellingItem() {
        wiremock.stubFor(post("/auction/bidder").willReturn(ResponseDefinitionBuilder.okForEmptyJson()));
    }

    void auctionHasReceivedJoinRequestFromSniper() {
        wiremock.verify(1, postRequestedFor(urlEqualTo("/auction/bidder")));
    }
}
