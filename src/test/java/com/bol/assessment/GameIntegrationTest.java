package com.bol.assessment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {GameMain.class})
@WebAppConfiguration
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GameIntegrationTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseURL = "http://localhost:8080";

    @Test
    public void testLogin() {
        Player player = restTemplate.postForObject(baseURL + "/gamer", null, Player.class);
        assertThat(player.getId(), is(UUID.class));
        assertThat(player.getName(), is("Anonymous"));
    }

    @Test
    public void testLogout() {
        Player player = restTemplate.postForObject(baseURL + "/gamer", null, Player.class);
        restTemplate.delete(baseURL + "/gamer/" + player.getId());
        try {
            ResponseEntity<String> startResponse = restTemplate.getForEntity(baseURL + "/gamer/" + player.getId() + "/start", String.class);
            fail();
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));
        }
    }

    @Test
    public void testStartMatchNoOtherPlayers() {
        Player player = restTemplate.postForObject(baseURL + "/gamer", null, Player.class);
        ResponseEntity<String> startResponse = restTemplate.getForEntity(baseURL + "/gamer/" + player.getId() + "/start", String.class);
        assertThat(startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));
    }

    @Test
    public void testStartMatchOtherPlayerJoinsLater() {
        Player player1 = restTemplate.postForObject(baseURL + "/gamer", null, Player.class);
        ResponseEntity<String> startResponse = restTemplate.getForEntity(baseURL + "/gamer/" + player1.getId() + "/start", String.class);
        assertThat(startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));

        Player player2 = restTemplate.postForObject(baseURL + "/gamer", null, Player.class);
        ResponseEntity<Match> match = restTemplate.getForEntity(baseURL + "/gamer/" + player2.getId() + "/start", Match.class);
        assertThat(match.getStatusCode(), is(HttpStatus.OK));
    }
}
