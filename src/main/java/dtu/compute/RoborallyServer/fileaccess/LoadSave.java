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
package dtu.compute.RoborallyServer.fileaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dtu.compute.RoborallyServer.RoboRallyServer;
import dtu.compute.RoborallyServer.controller.*;
import dtu.compute.RoborallyServer.fileaccess.model.BoardTemplate;
import dtu.compute.RoborallyServer.fileaccess.model.GameTemplate;
import dtu.compute.RoborallyServer.fileaccess.model.PlayerTemplate;
import dtu.compute.RoborallyServer.fileaccess.model.SpaceTemplate;
import dtu.compute.RoborallyServer.model.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class LoadSave {

    private static final String BOARDSFOLDER = "boards";
    public static final String GAMESFOLDER = "games";
    private static final String DEFAULTBOARD = "defaultboard";
    private static final String JSON_EXT = "json";

    public static Board loadBoard(String boardname) {
        if (boardname == null) {
            boardname = DEFAULTBOARD;
        }

        ClassLoader classLoader = LoadSave.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(BOARDSFOLDER + "/" + boardname + "." + JSON_EXT);
        if (inputStream == null) {
            // TODO these constants should be defined somewhere
            return new Board(8,8);
        }

		// In simple cases, we can create a Gson object with new Gson():
        GsonBuilder simpleBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>());
        Gson gson = simpleBuilder.create();

		Board result;
		// FileReader fileReader = null;
        JsonReader reader = null;
		try {
			// fileReader = new FileReader(filename);
			reader = gson.newJsonReader(new InputStreamReader(inputStream));
			BoardTemplate template = gson.fromJson(reader, BoardTemplate.class);

			result = new Board(template.width, template.height);

            result.setAntenna(template.antennaX, template.antennaY, Heading.values()[template.antennaHeading]);
            result.setRebootStation(template.rebootStationX, template.rebootStationY, Heading.values()[template.rebootStationHeading]);

			for (SpaceTemplate spaceTemplate: template.spaces) {
			    Space space = result.getSpace(spaceTemplate.x, spaceTemplate.y);
			    if (space != null) {
                    spaceTemplate.actions.forEach(space::addAction);
                    spaceTemplate.walls.forEach(space::addWall);
                    space.setPit(spaceTemplate.isPit);
                    if (spaceTemplate.startField) {
                        result.addStartField(space);
                    }
                }
            }
			reader.close();
			return result;
		} catch (IOException e1) {
            if (reader != null) {
                try {
                    reader.close();
                    inputStream = null;
                } catch (IOException e2) {}
            }
            if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e2) {}
			}
		}
		return null;
    }

    /**
     * Load the game state from a file with the given name. If the file does not exist, null is returned.
     * @author Jonathan (s235115)
     * @param gameState of the game you want to load
     */
    public static GameController loadGameState(GameTemplate gameState, RoboRallyServer server) {
        Board board = new Board(gameState.board.width, gameState.board.height);

        GameController gameController = new GameController(board, server);
        server.setGameController(gameController);
        gameController.commandCardController.setCurrentCommand(gameState.currentCommand != -1 ? Command.values()[gameState.currentCommand] : null);

        board.setGameId(gameState.gameId);

        board.setAntenna(gameState.board.antennaX, gameState.board.antennaY, Heading.values()[gameState.board.antennaHeading]);
        board.setRebootStation(gameState.board.rebootStationX, gameState.board.rebootStationY, Heading.values()[gameState.board.rebootStationHeading]);

        for (int i=0; i<gameState.upgradeShop.size(); i++) {
            gameController.getUpgradeShop()[i].setCard(gameState.upgradeShop.get(i) == null ?
                    null : new UpgradeCard(Upgrade.values()[gameState.upgradeShop.get(i)]));
        }

        for (SpaceTemplate spaceTemplate: gameState.board.spaces) {
            Space space = board.getSpace(spaceTemplate.x, spaceTemplate.y);
            if (space != null) {
                space.getActions().addAll(spaceTemplate.actions);
                space.getWalls().addAll(spaceTemplate.walls);
                space.setPit(spaceTemplate.isPit);
                if (spaceTemplate.startField) {
                    board.addStartField(space);
                }
            }
        }

        for (PlayerTemplate playerTemplate: gameState.players) {
            Player player = new Player(board, playerTemplate.color, playerTemplate.name, playerTemplate.id);
            player.setSpace(board.getSpace(playerTemplate.xPosition, playerTemplate.yPosition));
            board.getSpace(playerTemplate.xPosition, playerTemplate.yPosition).setPlayer(player);
            player.setHeading(Heading.values()[playerTemplate.heading]);

            for (int card : playerTemplate.drawPile) {
                player.getDrawPile().add(new CommandCard(Command.values()[card]));
            }
            for (int card : playerTemplate.discardPile) {
                player.getDiscardPile().add(new CommandCard(Command.values()[card]));
            }
            for (int i=0; i<playerTemplate.program.length; i++) {
                player.getProgram()[i] = new CommandCardField(player);
                if (playerTemplate.program[i] != -1)
                    player.getProgram()[i].setCard(new CommandCard(Command.values()[playerTemplate.program[i]]));
            }
            for (int i=0; i<playerTemplate.hand.length; i++) {
                player.getCards()[i] = new CommandCardField(player);
                if (playerTemplate.hand[i] != -1)
                    player.getCards()[i].setCard(new CommandCard(Command.values()[playerTemplate.hand[i]]));
            }
            for (int i=0; i<playerTemplate.permanent.length; i++) {
                player.getPermanentUpgrades()[i] = new UpgradeCardField();
                if (playerTemplate.permanent[i] != -1) {
                    player.getPermanentUpgrades()[i].setCard(new UpgradeCard(Upgrade.values()[playerTemplate.permanent[i]]));
                    player.getPermanentUpgrades()[i].getCard().setActive(playerTemplate.permanentActive[i]);
                }
            }
            for (int i=0; i<playerTemplate.temporary.length; i++) {
                player.getTemporaryUpgrades()[i] = new UpgradeCardField();
                if (playerTemplate.temporary[i] != -1) {
                    player.getTemporaryUpgrades()[i].setCard(new UpgradeCard(Upgrade.values()[playerTemplate.temporary[i]]));
                    player.getTemporaryUpgrades()[i].getCard().setActive(playerTemplate.temporaryActive[i]);
                }
            }

            player.setCheckpoints(playerTemplate.checkpoints);
            player.setEnergyCubes(playerTemplate.energyBank);
            player.setRebooting(playerTemplate.rebooting);
            board.addPlayer(player);
        }

        board.setCurrentPlayer(board.getPlayer(gameState.currentPlayer));
        List<Player> playerOrder = new ArrayList<>();
        for (int i : gameState.playerOrder) {
            playerOrder.add(board.getPlayer(i));
        }
        gameController.setPlayerOrder(playerOrder);
        board.setPhase(Phase.values()[gameState.playPhase]);
        board.setStep(gameState.step);

        return gameController;
    }

    /**
     * Save the current state of the game to a file with the given name.
     * @author Jonathan (s235115)
     * @param gameController of the current game
     * @return GameTemplate of the current game state
     */
    public static GameTemplate saveGameState(GameController gameController, boolean asSaveFile) {
        GameTemplate gameTemplate = new GameTemplate();
        gameTemplate.gameId = gameController.board.getGameId();
        gameTemplate.board = createBoardTemplate(gameController.board);
        Command currentCommand = gameController.commandCardController.getCurrentCommand();
        gameTemplate.currentCommand = currentCommand == null ? -1 : currentCommand.ordinal();
        gameTemplate.timeStamp = new Timestamp(System.currentTimeMillis()).toString();

        for (UpgradeCardField field : gameController.getUpgradeShop()) {
            UpgradeCard upgradeCard = field.getCard();
            gameTemplate.upgradeShop.add(upgradeCard != null ? upgradeCard.upgrade.ordinal() : -1);
        }

        for (int i = 0; i < gameController.board.getPlayersNumber(); i++) {
            PlayerTemplate playerTemplate = new PlayerTemplate();
            Player player = gameController.board.getPlayer(i);
            playerTemplate.id = player.getId();
            playerTemplate.name = player.getName();
            playerTemplate.color = player.getColor();
            playerTemplate.xPosition = player.getSpace().x;
            playerTemplate.yPosition = player.getSpace().y;
            playerTemplate.heading = player.getHeading().ordinal();

            if (asSaveFile) {
                for (CommandCard card : player.getDrawPile()) {
                    playerTemplate.drawPile.add(card.command.ordinal());
                }
                for (CommandCard card : player.getDiscardPile()) {
                    playerTemplate.discardPile.add(card.command.ordinal());
                }
            }
            for (int j = 0; j < player.getProgram().length; j++) {
                CommandCardField field = player.getProgram()[j];
                boolean ignore = !asSaveFile && !field.isVisible();
                playerTemplate.program[j] = (field.getCard() == null || ignore) ? -1 : field.getCard().command.ordinal();
            }
            for (int j = 0; j < player.getCards().length; j++) {
                CommandCardField field = player.getCards()[j];
                boolean ignore = !asSaveFile && !field.isVisible();
                playerTemplate.hand[j] = (field.getCard() == null || ignore) ? -1 : field.getCard().command.ordinal();
            }
            for (int j = 0; j < player.getPermanentUpgrades().length; j++) {
                UpgradeCardField field = player.getPermanentUpgrades()[j];
                playerTemplate.permanent[j] = (field.getCard() == null) ? -1 : field.getCard().upgrade.ordinal();
                playerTemplate.permanentActive[j] = field.getCard() != null && field.getCard().isActive();
            }
            for (int j = 0; j < player.getTemporaryUpgrades().length; j++) {
                UpgradeCardField field = player.getTemporaryUpgrades()[j];
                playerTemplate.temporary[j] = (field.getCard() == null) ? -1 : field.getCard().upgrade.ordinal();
                playerTemplate.temporaryActive[j] = field.getCard() != null && field.getCard().isActive();
            }
            playerTemplate.checkpoints = player.getCheckpoints();
            playerTemplate.energyBank = player.getEnergyCubes();
            playerTemplate.rebooting = player.isRebooting();

            gameTemplate.players.add(playerTemplate);
        }

        gameTemplate.currentPlayer = gameController.board.getCurrentPlayer().getId();

        gameController.getPlayerOrder().forEach(player -> gameTemplate.playerOrder.add(player.getId()));

        gameTemplate.playPhase = gameController.board.getPhase().ordinal();
        gameTemplate.step = gameController.board.getStep();
        gameTemplate.winnerName = gameController.getWinner() != null ? gameController.getWinner().getName() : null;

        return gameTemplate;
    }

    private static BoardTemplate createBoardTemplate(Board board) {
        BoardTemplate template = new BoardTemplate();
        template.width = board.width;
        template.height = board.height;

        template.antennaX = board.getAntenna().x;
        template.antennaY = board.getAntenna().y;
        template.antennaHeading = board.getAntennaHeading().ordinal();
        template.rebootStationX = board.getRebootStation().x;
        template.rebootStationY = board.getRebootStation().y;
        template.rebootStationHeading = board.getRebootStationHeading().ordinal();

        for (int i=0; i<board.width; i++) {
            for (int j=0; j<board.height; j++) {
                template.spaces.add(createSpaceTemplate(board.getSpace(i,j), board));
            }
        }
        return template;
    }

    public static SpaceTemplate createSpaceTemplate(Space space, Board board) {
        SpaceTemplate spaceTemplate = new SpaceTemplate();
        spaceTemplate.x = space.x;
        spaceTemplate.y = space.y;
        spaceTemplate.actions.addAll(space.getActions());
        spaceTemplate.walls.addAll(space.getWalls());
        spaceTemplate.isPit = space.isPit();
        spaceTemplate.player = space.getPlayer() == null ? -1 : board.getPlayerNumber(space.getPlayer());
        spaceTemplate.startField = board.getStartFields().contains(space);
        return spaceTemplate;
    }
