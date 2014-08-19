package com.bol.assessment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

// TODO: [AH] a netty + websockets approach would yield lower latency than polling a rest api

@RestController
public class MatchController {

    // TODO: [AH] limit maximum size
    // TODO: [AH] expire old entries (e.g. based on time in UUID or maintain a lastseen timestamp)
    // TODO: [AH] add security against bruteforce guessing of generated IDs

    private final Map<UUID, Match> arena = new HashMap<>();
    private final Map<UUID, Player> lobby = new HashMap<>();

    @RequestMapping(value = "/gamer", method = POST)
    public Player login(@RequestParam(value = "name", required = false, defaultValue = "Anonymous") String name) {
        Player player = new Player(name);
        synchronized (lobby) {
            lobby.put(player.getId(), player);
        }
        return player;
    }

    @RequestMapping(value = "/gamer/{id}", method = DELETE)
    public void logout(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);

        synchronized (lobby) {
            if (lobby.remove(uuid) == null) {
                throw new PlayerNotFoundException();
            }
        }
    }

    @RequestMapping(value = "/gamer/{id}/start", method = GET)
    public Match start(@PathVariable String id) {
        UUID secondId = UUID.fromString(id);
        Player secondPlayer = lobby.get(secondId);

        if (secondPlayer == null) {
            throw new PlayerNotFoundException();
        }

        synchronized (lobby) {
            if (lobby.size() == 1) {
                throw new WaitingForPlayerException();
            }

            lobby.remove(secondId);

            UUID firstId = lobby.keySet().iterator().next();
            Player firstPlayer = lobby.remove(firstId);

            Match match = new Match(firstPlayer, secondPlayer);

            synchronized (arena) {
                arena.put(firstId, match);
                arena.put(secondId, match);
            }

            return match;
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Unknown player ID")
    class PlayerNotFoundException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.ACCEPTED, reason = "Waiting for other players")
    class WaitingForPlayerException extends RuntimeException {
    }
}
