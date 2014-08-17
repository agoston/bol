package com.bol.assessment;

public class GameState {

    private long id;
    private String content;

    public GameState() {
    }

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
