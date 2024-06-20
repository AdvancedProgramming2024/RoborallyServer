package dtu.compute.RoborallyServer.roborally.model;

import dtu.compute.RoborallyServer.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlayerTest {

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void shuffleDrawPile() {
        Board board = new Board(8, 8);
        Player player = new Player(board, "red", "Player 2", 0);

        int[] sumValues = new int[20];
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < 20; j++) {
                CommandCard card = player.drawCommandCard();
                sumValues[j] += card.command.ordinal();
                player.discardCommandCard(card);
            }
        }
        for (int i = 0; i < 20; i++) {
            // The average ordinal value of the default 20 cards in the draw pile is 2.95
            Assertions.assertTrue((double) sumValues[i] / 10000 > 2.85, "The sum of the command card values should be between 2.85 and 3.05");
            Assertions.assertTrue((double) sumValues[i] / 10000 < 3.05, "The sum of the command card values should be between 2.85 and 3.05");
        }
    }
}