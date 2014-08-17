package com.bol.assessment;

public class GameState {

    private final long id;
    private final String content;

    public GameState(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
