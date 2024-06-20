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
package dtu.compute.RoborallyServer.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dtu.compute.RoborallyServer.model.Phase.INITIALISATION;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class Board {

    public final int width;

    public final int height;

    private Integer gameId;

    private final Space[][] spaces;

    private final List<Player> players = new ArrayList<>();

    private Player current;

    private Phase phase = INITIALISATION;
    private Space antenna;
    private Space rebootStation;
    private Heading rebootStationHeading;
    private Heading antennaHeading;

    private int step = 0;

    private boolean stepMode;
    public int checkpoints = 0;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        spaces = new Space[width][height];
        for (int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                Space space = new Space(this, x, y);
                spaces[x][y] = space;
            }
        }
        this.stepMode = false;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        if (this.gameId == null) {
            this.gameId = gameId;
        } else {
            if (!this.gameId.equals(gameId)) {
                throw new IllegalStateException("A game with a set id may not be assigned a new id!");
            }
        }
    }

    public Space getSpace(int x, int y) {
        if (x >= 0 && x < width &&
                y >= 0 && y < height) {
            return spaces[x][y];
        } else {
            return null;
        }
    }

    /**
     * @author Jonathan (s235115)
     * @param space Space used to calculate distance
     * @return Distance
     */
    public double getDistanceToAntenna(Space space) {
        if (antenna == null) {
            return -1;
        }
        int dx = space.x - antenna.x;
        int dy = space.y - antenna.y;
        return Math.abs(dx) + Math.abs(dy);
    }

    /**
     * @author Jonathan (s235115)
     * @param space Space used to calculate angle
     * @return Angle to antenna
     */
    public double getAngleToAntenna(Space space) {
        if (antenna == null) {
            return -1;
        }
        int dx = space.x - antenna.x;
        int dy = space.y - antenna.y;

        // This calculates the angle between the antenna and the space
        double angle = switch (antennaHeading) {
            case NORTH -> Math.atan2(dx, -dy);
            case EAST -> Math.atan2(dy, dx);
            case SOUTH -> Math.atan2(-dx, dy);
            case WEST -> Math.atan2(-dy, -dx);
        };

        // If the angle is negative, add 2 pi to get positive angles to easily compare them
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    public Space getAntenna() {
        return antenna;
    }

    public void setAntenna(int x, int y, Heading heading) {
        antenna = getSpace(x, y);
        antennaHeading = heading;
    }

    public Heading getAntennaHeading() {
        return antennaHeading;
    }

    public Space getRebootStation() {
        return rebootStation;
    }

    public Heading getRebootStationHeading() {
        return rebootStationHeading;
    }

    public void setRebootStation(int x, int y, Heading heading) {
        rebootStation = getSpace(x, y);
        rebootStationHeading = heading;
    }

    public int getPlayersNumber() {
        return players.size();
    }

    public void addPlayer(@NotNull Player player) {
        if (player.board == this && !players.contains(player)) {
            players.add(player);
        }
    }

    public Player getPlayer(int id) {
        if (id >= 0 && id < players.size()) {
            return players.get(id);
        } else {
            return null;
        }
    }

    public Player getCurrentPlayer() {
        return current;
    }

    public void setCurrentPlayer(Player player) {
        if (player != this.current && players.contains(player)) {
            this.current = player;
        }
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        if (phase != this.phase) {
            this.phase = phase;
        }
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        if (step != this.step) {
            this.step = step;
        }
    }

    public boolean isStepMode() {
        return stepMode;
    }

    public void setStepMode(boolean stepMode) {
        if (stepMode != this.stepMode) {
            this.stepMode = stepMode;
        }
    }

    public int getPlayerNumber(@NotNull Player player) {
        if (player.board == this) {
            return players.indexOf(player);
        } else {
            return -1;
        }
    }

    /**
     * Returns the neighbour of the given space of the board in the given heading.
     * The neighbour is returned only, if it can be reached from the given space
     * (no walls or obstacles in either of the involved spaces); otherwise,
     * null will be returned.
     *
     * @param space the space for which the neighbour should be computed
     * @param heading the heading of the neighbour
     * @return the space in the given direction; null if neighbor is off the board; the same space if movement is blocked by wall or antenna
     */
    public Space getNeighbour(@NotNull Space space, @NotNull Heading heading) {
        if (space.getWalls().contains(heading)) {
            return space;
        }
        // TODO needs to be implemented based on the actual spaces
        //      and obstacles and walls placed there. For now it,
        //      just calculates the next space in the respective
        //      direction in a cyclic way.

        // XXX an other option (not for now) would be that null represents a hole
        //     or the edge of the board in which the players can fall

        int x = space.x;
        int y = space.y;
        switch (heading) {
            case SOUTH:
                y++;
                break;
            case WEST:
                x--;
                break;
            case NORTH:
                y--;
                break;
            case EAST:
                x++;
                break;
        }
        Heading reverse = heading.next().next();
        Space result = getSpace(x, y);

        if (result != null) {
            if (result.getWalls().contains(reverse)) {
                return space;
            }
        }
        return result;
    }

    /**
     * LOS looks in a given heading and adds all spaces to a list until it encounters a player, wall
     * or null aka outside the boundary
     * @author Peter (s235069)
     * @param space used where to check
     * @param heading used to find LOS in that direction
     * @return all the spaces until it encounters a player, wall or null including that last space if not null
     */
    public List<Space> getLOS(@NotNull Space space, @NotNull Heading heading, @NotNull List<Space> LOS) {
        Player player = space.getPlayer();
        if (LOS.isEmpty()) {
            LOS.add(space);
        }
        if (player != null || space.getWalls().contains(heading)) {
            return LOS;
        }

        Space neighbour = getNeighbour(space, heading);
        if (space.equals(neighbour) || neighbour == null) {
            return LOS;
        }
        LOS.add(neighbour);

        //Check for player at neighbours neighbours neighbour...
        return getLOS(neighbour, heading, LOS);
    }
}
