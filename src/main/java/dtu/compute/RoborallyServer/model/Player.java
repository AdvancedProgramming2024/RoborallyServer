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

import dtu.compute.RoborallyServer.RoboRallyServer;
import dtu.compute.RoborallyServer.controller.GameController;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dtu.compute.RoborallyServer.model.Command.*;
import static dtu.compute.RoborallyServer.model.Heading.SOUTH;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
@Setter
@Getter
public class Player {

    final public static int NO_REGISTERS = 5;
    final public static int NO_CARDS = 8;
    final public static int NO_UPGRADE_CARDS = 3; // Both for temporary and permanent, so 3 for each

    final public Board board;

    final private int id;

    private boolean ready = false;
    private boolean usedUpgradePhase = false;

    @Getter
    private String name;
    @Getter
    @Setter
    private String color;

    @Getter
    private Space space;
    @Getter
    private Heading heading = SOUTH;

    private List<CommandCard> drawPile;
    private List<CommandCard> discardPile;
    private UpgradeCardField[] temporaryUpgrades;
    private UpgradeCardField[] permanentUpgrades;

    private int checkpoints = 0;
    /**
     * -- GETTER --
     *
     * @return the amount of energy cubes the player has in their energy bank.
     *
     * -- SETTER --
     *  Sets the amount of energy cubes the player has in their energy bank to a wished amount.
     *

     */
    @Setter
    @Getter
    public int energyCubes = 0;
    private CommandCardField[] program;
    private CommandCardField[] cards;

    boolean rebooting = false;

    public static RoboRallyServer server;

    public Player(@NotNull Board board, String color, @NotNull String name, int id) {
        this.board = board;
        this.id = id;
        this.name = name;
        this.color = color;
        this.space = null;

        Command[] commands = Command.values();
        drawPile = new ArrayList<CommandCard>();
        discardPile = new ArrayList<CommandCard>();
        int[] commandValues = {0, 0, 0, 0, 1, 1, 1, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 6, 7, 8};
        //int[] commandValues = {14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14};
        for (int commandValue : commandValues) {
            drawPile.add(new CommandCard(commands[commandValue]));
        }

        shuffleDrawPile();

        program = new CommandCardField[NO_REGISTERS];
        for (int i = 0; i < program.length; i++) {
            program[i] = new CommandCardField(this);
        }

        cards = new CommandCardField[NO_CARDS];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new CommandCardField(this);
        }

        temporaryUpgrades = new UpgradeCardField[NO_UPGRADE_CARDS];
        for (int i = 0; i < temporaryUpgrades.length; i++) {
            temporaryUpgrades[i] = new UpgradeCardField();
        }

