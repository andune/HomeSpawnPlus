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

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.HomeInvite;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.HomeInviteDAO;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.Transaction;

import java.util.HashSet;
import java.util.Set;

/**
 * @author andune
 */
public class HomeInviteDAOEBean implements HomeInviteDAO {
    private static final Logger log = LoggerFactory.getLogger(HomeInviteDAOEBean.class);
    protected static final String TABLE = "hsp_homeinvite";

    private EbeanServer ebean;
    private final Storage storage;
    private final ConfigCore configCore;
    private final EbeanStorageUtil util;

    public HomeInviteDAOEBean(final EbeanServer ebean, final Storage storage,
                              ConfigCore configCore, final EbeanStorageUtil util) {
        setEbeanServer(ebean);
        this.storage = storage;
        this.configCore = configCore;
        this.util = util;
    }

    public void setEbeanServer(final EbeanServer ebean) {
        this.ebean = ebean;
    }

    @Override
    public HomeInvite findHomeInviteById(int id) {
        return ebean.find(HomeInvite.class).where().idEq(id).findUnique();
    }

    @Override
    public HomeInvite findInviteByHomeAndInvitee(Home home, String invitee) {
        return ebean.find(HomeInvite.class).where().eq("home", home)
                .and().ieq("invitedPlayer", invitee).findUnique();
    }

    @Override
    public Set<HomeInvite> findInvitesByHome(Home home) {
        return ebean.find(HomeInvite.class).where().eq("home", home).findSet();
    }

    @Override
    public Set<HomeInvite> findAllAvailableInvites(String invitee) {
        return ebean.find(HomeInvite.class).where().ieq("invitedPlayer", invitee).findSet();
    }

    @Override
    public Set<HomeInvite> findAllPublicInvites() {
        return findAllAvailableInvites(HomeInvite.PUBLIC_HOME);
    }

    @Override
    public Set<HomeInvite> findAllOpenInvites(String player) {
        Set<HomeInvite> invites = new HashSet<HomeInvite>(5);

        // first find all homes for this player
        Set<? extends Home> homes = storage.getHomeDAO().findHomesByPlayer(player);
        if (homes == null || homes.size() == 0)
            return invites;

        // then find all HomeInvites related to any of those homes
        for (Home home : homes) {
            Set<HomeInvite> homeInvites = findInvitesByHome(home);
            if (homeInvites != null)
                invites.addAll(homeInvites);
        }

        return invites;
    }

    @Override
    public void saveHomeInvite(HomeInvite homeInvite) {
        Transaction tx = ebean.beginTransaction();
        ebean.save(homeInvite, tx);
        tx.commit();
    }

    @Override
    public void deleteHomeInvite(HomeInvite homeInvite) throws StorageException {
        Transaction tx = ebean.beginTransaction();
        tx.setPersistCascade(false);
        ebean.delete(homeInvite, tx);
        tx.commit();
    }

    @Override
    public Set<HomeInvite> findAllHomeInvites() {
        return ebean.find(HomeInvite.class).findSet();
    }

    @Override
    public int purgePlayerData(long purgeTime) {
        return util.purgePlayers(this, purgeTime);
    }

    @Override
    public int purgeWorldData(final String world) {
        int rowsPurged = 0;

        // delete any invites whose home is on the purged world
        Set<HomeInvite> set = findAllHomeInvites();
        for (HomeInvite hi : set) {
            if (world.equals(hi.getHome().getWorld())) {
                try {
                    deleteHomeInvite(hi);
                    rowsPurged++;
                } catch (Exception e) {
                    log.error("Caught exception while purging homeInvite", e);
                }
            }
        }
        return rowsPurged;
    }

    @Override
    public int purgePlayer(String playerName) {
        // first delete any homeInvites the player was invited too
        int rowsPurged = util.deleteRows(TABLE, "invited_player", playerName);

        // then delete any HomeInvites the player sent out to others
        Set<HomeInvite> set = findAllOpenInvites(playerName);
        for (HomeInvite hi : set) {
            try {
                deleteHomeInvite(hi);
                rowsPurged++;
            } catch (Exception e) {
                log.error("Caught exception while purging homeInvite", e);
            }
        }
        return rowsPurged;
    }

    @Override
    public Set<String> getAllPlayerNames() {
        Set<HomeInvite> set = findAllHomeInvites();
        Set<String> playerNames = new HashSet<String>(set.size() * 3 / 2);
        for (HomeInvite hi : set) {
            playerNames.add(hi.getInvitedPlayer());
            playerNames.add(hi.getHome().getPlayerName());
        }
        return playerNames;
    }
}
