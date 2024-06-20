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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class UpgradeCard {

    final public Upgrade upgrade;
    @Getter
    @Setter
    private boolean active = true;


    public void toggle() {
        active = !active;
    }

    public UpgradeCard(@NotNull Upgrade upgrade) {
        this.upgrade = upgrade;
    }
    public int getCost() {
        return upgrade.cost;
    }
    public boolean getIsPermanent() {
        return upgrade.isPermanent;
    }
    public static UpgradeCard drawRandomUpgradeCard() {
        Random rand = new Random();
        int upgrade = rand.nextInt(Upgrade.values().length);
        return new UpgradeCard(Upgrade.values()[upgrade]);
    }
}