        permanentUpgrades = new UpgradeCardField[NO_UPGRADE_CARDS];
        for (int i = 0; i < permanentUpgrades.length; i++) {
            permanentUpgrades[i] = new UpgradeCardField();
        }
    }

    public void shuffleDrawPile() {
        for (int i = 0; i < drawPile.size(); i++) {
            int r = (int) (Math.random() * drawPile.size());
            CommandCard tmp = drawPile.get(i);
            drawPile.set(i, drawPile.get(r));
            drawPile.set(r, tmp);
        }
    }

    public CommandCard drawCommandCard() {
        if (drawPile.isEmpty()) {
            drawPile = discardPile;
            discardPile = new ArrayList<CommandCard>();
            shuffleDrawPile();
        }
        CommandCard card = drawPile.get(0);
        drawPile.remove(0);
        return card;
    }

    public void addCommandCard(CommandCard card) {
        discardPile.add(card);
    }

    public void discardCommandCard(CommandCard card) {
        discardPile.add(card);
    }

    public void removeFromDiscardPile(Command command) {
        for (CommandCard card : discardPile) {
            if (card.command == command) {
                discardPile.remove(card);
                return;
            }
        }
    }

    public void reachCheckpoint() {
        checkpoints++;

        if (checkpoints == board.checkpoints) {
            server.gameWon(this);
        }
    }

    /**
     * adds a wished number of energy cubes to the player's energy bank.
     * @author Oscar (224752)
     */
    public void addEnergyCubes(int energyCubes) {
        this.energyCubes += energyCubes;
    }

    /**
     * removes a wished number of energy cubes from the player's energy bank.
     * @author Oscar (224752)
     */
    public void removeEnergyCubes(int energyCubes) {
        this.energyCubes -= energyCubes;
    }

    public void setName(String name) {
        if (name != null && !name.equals(this.name)) {
            this.name = name;
        }
    }

    public void setSpace(Space space) {
        Space oldSpace = this.space;
        if (space == null || space.board == this.board) {
            this.space = space;
            if (oldSpace != null) {
                oldSpace.setPlayer(null);
            }
            if (space != null) {
                space.setPlayer(this);
            }
        }
    }

    public void setHeading(@NotNull Heading heading) {
        if (heading != this.heading) {
            this.heading = heading;
        }
    }

    public boolean hasUsedUpgradePhase() {
        return usedUpgradePhase;
    }

    /**
     * Reboots the player and adds spam cards
     * @author Jonathan (s235115)
     * @param gameController game controller used for this game
     */
    public void reboot(GameController gameController) {
        rebooting = true;
        if (!hasActiveUpgrade(Upgrade.FIREWALL)) {
            addCommandCard(new CommandCard(SPAM));
            addCommandCard(new CommandCard(SPAM));
        }

        gameController.moveToSpace(this, board.getRebootStation(), board.getRebootStationHeading());
    }

    /**
     * @return Returns whether the card was successfully bought
     * @author Jonathan (s235115)
     */
    public boolean buyUpgradeCard(UpgradeCard upgrade) {
        if (energyCubes < upgrade.getCost()) {
            return false;
        }
        switch (upgrade.upgrade) {
            case ENERGY_ROUTINE:
                discardCommandCard(new CommandCard(ENERGY_ROUTINE));
                return true;
            case REPEAT_ROUTINE:
                discardCommandCard(new CommandCard(REPEAT_ROUTINE));
                return true;
            case SANDBOX_ROUTINE:
                discardCommandCard(new CommandCard(SANDBOX_ROUTINE));
                return true;
            case SPAM_FOLDER_ROUTINE:
                discardCommandCard(new CommandCard(SPAM_FOLDER));
                return true;
            case SPEED_ROUTINE:
                discardCommandCard(new CommandCard(SPEED_ROUTINE));
                return true;
            case WEASEL_ROUTINE:
                discardCommandCard(new CommandCard(WEASEL_ROUTINE));
                return true;
        }
        for (UpgradeCardField field : (upgrade.getIsPermanent() ? permanentUpgrades : temporaryUpgrades)) {
            if (field.getCard() == null) {
                field.setCard(upgrade);
                removeEnergyCubes(upgrade.getCost());
                return true;
            }
        }
        return false;
    }

    public void discardUpgradeCard(int index, boolean isPermanent) {
        UpgradeCardField field = (isPermanent ? permanentUpgrades : temporaryUpgrades)[index];
        if (field.getCard() != null) {
            field.setCard(null);
        }
    }

    public void togglePermanentUpgradeCard(int index) {
        UpgradeCardField field = permanentUpgrades[index];
        if (field.getCard() != null) {
            field.getCard().toggle();
        }
    }

    public void useTemporaryUpgradeCard(int index) {
        UpgradeCardField field = temporaryUpgrades[index];
        if (field.getCard() != null) {
            Upgrade upgrade = field.getCard().upgrade;
            switch (upgrade) {
                case RECHARGE:
                    addEnergyCubes(3);
                    break;
                case RECOMPILE:
                    for (CommandCardField cardField : cards) {
                        if (cardField.getCard() != null) {
                            discardCommandCard(cardField.getCard());
                            cardField.setCard(drawCommandCard());
                        }
                    }
                    break;
                case SPAM_BLOCKER:
                    for (CommandCardField cardField : cards) {
                        if (cardField.getCard() != null && cardField.getCard().command == SPAM) {
                            cardField.setCard(drawCommandCard());
                        }
                    }
                    break;
            }
            field.setCard(null);
        }
    }

    public UpgradeCard getUpgrade(Upgrade upgrade) {
        for (UpgradeCardField field : (upgrade.isPermanent ? permanentUpgrades : temporaryUpgrades)) {
            if (field.getCard() != null && field.getCard().upgrade == upgrade) {
                return field.getCard();
            }
        }
        return null;
    }

    public boolean hasActiveUpgrade(Upgrade upgrade) {
        UpgradeCard card = getUpgrade(upgrade);
        if (card != null) return card.isActive();
        return false;
    }

    public void takeDamage(Player aggressor, Command damageType) {
        if (aggressor != null) {
            if (aggressor.hasActiveUpgrade(Upgrade.SCRAMBLER) && board.getStep() < 5) {
                discardCommandCard(getProgramField(board.getStep()).getCard()); // Step has already been incremented
                getProgramField(board.getStep()).setCard(drawCommandCard());
            }

            if (aggressor.hasActiveUpgrade(Upgrade.BLUE_SCREEN_OF_DEATH) &&
                    (Math.abs(aggressor.space.x - space.x + aggressor.space.y - space.y) < 2) &&
                    damageType == SPAM) {
                addCommandCard(new CommandCard(WORM));
                return;
            }
            if (aggressor.hasActiveUpgrade(Upgrade.TROJAN_NEEDLER) && damageType == SPAM) {
                addCommandCard(new CommandCard(TROJAN_HORSE));
                return;
            }

            if (aggressor.hasActiveUpgrade(Upgrade.CORRUPTION_WAVE) && damageType == SPAM) {
                drawPile.add(0, new CommandCard(SPAM));
                return;
            }
        }
        addCommandCard(new CommandCard(damageType));
    }

    public boolean isRebooting() {
        return rebooting;
    }

    public void stopRebooting() {
        rebooting = false;
    }

    // For save and load
    public void setRebooting(boolean rebooting) {
        this.rebooting = rebooting;
    }

    public CommandCardField getProgramField(int i) {
        return program[i];
    }

    public CommandCardField getCardField(int i) {
        return cards[i];
    }

    public List<CommandCard> getDrawPile() {
        return drawPile;
    }

    public List<CommandCard> getDiscardPile() {
        return discardPile;
    }

    public CommandCardField[] getProgram() {
        return program;
    }

    public CommandCardField[] getCards() {
        return cards;
    }

}
