package dtu.compute.RoborallyServer.controller;

import dtu.compute.RoborallyServer.model.Heading;
import dtu.compute.RoborallyServer.model.Player;
import dtu.compute.RoborallyServer.model.Space;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class Gear extends FieldAction {
    private Heading heading;

    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player == null) return false;

        //If heading of gear is west, player turns counterclockwise
        if (heading == Heading.WEST) {
            player.setHeading(player.getHeading().prev());
        } else {                                            //If any other heading, turn clockwise
            player.setHeading(player.getHeading().next());
        }

        return true;
    }
}

