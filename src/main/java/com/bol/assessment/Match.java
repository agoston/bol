package com.bol.assessment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public class Match {

    public enum State {MOVE_PLAYER_1, MOVE_PLAYER_2, PLAYER_LOGOUT, WON_PLAYER1, WON_PLAYER2, WON_BOTH}

    @JsonIgnore
    private Player[] players;

    private String[] names;
    private int[][] pits = new int[2][7];
    private State state;

    // for serialization
    public Match() {}

    public Match(Player... players) {
        this.players = players;
        this.names = new String[]{
                players[0].getName(),
                players[1].getName()
        };
    }

    public int[][] getPits() {
        return pits;
    }

    public void setPits(int[][] pits) {
        this.pits = pits;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String[] getNames() {
        return names;
    }

    public int whichPlayer(UUID id) {
        if (players[0].getId().equals(id)) {
            return 0;
        } else if (players[1].getId().equals(id)) {
            return 1;
        }
        throw new IllegalArgumentException("No such player in match: " + id);
    }

    public void nextPlayer() {
        if (state == State.MOVE_PLAYER_1) {
            state = State.MOVE_PLAYER_2;
        } else if (state == State.MOVE_PLAYER_2) {
            state = State.MOVE_PLAYER_1;
        }
    }
}
