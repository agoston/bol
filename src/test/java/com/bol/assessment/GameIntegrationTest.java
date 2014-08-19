package com.bol.assessment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {GameMain.class})
@WebAppConfiguration
@IntegrationTest
public class GameIntegrationTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseURL = "http://localhost:8080";

    @Test
    public void testLogin() {
        Player player = restTemplate.getForObject(baseURL + "/login", Player.class);
        assertThat(player.getId(), is(UUID.class));
        assertThat(player.getName(), is("Anonymous"));
    }

    @Test
    public void testStartMatchNoOtherPlayers() {
        Player player = restTemplate.getForObject(baseURL + "/login", Player.class);
        ResponseEntity<String> startResponse = restTemplate.getForEntity(baseURL + "/start/" + player.getId(), String.class);
        assertThat(startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));
    }

    @Test
    public void testStartMatchOtherPlayerJoinsLater() {
        Player player1 = restTemplate.getForObject(baseURL + "/login", Player.class);
        ResponseEntity<String> startResponse = restTemplate.getForEntity(baseURL + "/start/" + player1.getId(), String.class);
        assertThat(startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));

        Player player2 = restTemplate.getForObject(baseURL + "/login", Player.class);
        ResponseEntity<String> match = restTemplate.getForEntity(baseURL + "/start/" + player2.getId(), String.class);
        System.err.println(match);
//        assertThat(match.getStatusCode(), is(HttpStatus.OK));
    }
}
