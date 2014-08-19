package com.bol.assessment;

public class Match {

//    private transient Player[] players = new Player[2];
    private int[][] pits = new int[2][7];

    // for serialization
    public Match() {}

//    public Match(Player p1, Player p2) {
//    }


    public int[][] getPits() {
        return pits;
    }

    public void setPits(int[][] pits) {
        this.pits = pits;
    }
}
