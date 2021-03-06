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
/**
 *
 */
package com.andune.minecraft.hsp.storage.dao;

import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.storage.StorageException;

import java.util.Set;

/**
 * @author andune
 */
public interface HomeDAO extends PurgePlayer {
    Home findHomeById(int id);

    Home findDefaultHome(String world, String playerName);

    Home findBedHome(String world, String playerName);

    Home findHomeByNameAndPlayer(String homeName, String playerName);

    /**
     * Given a world and player, find all homes on that world for that player.
     *
     * @param world      the world. A value of 'null', 'all' or '*' should be treated
     *                   as a search for all worlds.
     * @param playerName
     * @return
     */
    Set<? extends Home> findHomesByWorldAndPlayer(String world, String playerName);

    Set<? extends Home> findHomesByPlayer(String playerName);

    Set<? extends Home> findAllHomes();

    void saveHome(Home home) throws StorageException;

    void deleteHome(Home home) throws StorageException;

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.Storage#purgeWorldData(java.lang.String)
     */
    int purgeWorldData(String world);
}
