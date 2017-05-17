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

import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.HomeImpl;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;

import java.util.HashSet;
import java.util.Set;

/**
 * @author andune
 */
public class HomeDAOEBean implements HomeDAO {
    protected static final String TABLE = "hsp_home";

    private final ConfigCore configCore;
    private final EbeanStorageUtil util;
    private EbeanServer ebean;

    public HomeDAOEBean(final EbeanServer ebean, final ConfigCore configCore, final EbeanStorageUtil util) {
        setEbeanServer(ebean);
        this.configCore = configCore;
        this.util = util;
    }

    public void setEbeanServer(final EbeanServer ebean) {
        this.ebean = ebean;
    }

    @Override
    public Home findHomeById(int id) {
        return ebean.find(HomeImpl.class).where().idEq(id).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.HomeDAO#getDefaultHome(java.lang.String, java.lang.String)
     */
    @Override
    public Home findDefaultHome(String world, String playerName) {
        return ebean.find(HomeImpl.class).where().ieq("playerName", playerName)
                .and().eq("world", world)
                .and().eq("defaultHome", 1).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.HomeDAO#getBedHome(java.lang.String, java.lang.String)
     */
    @Override
    public Home findBedHome(String world, String playerName) {
        return ebean.find(HomeImpl.class).where().ieq("playerName", playerName)
                .and().eq("world", world)
                .and().eq("bedHome", 1).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.HomeDAO#getNamedHome(java.lang.String, java.lang.String)
     */
    @Override
    public Home findHomeByNameAndPlayer(String homeName, String playerName) {
        return ebean.find(HomeImpl.class).where().ieq("playerName", playerName)
                .and().eq("name", homeName).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.HomeDAO#getHomes(java.lang.String, java.lang.String)
     */
    @Override
    public Set<? extends Home> findHomesByWorldAndPlayer(String world, String playerName) {
        if (world == null || "all".equals(world) || "*".equals(world))
            world = "%";

        return ebean.find(HomeImpl.class).where().eq("playerName", playerName)
                .and().like("world", world).findSet();
    }

    public Set<? extends Home> findHomesByPlayer(String playerName) {
        return findHomesByWorldAndPlayer(null, playerName);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.HomeDAO#getAllHomes()
     */
    @Override
    public Set<? extends Home> findAllHomes() {
        return ebean.find(HomeImpl.class).findSet();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.HomeDAO#writeHome(com.andune.minecraft.hsp.entity.Home)
     */
    @Override
    public void saveHome(final Home homeArg) {
        HomeImpl home = (HomeImpl) homeArg;

        final int homeId = home.getId();

        Transaction tx = ebean.beginTransaction();
        // We should only have one "BedHome" per player per world. So if this update is setting
        // BedHome to true, then we make sure to clear out all others for this player/world combo
        if (home.isBedHome()) {
            SqlUpdate update = ebean.createSqlUpdate("update " + TABLE + " set bed_home=0"
                    + " where player_name = :playerName and world = :world and id != :id");
            update.setParameter("playerName", home.getPlayerName());
            update.setParameter("world", home.getWorld());
            update.setParameter("id", homeId);
            update.execute();
        }

        // We should only have one defaultHome per player per world. So if this update is setting
        // defaultHome to true, then we make sure to clear out all others for this player/world combo
        if (home.isDefaultHome()) {
            SqlUpdate update = ebean.createSqlUpdate("update " + TABLE + " set default_home=0"
                    + " where player_name = :playerName and world = :world and id != :id");
            update.setParameter("playerName", home.getPlayerName());
            update.setParameter("world", home.getWorld());
            update.setParameter("id", homeId);
            update.execute();
        }
        tx.commit();

        ebean.save(home);

        // clean up any related home invites as well
        tx = ebean.beginTransaction();
        SqlUpdate update = ebean.createSqlUpdate("delete from hsp_homeinvite"
                + " where home_id = :id");
        update.setParameter("id", homeId);
        update.execute();
        tx.commit();
    }

    @Override
    public void deleteHome(Home home) {
        ebean.delete((HomeImpl) home);
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
        return util.deleteRows(TABLE, "player_name", playerName);
    }

    @Override
    public Set<String> getAllPlayerNames() {
        Set<HomeImpl> set = ebean.find(HomeImpl.class).select("playerName").findSet();
        Set<String> playerNames = new HashSet<String>(set.size() * 3 / 2);
        for (HomeImpl home : set) {
            playerNames.add(home.getPlayerName());
        }
        return playerNames;
    }
}
