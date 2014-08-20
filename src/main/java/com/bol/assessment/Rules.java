package com.bol.assessment;

import org.springframework.stereotype.Component;

@Component
public class Rules {

    public void apply(Match match, int which, int selectedPit) {
        int[] pits = match.getPits()[which];

        int stones = pits[selectedPit];
        pits[selectedPit] = 0;

        int lastPit = selectedPit + stones;

        for (int i = selectedPit + 1; i <= 6 && i <= lastPit; i++) {
            pits[i]++;
        }

        if (lastPit < 6 && pits[lastPit] == 1) {
            pits[6]++;
            pits[lastPit] = 0;

            int[] otherPits = match.getPits()[1 - which];
            pits[6] += otherPits[lastPit];
            otherPits[lastPit] = 0;
        }

        if (checkIfPitsEmpty(match.getPits())) {
            gameOver(match);
            return;
        }

        if (lastPit != 6) {
            match.nextPlayer();
        }
    }

    private void gameOver(Match match) {
        int[][] pitsAll = match.getPits();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                pitsAll[i][6] += pitsAll[i][j];
            }
        }

        if (pitsAll[0][6] > pitsAll[1][6]) {
            match.setState(Match.State.WON_PLAYER1);
        } else if (pitsAll[0][6] < pitsAll[1][6]) {
            match.setState(Match.State.WON_PLAYER2);
        } else {
            match.setState(Match.State.WON_BOTH);
        }
    }

    private boolean checkIfPitsEmpty(int[][] pitsAll) {
        outer:
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                if (pitsAll[i][j] != 0) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

}
