package com.bol.assessment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

@RestController
public class MatchController {
    private final HashMap<Player, Match> arena = new HashMap<>();
    private final HashMap<UUID, Player> lobby = new HashMap<>();

    @RequestMapping("/login")
    public Player login(@RequestParam(value = "name", required = false, defaultValue = "Anonymous") String name) {
        Player player = new Player(name);
        synchronized (lobby) {
            // TODO: limit maximum size
            // TODO: expire old entries based on time in UUID
            lobby.put(player.getId(), player);
        }
        return player;
    }

    @RequestMapping("/start/{id}")
    public Match start(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        Player player = lobby.get(uuid);

        if (player == null) {
            throw new PlayerNotFoundException();
        }

        Player other;

        synchronized (lobby) {
            if (lobby.size() == 1) {
                throw new WaitingForPlayerException();
            }

            lobby.remove(player.getId());

            other = lobby.values().iterator().next();
            lobby.remove(other.getId());
        }

        Match match = new Match();//other, player);

        synchronized (arena) {
            arena.put(player, match);
            arena.put(other, match);
        }

        return match;
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Unknown player ID")
    class PlayerNotFoundException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.ACCEPTED, reason = "Waiting for other players")
    class WaitingForPlayerException extends RuntimeException {
    }

}