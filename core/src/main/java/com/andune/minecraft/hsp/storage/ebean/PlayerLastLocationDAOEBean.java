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
package com.andune.minecraft.hsp.storage.ebean;

import com.andune.minecraft.hsp.entity.PlayerLastLocation;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO;
import io.ebean.EbeanServer;
import io.ebean.Query;

import java.util.HashSet;
import java.util.Set;

/**
 * @author andune
 */
public class PlayerLastLocationDAOEBean implements PlayerLastLocationDAO {
    protected static final String TABLE = "hsp_playerlastloc";

    private EbeanServer ebean;
    private final EbeanStorageUtil util;

    public PlayerLastLocationDAOEBean(final EbeanServer ebean, final EbeanStorageUtil util) {
        setEbeanServer(ebean);
        this.util = util;
    }

    public void setEbeanServer(final EbeanServer ebean) {
        this.ebean = ebean;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO#findById(int)
     */
    @Override
    public PlayerLastLocation findById(int id) {
        return ebean.find(PlayerLastLocation.class).where().idEq(id).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO#findByWorldAndPlayerName(java.lang.String, java.lang.String)
     */
    @Override
    public PlayerLastLocation findByWorldAndPlayerName(String world, String playerName) {
        return ebean.find(PlayerLastLocation.class).where().ieq("player_name", playerName)
                .and().eq("world", world).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO#findByPlayerName(java.lang.String)
     */
    @Override
    public Set<PlayerLastLocation> findByPlayerName(String playerName) {
        return ebean.find(PlayerLastLocation.class).where().ieq("player_name", playerName).findSet();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO#findAll()
     */
    @Override
    public Set<PlayerLastLocation> findAll() {
        return ebean.find(PlayerLastLocation.class).findSet();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO#save(com.andune.minecraft.hsp.entity.PlayerLastLocation)
     */
    @Override
    public void save(PlayerLastLocation playerLastLocation) throws StorageException {
        ebean.save(playerLastLocation);
    }

    @Override
    public int purgePlayerData(long purgeTime) {
        return util.purgePlayers(this, purgeTime);
    }

    @Override
    public int purgeWorldData(final String world) {
        return util.deleteRows(TABLE, "world", world);
    }

    @Override
    public int purgePlayer(String playerName) {
        return util.deleteRows(TABLE, "playerName", playerName);
    }

    @Override
    public Set<String> getAllPlayerNames() {
        Set<PlayerLastLocation> set = ebean.find(PlayerLastLocation.class).select("playerName").findSet();
        Set<String> playerNames = new HashSet<String>(set.size() * 3 / 2);
        for (PlayerLastLocation pll : set) {
            playerNames.add(pll.getPlayerName());
        }
        return playerNames;
    }
}
