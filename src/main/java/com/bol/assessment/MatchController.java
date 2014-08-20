package com.bol.assessment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.bol.assessment.Match.State;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

// TODO: [AH] a netty + websockets approach would yield lower latency than polling a rest api

@RestController
public class MatchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchController.class);

    private static final EnumSet<State> MATCH_ONGOING = EnumSet.of(State.MOVE_PLAYER_1, State.MOVE_PLAYER_2);

    // TODO: [AH] limit maximum size
    // TODO: [AH] expire old entries (e.g. based on time in UUID or maintain a lastseen timestamp)
    // TODO: [AH] add security against bruteforce guessing of generated IDs

    private final Map<UUID, Match> arena = new ConcurrentHashMap<>();
    private final AtomicReference<Player> waiting = new AtomicReference<>(null);
    private final Map<UUID, Player> lobby = new ConcurrentHashMap<>();

    @Autowired
    Rules rules;

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
            match.setState(State.PLAYER_LOGOUT);
        }
    }

    @RequestMapping(value = "/player/{id}/match", method = GET)
    public Match getMatch(@PathVariable String id) {
        Player player = getPlayer(id);
        Match match = arena.get(player.getId());
        if (match == null) {
            throw new PlayerNotInAMatchException();
        }

        return match;
    }

    @RequestMapping(value = "/player/{id}/match", method = POST)
    public Match create(@PathVariable String id) {
        Player secondPlayer = getPlayer(id);

        // return existing match if still ongoing; wipe from arena if not
        Match match = arena.get(secondPlayer.getId());
        if (match != null) {
            if (MATCH_ONGOING.contains(match.getState())) {
                return match;
            } else {
                arena.remove(secondPlayer.getId());
            }
        }

        Player firstPlayer = waiting.getAndUpdate(player -> player == null || player.equals(secondPlayer) ? secondPlayer : null);

        if (firstPlayer == null || firstPlayer.equals(secondPlayer)) {
            throw new WaitingForPlayerException();
        }

        LOGGER.info("Match added: " + firstPlayer + " vs. " + secondPlayer);

        Match newMatch = new Match(firstPlayer, secondPlayer);
        arena.put(firstPlayer.getId(), newMatch);
        arena.put(secondPlayer.getId(), newMatch);
        return newMatch;
    }

    @RequestMapping(value = "/player/{id}/match/{pit}", method = GET)
    public Match move(@PathVariable String id, @PathVariable int pit) {
        Player player = getPlayer(id);
        Match match = arena.get(player.getId());
        if (match == null) {
            throw new PlayerNotInAMatchException();
        }

        if (pit < 0 || pit > 5) {
            throw new PlayerChoseIncorrectPitException();
        }

        if (!MATCH_ONGOING.contains(match.getState())) {
            throw new MatchOverException();
        }

        int whichPlayer = match.whichPlayer(player.getId());
        if (match.getState() == State.MOVE_PLAYER_1 && whichPlayer != 0 ||
                match.getState() == State.MOVE_PLAYER_2 && whichPlayer != 1) {
            throw new NotYourTurnException();
        }

        if (match.getPits()[whichPlayer][pit] == 0) {
            throw new PlayerChoseIncorrectPitException();
        }

        rules.apply(match, whichPlayer, pit);

        return match;
    }


    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Unknown player ID")
    class PlayerNotFoundException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Player not in a match")
    class PlayerNotInAMatchException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Match over")
    class MatchOverException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Incorrect pit")
    class PlayerChoseIncorrectPitException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Not your turn")
    class NotYourTurnException extends RuntimeException {
    }

    @ResponseStatus(value = HttpStatus.ACCEPTED, reason = "Waiting for other players")
    class WaitingForPlayerException extends RuntimeException {
    }
}
