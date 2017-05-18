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
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.entity.HomeImpl;
import com.andune.minecraft.hsp.entity.HomeInvite;
import com.andune.minecraft.hsp.entity.PlayerLastLocation;
import com.andune.minecraft.hsp.entity.PlayerSpawn;
import com.andune.minecraft.hsp.entity.SpawnImpl;
import com.andune.minecraft.hsp.entity.Version;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.PersistenceException;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements storage using Bukkit Ebeans system.  This can be backed by either MySQL
 * or sqlLite depending on what the admin has configured in their bukkit.yml: it makes no
 * difference to us, the API is the same.
 *
 * @author andune
 */
@Singleton
public class StorageEBeans implements Storage {
    private final Logger log = LoggerFactory.getLogger(StorageEBeans.class);

    private final DatabaseInstaller dbSetup;

    private final EbeanServer ebeanServer;
    private final EBeanUtils ebeanUtils;
    private final Plugin plugin;
    private HomeDAOEBean homeDAO;
    private HomeInviteDAOEBean homeInviteDAO;
    private SpawnDAOEBean spawnDAO;
    private PlayerDAOEBean playerDAO;
    private VersionDAOEBean versionDAO;
    private PlayerSpawnDAOEBean playerSpawnDAO;
    private PlayerLastLocationDAOEBean playerLastLocationDAO;

    @Inject
    public StorageEBeans(EbeanServer ebeanServer, EBeanUtils ebeanUtils, Plugin plugin,
                         ConfigCore configCore, Server server, DatabaseInstaller dbSetup) {
        this.ebeanServer = ebeanServer;
        this.ebeanUtils = ebeanUtils;
        this.plugin = plugin;
        this.dbSetup = dbSetup;

        final EbeanStorageUtil ebeanStorageUtil = new EbeanStorageUtil(server, ebeanServer);

        homeDAO = new HomeDAOEBean(getDatabase(), configCore, ebeanStorageUtil);
        homeInviteDAO = new HomeInviteDAOEBean(getDatabase(), this, configCore, ebeanStorageUtil);
        spawnDAO = new SpawnDAOEBean(getDatabase(), ebeanStorageUtil);
        playerDAO = new PlayerDAOEBean(getDatabase(), ebeanStorageUtil);
        versionDAO = new VersionDAOEBean(getDatabase());
        playerSpawnDAO = new PlayerSpawnDAOEBean(getDatabase(), ebeanStorageUtil);
        playerLastLocationDAO = new PlayerLastLocationDAOEBean(getDatabase(), ebeanStorageUtil);
    }

    @Override
    public String getImplName() {
        return "EBEANS";
    }

    public final EbeanServer getDatabase() {
        return ebeanServer;
    }

    public static List<Class<?>> getDatabaseClasses() {
        List<Class<?>> classList = new LinkedList<Class<?>>();
        classList.add(HomeImpl.class);
        classList.add(SpawnImpl.class);
        classList.add(com.andune.minecraft.hsp.entity.Player.class);
        classList.add(Version.class);
        classList.add(HomeInvite.class);
        classList.add(PlayerSpawn.class);
        classList.add(PlayerLastLocation.class);
        return classList;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.IStorage#initializeStorage
     */
    public void initializeStorage() throws StorageException {
        if (ebeanServer == null)
            throw new IllegalStateException("EbeanServer is null!");

        // Check that our tables exist - if they don't, then install the database.
        try {
            ebeanServer.find(SpawnImpl.class).findCount();
        } catch (PersistenceException ex) {
            log.info("Installing database for " + plugin.getName());
            dbSetup.installDatabase();
        }

        try {
            new DatabaseUpgrade(getDatabase(), ebeanUtils, this).upgradeDatabase();
        } catch (Exception e) {
            log.error("Caught exception when checking for database upgrade", e);
        }
    }

    @Override
    public void shutdownStorage() {
        // do nothing
    }

    @Override
    public com.andune.minecraft.hsp.storage.dao.HomeDAO getHomeDAO() {
        return homeDAO;
    }

    @Override
    public com.andune.minecraft.hsp.storage.dao.HomeInviteDAO getHomeInviteDAO() {
        return homeInviteDAO;
    }

    @Override
    public com.andune.minecraft.hsp.storage.dao.PlayerDAO getPlayerDAO() {
        return playerDAO;
    }

    @Override
    public com.andune.minecraft.hsp.storage.dao.SpawnDAO getSpawnDAO() {
        return spawnDAO;
    }

    @Override
    public com.andune.minecraft.hsp.storage.dao.VersionDAO getVersionDAO() {
        return versionDAO;
    }

    @Override
    public com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO getPlayerSpawnDAO() {
        return playerSpawnDAO;
    }

    @Override
    public com.andune.minecraft.hsp.storage.dao.PlayerLastLocationDAO getPlayerLastLocationDAO() {
        return playerLastLocationDAO;
    }

    @Override
    public void purgeCache() {
        // in theory we should pass this call through to the EBEAN server, but it doesn't
        // offer any support for this functionality.  So we do nothing.
    }

    @Override
    public int purgePlayerData(long purgeTime) {
        int purgedRows = getHomeInviteDAO().purgePlayerData(purgeTime);
        purgedRows += getHomeDAO().purgePlayerData(purgeTime);
        purgedRows += getHomeInviteDAO().purgePlayerData(purgeTime);
        purgedRows += getPlayerLastLocationDAO().purgePlayerData(purgeTime);
        purgedRows += getPlayerSpawnDAO().purgePlayerData(purgeTime);
        purgedRows += getPlayerDAO().purgePlayerData(purgeTime);
        return purgedRows;
    }

    @Override
    public int purgeWorldData(String world) {
        int purgedRows = getHomeInviteDAO().purgeWorldData(world);
        purgedRows += getHomeDAO().purgeWorldData(world);
        purgedRows += getHomeInviteDAO().purgeWorldData(world);
        purgedRows += getPlayerLastLocationDAO().purgeWorldData(world);
        purgedRows += getPlayerSpawnDAO().purgeWorldData(world);
        purgedRows += getSpawnDAO().purgeWorldData(world);
        return purgedRows;
    }

    @Override
    public void deleteAllData() {
        EbeanServer db = getDatabase();
        db.beginTransaction();

        SqlUpdate update = db.createSqlUpdate("delete from hsp_spawn");
        update.execute();

        update = db.createSqlUpdate("delete from hsp_home");
        update.execute();

        update = db.createSqlUpdate("delete from hsp_player");
        update.execute();

        update = db.createSqlUpdate("delete from hsp_homeinvite");
        update.execute();

        update = db.createSqlUpdate("delete from hsp_playerspawn");
        update.execute();

        update = db.createSqlUpdate("delete from hsp_playerlastloc");
        update.execute();

        db.commitTransaction();
    }


    // Ebeans does nothing with these methods
    @Override
    public void setDeferredWrites(boolean deferred) {
    }

    @Override
    public void flushAll() throws StorageException {
    }
}
