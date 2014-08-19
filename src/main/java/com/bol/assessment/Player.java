package com.bol.assessment;

import java.util.UUID;

public class Player extends MatchPlayer {
    private final UUID id;

    public Player() {
        this("Anonymous");
    }

    public Player(String name) {
        super(name);
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;

        Player player = (Player) o;

        if (!id.equals(player.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
