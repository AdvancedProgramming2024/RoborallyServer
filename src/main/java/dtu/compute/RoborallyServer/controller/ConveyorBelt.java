/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dtu.compute.RoborallyServer.controller;

import dtu.compute.RoborallyServer.model.Heading;
import dtu.compute.RoborallyServer.model.Player;
import dtu.compute.RoborallyServer.model.Space;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static dtu.compute.RoborallyServer.model.Heading.WEST;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
@Getter
@Setter
public class ConveyorBelt extends FieldAction {

    private Heading heading;
    private Heading turn = null;

    private Heading cross = null;
    private Heading tea = null;
    private int belt = 1;


    //If heading turn = west then turn left, east turn right
    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        boolean turned = false;
        Player player = space.getPlayer();
        Heading optional = null;
        boolean second = false;
        if (player == null) return false; // TODO: Remove if doAction is only called when a player is on a conveyor belt
        //if (player.isConveyorPush()) return false;

        //Check neighbour to see what the fuck to do
        Space neighbour = space.board.getNeighbour(space,heading);

        //This is a suprise tool that will help us later.
        if (neighbour != null) {
            for (FieldAction action : neighbour.getActions()) {
                if (action instanceof ConveyorBelt) {
                   optional = ((ConveyorBelt) action).getHeading();
                   second = true;
                }
            }
        }


        //If neighbour is null or wall, move one forward. wall should block regardless.
        if (neighbour == null) {
            gameController.moveInDirection(player, heading, false);
            return false;
        }
        //Landing on a turningBelt, well, turns you
        for (int i = 0; i < belt; i++) {
            if (i == 1 && turned)  {
                gameController.moveInDirection(player, optional, false);
            } else {
                if (i == 1 && !second) {
                    return false;
                }
                gameController.moveInDirection(player, heading, false);
                turned = turningBelt(player.getSpace(), heading);
            }
        }


        return false;
    }

    /**
     * Check if a given space is a conveyorBelt with a turn on it, turns the player if necessary
     * @author Peter (s235069)
     * @param space used where to check
     * @param heading1 used to see if prior heading is different, if it is then turn player
     */
    private boolean turningBelt(Space space, Heading heading1) {
        for (FieldAction action : space.getActions()) {
            if (action instanceof ConveyorBelt && ((ConveyorBelt) action).getHeading() != heading1) {
                if (((ConveyorBelt) action).getTurn() == WEST) {
                    //If turn is not applied on the json, it always turns players right
                    space.getPlayer().setHeading(space.getPlayer().getHeading().prev());
                    return true;
                } else {
                    space.getPlayer().setHeading(space.getPlayer().getHeading().next());
                    return true;
                }
            }
        }
        return false;
    }

}
