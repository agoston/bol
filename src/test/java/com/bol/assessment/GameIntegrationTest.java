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
import static org.junit.Assert.assertArrayEquals;
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
        Player player = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        assertThat(player.getId(), is(UUID.class));
        assertThat(player.getName(), is("Anonymous"));
    }

    @Test
    public void testLogout() {
        Player player = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        restTemplate.delete(baseURL + "/player/" + player.getId());
        try {
            ResponseEntity<String> startResponse = restTemplate.postForEntity(baseURL + "/player/" + player.getId() + "/match", null, String.class);
            fail();
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));
        }
    }

    @Test
    public void testStartMatchNoOtherPlayers() {
        Player player = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        ResponseEntity<String> startResponse = restTemplate.postForEntity(baseURL + "/player/" + player.getId() + "/match", null, String.class);
        assertThat(startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));

        startResponse = restTemplate.postForEntity(baseURL + "/player/" + player.getId() + "/match", null, String.class);
        assertThat(startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));
    }

    @Test
    public void testStartMatchOtherPlayerJoinsLater() {
        Player player1 = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        ResponseEntity<String> startResponse = restTemplate.postForEntity(baseURL + "/player/" + player1.getId() + "/match", null, String.class);
        assertThat(startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));

        Player player2 = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        ResponseEntity<Match> match = restTemplate.postForEntity(baseURL + "/player/" + player2.getId() + "/match", null, Match.class);
        assertThat(match.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void testLogoutDuringMatch() {
        Player player1 = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        ResponseEntity<String> p1startResponse = restTemplate.postForEntity(baseURL + "/player/" + player1.getId() + "/match", null, String.class);
        assertThat(p1startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));
        Player player2 = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        ResponseEntity<Match> p2startResponse = restTemplate.postForEntity(baseURL + "/player/" + player2.getId() + "/match", null, Match.class);
        assertThat(p2startResponse.getStatusCode(), is(HttpStatus.OK));

        restTemplate.delete(baseURL + "/player/" + player1.getId());

        ResponseEntity<Match> endedMatch = restTemplate.getForEntity(baseURL + "/player/" + player2.getId() + "/match", Match.class);
        assertThat(endedMatch.getStatusCode(), is(HttpStatus.OK));
        assertThat(endedMatch.getBody().getState(), is(Match.State.PLAYER_LOGOUT));
    }

    // TODO: [AH] this is too complicated to maintain; look for ways to simplify/clean this, e.g. with helper methods that do the grunt work
    @Test
    public void testFirstMoves() {
        Player player1 = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        ResponseEntity<String> p1startResponse = restTemplate.postForEntity(baseURL + "/player/" + player1.getId() + "/match", null, String.class);
        assertThat(p1startResponse.getStatusCode(), is(HttpStatus.ACCEPTED));
        Player player2 = restTemplate.postForObject(baseURL + "/player", null, Player.class);
        ResponseEntity<Match> p2startResponse = restTemplate.postForEntity(baseURL + "/player/" + player2.getId() + "/match", null, Match.class);
        assertThat(p2startResponse.getStatusCode(), is(HttpStatus.OK));

        Match match = p2startResponse.getBody();
        assertThat(match.getState(), is(Match.State.MOVE_PLAYER_1));
        assertArrayEquals(new int[] {6,6,6,6,6,6,0}, match.getPits()[0]);
        assertArrayEquals(new int[] {6,6,6,6,6,6,0}, match.getPits()[1]);

        match = restTemplate.getForObject(baseURL + "/player/" + player1.getId() + "/match/0", Match.class);
        assertThat(match.getState(), is(Match.State.MOVE_PLAYER_1));
        assertArrayEquals(new int[] {0,7,7,7,7,7,1}, match.getPits()[0]);
        assertArrayEquals(new int[] {6,6,6,6,6,6,0}, match.getPits()[1]);

        match = restTemplate.getForObject(baseURL + "/player/" + player1.getId() + "/match/1", Match.class);
        assertThat(match.getState(), is(Match.State.MOVE_PLAYER_2));
        assertArrayEquals(new int[] {0,0,8,8,8,8,2}, match.getPits()[0]);
        assertArrayEquals(new int[] {6,6,6,6,6,6,0}, match.getPits()[1]);

        try {
            restTemplate.getForObject(baseURL + "/player/" + player1.getId() + "/match/2", Match.class);
            fail();
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        }
    }
}
