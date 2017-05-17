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

import com.andune.minecraft.hsp.entity.Player;
import com.andune.minecraft.hsp.storage.dao.PlayerDAO;
import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author andune
 */
public class PlayerDAOEBean implements PlayerDAO {
    private EbeanServer ebean;
    private final EbeanStorageUtil util;

    public PlayerDAOEBean(final EbeanServer ebean, final EbeanStorageUtil util) {
        setEbeanServer(ebean);
        this.util = util;
    }

    public void setEbeanServer(final EbeanServer ebean) {
        this.ebean = ebean;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.PlayerDAO#findPlayerByName(java.lang.String)
     */
    @Override
    public Player findPlayerByName(String name) {
        return ebean.find(Player.class).where().ieq("name", name).findUnique();
    }

    @Override
    public Player findPlayerByUUID(UUID uuid) {
        return ebean.find(Player.class).where().ieq("uuid", uuid.toString()).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.PlayerDAO#findAllPlayers()
     */
    @Override
    public Set<Player> findAllPlayers() {
        return ebean.find(Player.class).findSet();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.PlayerDAO#savePlayer(com.andune.minecraft.hsp.entity.Player)
     */
    @Override
    public void savePlayer(Player player) {
        ebean.save(player);
    }

    @Override
    public int purgePlayerData(long purgeTime) {
        return util.purgePlayers(this, purgeTime);
    }

    @Override
    public int purgePlayer(String playerName) {
        int rowsPurged = 0;
        Transaction tx = ebean.beginTransaction();
        SqlUpdate update = ebean.createSqlUpdate("delete from hsp_player where name = :playerName");
        update.setParameter("playerName", playerName);
        rowsPurged += update.execute();
        tx.commit();
        return rowsPurged;
    }

    @Override
    public Set<String> getAllPlayerNames() {
        Set<Player> set = ebean.find(Player.class).select("name").findSet();
        Set<String> playerNames = new HashSet<String>(set.size() * 3 / 2);
        for (Player player : set) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }
}
