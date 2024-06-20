package dtu.compute.RoborallyServer.roborally.controller;

import dtu.compute.RoborallyServer.controller.GameController;
import dtu.compute.RoborallyServer.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameControllerTest {

    private final int TEST_WIDTH = 8;
    private final int TEST_HEIGHT = 8;

    private GameController gameController;

    @BeforeEach
    void setUp() {
        Board board = new Board(TEST_WIDTH, TEST_HEIGHT);
        gameController = new GameController(board, null);
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null,"Player " + i, i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @AfterEach
    void tearDown() {
        gameController = null;
    }

    @Test
    void moveForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.moveForward(current);

        Assertions.assertEquals(current, board.getSpace(0, 1).getPlayer(), "Player " + current.getName() + " should beSpace (0,1)!");
        Assertions.assertEquals(Heading.SOUTH, current.getHeading(), "Player 0 should be heading SOUTH!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void turnRight() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.turn(current, 1);

        Assertions.assertEquals(current, board.getSpace(0, 0).getPlayer(), "Player " + current.getName() + " should beSpace (0,0)!");
        Assertions.assertEquals(Heading.WEST, current.getHeading(), "Player 0 should be heading WEST!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void turnLeft() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.turn(current, 3);

        Assertions.assertEquals(current, board.getSpace(0, 0).getPlayer(), "Player " + current.getName() + " should beSpace (0,0)!");
        Assertions.assertEquals(Heading.EAST, current.getHeading(), "Player 0 should be heading EAST!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void determinePlayerOrder() {
        Board board = gameController.board;
        board.setAntenna(0, 3, Heading.EAST);

        gameController.determinePlayerOrder();

        Assertions.assertEquals(board.getPlayer(3), gameController.getPlayerOrder().get(0), "1. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(0), gameController.getPlayerOrder().get(1), "2. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(1), gameController.getPlayerOrder().get(2), "3. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(2), gameController.getPlayerOrder().get(3), "4. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(4), gameController.getPlayerOrder().get(4), "5. player should be " + board.getPlayer(2).getName());
        Assertions.assertEquals(board.getPlayer(5), gameController.getPlayerOrder().get(5), "6. player should be " + board.getPlayer(2).getName());
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void fallInVoid() {
        Board board = gameController.board;
        board.setRebootStation(0, 3, Heading.EAST);
        Player current = board.getCurrentPlayer();

        gameController.moveInDirection(current, Heading.NORTH, true);

        Assertions.assertEquals(current.getSpace(), board.getSpace(0, 3), "Player 0 should be at the reboot station!");
        Assertions.assertTrue(current.isRebooting(), "Player 0 should be rebooting!");
    }

    /**
     * @author Jonathan (s235115)
     */
    @Test
    void multipleFallInVoid() {
        Board board = gameController.board;
        board.setRebootStation(0, 3, Heading.EAST);
        Player player1 = board.getPlayer(0);
        Player player2 = board.getPlayer(1);

        gameController.moveInDirection(player1, Heading.NORTH, true);

        Assertions.assertEquals(player1.getSpace(), board.getSpace(0, 3), "Player 0 should be at the reboot station!");
        Assertions.assertTrue(player1.isRebooting(), "Player 0 should be rebooting!");

        gameController.moveInDirection(player2, Heading.NORTH, true);
        gameController.moveInDirection(player2, Heading.NORTH, true);

        Assertions.assertTrue(player1.isRebooting(), "Player 0 should be rebooting!");
        Assertions.assertTrue(player2.isRebooting(), "Player 1 should be rebooting!");
        Assertions.assertEquals(player1.getSpace(), board.getSpace(1, 3), "Player 0 should be beside the reboot station!");
        Assertions.assertEquals(player2.getSpace(), board.getSpace(0, 3), "Player 1 should be at the reboot station!");
    }


}