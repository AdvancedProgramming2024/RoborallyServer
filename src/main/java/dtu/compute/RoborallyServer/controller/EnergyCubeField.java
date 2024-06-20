package dtu.compute.RoborallyServer.controller;

import dtu.compute.RoborallyServer.model.Player;
import dtu.compute.RoborallyServer.model.Space;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class EnergyCubeField extends FieldAction {
    @Setter
    @Getter
    private int energyCubes = 1;


    /**
     * Picks up an energy cube and adds it to the player's energy bank.
     * @author Oscar (224752)
     */
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player player = space.getPlayer();
        if (player == null) return false;

        if (energyCubes != 1) {
            return false;
        } else {
            player.addEnergyCubes(1);
            if (gameController.board.getStep() % 4 != 0 || gameController.board.getStep() == 0) {
                energyCubes--;
            }
            return true;
        }
    }
}
