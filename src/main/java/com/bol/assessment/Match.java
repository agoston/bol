package com.bol.assessment;

public class Match {

    public enum State {MOVE_PLAYER_1, MOVE_PLAYER_2, PLAYER_LOGOUT, FINISHED};

    private MatchPlayer[] players;
    private int[][] pits = new int[2][7];
    private State state;

    // for serialization
    public Match() {
    }

    public Match(Player... players) {
        this.players = new MatchPlayer[] {
                new MatchPlayer(players[0].getName()),
                new MatchPlayer(players[1].getName()),
        };
    }

    public int[][] getPits() {
        return pits;
    }

    public void setPits(int[][] pits) {
        this.pits = pits;
    }

    public MatchPlayer[] getPlayers() {
        return players;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
