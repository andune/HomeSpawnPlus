/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.andune.minecraft.hsp.strategy;

/**
 * The HomeSpawnPlus event types. Strategy chains are defined and
 * based on these event types.
 *
 * @author andune
 */
public enum EventType {
    ON_JOIN("onJoin"),
    ON_DEATH("onDeath"),
    HOME_COMMAND("onHomeCommand"),
    NAMED_HOME_COMMAND("onNamedHomeCommand"),
    NAMED_SPAWN_COMMAND("onNamedSpawnCommand"),
    SPAWN_COMMAND("onSpawnCommand"),
    GROUPSPAWN_COMMAND("onGroupSpawnCommand"),
    CROSS_WORLD_TELEPORT("crossWorldTeleport"),
    MULTIVERSE_TELEPORT_CROSSWORLD("multiverseCrossWorldTeleport"),
    MULTIVERSE_TELEPORT("multiverseTeleport"),
    ENTER_REGION("onRegionEnter"),
    EXIT_REGION("onRegionExit"),
    NEW_PLAYER("onNewPlayer"),
    TELEPORT_OBSERVE("onTeleportObserve"),
    FALL_THROUGH_WORLD("onFallThroughWorld");

    private String configOption;

    EventType(String configOption) {
        this.configOption = configOption;
    }

    public String getConfigOption() {
        return configOption;
    }

    public String toString() {
        return getConfigOption();
    }
}
