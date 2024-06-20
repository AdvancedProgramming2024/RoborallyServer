package dtu.compute.RoborallyServer.controller;

import dtu.compute.RoborallyServer.model.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class CommandCardController {

    private Command currentCommand = null;

    /**
     * Execute command boolean.
     *
     * @author Jonathan (s235115)
     * @param gameController of the current game
     * @param player who is executing the command
     * @param command to be executed
     * @return whether the command was executed (true) or a new card needs to be drawn (false)
     */
    public boolean executeCommand(GameController gameController, @NotNull Player player, Command command) {
        currentCommand = command;
        if (player.board == gameController.board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).

            switch (command) {
                case MOVE_1:
                    gameController.moveForward(player);
                    break;
                case MOVE_2:
                    for (int i = 0; i < 2; i++) {
                        if (player.isRebooting()) break;
                        gameController.moveForward(player);
                    }
                    break;
                case MOVE_3:
                    for (int i = 0; i < 3; i++) {
                        if (player.isRebooting()) break;
                        gameController.moveForward(player);
                    }
                    break;
                case RIGHT:
                    gameController.turn(player, 1);
                    break;
                case LEFT:
                    gameController.turn(player, 3);
                    break;
                case U_TURN:
                    gameController.turn(player, 2);
                    break;
                case MOVE_BACK:
                    gameController.moveInDirection(player, player.getHeading().next().next(), true);
                    break;
                case POWER_UP:
                    player.addEnergyCubes(1);
                    break;
                case AGAIN:
                    int i = gameController.board.getStep()-1;
                    if (i < 0 ) break;
                    Command c = gameController.board.getCurrentPlayer().
                            getProgramField(i).getCard().command;
                    executeCommand(gameController, player, c);
                    break;
                case SPAM:
                    return false;
                case VIRUS:
                    Board board = gameController.board;
                    for (int j = 0; j < board.getPlayersNumber(); j++) {
                        int distance = Math.abs(player.getSpace().x - board.getPlayer(j).getSpace().x) +
                                Math.abs(player.getSpace().y - board.getPlayer(j).getSpace().y);
                        if (board.getPlayer(j) != player && distance <= 6) {
                            board.getPlayer(j).addCommandCard(new CommandCard(Command.VIRUS));
                        }
                    }
                    return false;
                case TROJAN_HORSE:
                    gameController.board.getCurrentPlayer().addCommandCard(new CommandCard(Command.SPAM));
                    gameController.board.getCurrentPlayer().addCommandCard(new CommandCard(Command.SPAM));
                    return false;
                case WORM:
                    gameController.board.getCurrentPlayer().reboot(gameController);
                    return false;
                case ENERGY_ROUTINE:
                    player.addEnergyCubes(1);
                    break;
                case SANDBOX_ROUTINE, WEASEL_ROUTINE:
                    gameController.board.setPhase(Phase.PLAYER_INTERACTION);
                    break;
                case SPEED_ROUTINE:
                    executeCommand(gameController, player, Command.MOVE_3);
                    break;
                case SPAM_FOLDER:
                    player.removeFromDiscardPile(Command.SPAM);
                    break;
                case REPEAT_ROUTINE:
                    executeCommand(gameController, player, Command.AGAIN);
                    break;
                default:
                    // DO NOTHING (for now)
            }
        }
        return true;
    }
}
