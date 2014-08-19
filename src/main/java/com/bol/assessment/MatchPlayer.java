package com.bol.assessment;

public class MatchPlayer {
    private final String name;

    public MatchPlayer() {
        this("Anonymous");
    }

    public MatchPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