/*
    private static FieldAction createFieldAction(FieldActionTemplate template) {
        if (template instanceof CheckpointTemplate checkpointTemplate) {
            return new Checkpoint(checkpointTemplate.id);
        } else if (template instanceof ConveyorBeltTemplate beltTemplate) {
            ConveyorBelt belt = new ConveyorBelt();
            belt.setBelt(beltTemplate.belt);
            belt.setTurn(beltTemplate.turn);
            belt.setHeading(beltTemplate.heading);
            belt.setCross(beltTemplate.cross);
            belt.setTea(beltTemplate.tea);
            return belt;
        } else if (template instanceof EnergyCubeFieldTemplate) {
            EnergyCubeField field = new EnergyCubeField();
            field.setEnergyCubes(((EnergyCubeFieldTemplate) template).energyCubes);
            return field;
        } else if (template instanceof GearTemplate) {
            Gear gear = new Gear();
            gear.setHeading(((GearTemplate) template).heading);
            return gear;
        } else if (template instanceof LaserTemplate) {
            Laser laser = new Laser();
            laser.setHeading(((LaserTemplate) template).heading);
            laser.setLazer(((LaserTemplate) template).lazer);
            return laser;
        } else if (template instanceof PushPanelTemplate) {
            PushPanel pushPanel = new PushPanel();
            pushPanel.setHeading(((PushPanelTemplate) template).heading);
            pushPanel.setPushTime(PushPanel.PushTime.values()[((PushPanelTemplate) template).pushTime]);
            return pushPanel;
        }
        return null;
    }

    public static FieldActionTemplate createFieldActionTemplate(FieldAction action) {
        if (action instanceof Checkpoint checkpoint) {
            CheckpointTemplate template = new CheckpointTemplate();
            template.id = checkpoint.getId();
            return template;
        } else if (action instanceof ConveyorBelt belt) {
            ConveyorBeltTemplate template = new ConveyorBeltTemplate();
            template.belt = belt.getBelt();
            template.turn = belt.getTurn();
            template.heading = belt.getHeading();
            template.cross = belt.getCross();
            template.tea = belt.getTea();
            return template;
        } else if (action instanceof EnergyCubeField field) {
            EnergyCubeFieldTemplate template = new EnergyCubeFieldTemplate();
            template.energyCubes = field.getEnergyCubes();
            return template;
        } else if (action instanceof Gear gear) {
            GearTemplate template = new GearTemplate();
            template.heading = gear.getHeading();
            return template;
        } else if (action instanceof Laser laser) {
            LaserTemplate template = new LaserTemplate();
            template.heading = laser.getHeading();
            template.lazer = laser.getLazer();
            return template;
        } else if (action instanceof PushPanel pushPanel) {
            PushPanelTemplate template = new PushPanelTemplate();
            template.heading = pushPanel.getHeading();
            template.pushTime = pushPanel.getPushTime().ordinal();
            return template;
        }
        return null;
    }*/
}
