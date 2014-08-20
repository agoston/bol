package com.bol.assessment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

// TODO: [AH] a netty + websockets approach would yield lower latency than polling a rest api

@RestController
public class MatchController {

    // TODO: [AH] limit maximum size
    // TODO: [AH] expire old entries (e.g. based on time in UUID or maintain a lastseen timestamp)
    // TODO: [AH] add security against bruteforce guessing of generated IDs

    private final Map<UUID, Match> arena = new ConcurrentHashMap<>();
    private final AtomicReference<Player> waiting = new AtomicReference<>(null);
    private final Map<UUID, Player> lobby = new ConcurrentHashMap<>();

    @RequestMapping(value = "/player", method = POST)
    public Player login(@RequestParam(value = "name", required = false, defaultValue = "Anonymous") String name) {
        Player player = new Player(name);
        lobby.put(player.getId(), player);
        return player;
    }

    @RequestMapping(value = "/player/{id}", method = GET)
    public Player getPlayer(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Player player = lobby.get(uuid);

        if (player == null) {
            throw new PlayerNotFoundException();
        }
        return player;
    }

    @RequestMapping(value = "/player/{id}", method = DELETE)
    public void logout(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);

        Player player = lobby.remove(uuid);
        if (player == null) {
            throw new PlayerNotFoundException();
        }

        waiting.compareAndSet(player, null);

        Match match = arena.remove(uuid);
        if (match != null) {
            match.setState(Match.State.PLAYER_LOGOUT);
        }
    }

    @RequestMapping(value = "/player/{id}/match", method = GET)
    public Match start(@PathVariable String id) {
        Player secondPlayer = getPlayer(id);

        Player firstPlayer = waiting.getAndUpdate(player -> player == null || player.equals(secondPlayer) ? secondPlayer : null);

        if (firstPlayer == null) {
            throw new WaitingForPlayerException();
        }

        Match match = new Match(firstPlayer, secondPlayer);
        arena.put(firstPlayer.getId(), match);
        arena.put(secondPlayer.getId(), match);
        return match;
    }

    @RequestMapping(value = "/player/{id}/match/{pit}", method = GET)
    public Match move(@PathVariable String id, @PathVariable int pit) {
        Player player = getPlayer(id);
        Match match = arena.get(player.getId());
        if (match == null) {
            throw new PlayerNotInAMatchException();
        }

        applyRules(match.whichPlayer(player.getId()), match, pit);

        return match;
    }

    private void applyRules(int i, Match match, int pit) {

    }


    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Unknown player ID")
    class PlayerNotFoundException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Player not in a match")
    class PlayerNotInAMatchException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.ACCEPTED, reason = "Waiting for other players")
    class WaitingForPlayerException extends RuntimeException {
    }
}
