package dtu.compute.RoborallyServer.controller;

import dtu.compute.RoborallyServer.model.Heading;
import dtu.compute.RoborallyServer.model.Player;
import dtu.compute.RoborallyServer.model.Space;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PushPanel extends FieldAction {
    private Heading heading;
    private PushTime pushTime;
    public static enum PushTime {
        EVEN, ODD
    }

    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player == null) return false;
        if (pushTime == PushTime.EVEN && (space.board.getStep() + 1) % 2 == 0) {  // +1 because step is 0-indexed
            gameController.moveInDirection(player, heading, true);
        } else if (pushTime == PushTime.ODD && (space.board.getStep() + 1) % 2 == 1) {  // +1 because step is 0-indexed
            gameController.moveInDirection(player, heading, true);
        }
        return true;
    }
}
