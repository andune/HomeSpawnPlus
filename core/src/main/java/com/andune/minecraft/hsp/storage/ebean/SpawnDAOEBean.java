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

import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.entity.SpawnImpl;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.SqlUpdate;

import java.util.HashSet;
import java.util.Set;

/**
 * @author andune
 */
public class SpawnDAOEBean implements SpawnDAO {
    protected static final String TABLE = "hsp_spawn";

    private EbeanServer ebean;
    private final EbeanStorageUtil util;

    public SpawnDAOEBean(final EbeanServer ebean, final EbeanStorageUtil util) {
        setEbeanServer(ebean);
        this.util = util;
    }

    public void setEbeanServer(final EbeanServer ebean) {
        this.ebean = ebean;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.SpawnDAO#findSpawnByWorld(java.lang.String)
     */
    @Override
    public Spawn findSpawnByWorld(String world) {
        return findSpawnByWorldAndGroup(world, Storage.HSP_WORLD_SPAWN_GROUP);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.SpawnDAO#findSpawnByWorldAndGroup(java.lang.String, java.lang.String)
     */
    @Override
    public Spawn findSpawnByWorldAndGroup(String world, String group) {
        return ebean.find(SpawnImpl.class).where().eq("world", world)
                .and().eq("group", group).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.SpawnDAO#findSpawnByName(java.lang.String)
     */
    @Override
    public Spawn findSpawnByName(String name) {
        return ebean.find(SpawnImpl.class).where().eq("name", name).findUnique();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.SpawnDAO#findSpawnById(int)
     */
    @Override
    public Spawn findSpawnById(int id) {
        return ebean.find(SpawnImpl.class).where().idEq(id).findUnique();
    }

    public Spawn getNewPlayerSpawn() {
        return findSpawnByName(NEW_PLAYER_SPAWN);
    }

    /**
     * We make the assumption that there are relatively few spawns and group combinations,
     * thus the easiest algorithm is simply to grab all the spawns and iterate through
     * them for the valid group list.
     *
     * @see com.andune.minecraft.hsp.storage.dao.SpawnDAO#getSpawnDefinedGroups()
     */
    @Override
    public java.util.Set<String> getSpawnDefinedGroups() {
        Set<String> groups = new HashSet<String>();
        Set<? extends Spawn> spawns = findAllSpawns();

        for (Spawn spawn : spawns) {
            String group = spawn.getGroup();
            if (group != null)
                groups.add(group);
        }

        return groups;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.SpawnDAO#findAllSpawns()
     */
    @Override
    public Set<? extends Spawn> findAllSpawns() {
        return ebean.find(SpawnImpl.class).findSet();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.SpawnDAO#saveSpawn(com.andune.minecraft.hsp.entity.Spawn)
     */
    @Override
    public void saveSpawn(Spawn spawn) {
        ebean.save((SpawnImpl) spawn);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.storage.dao.SpawnDAO#deleteSpawn(com.andune.minecraft.hsp.entity.Spawn)
     */
    @Override
    public void deleteSpawn(Spawn spawn) {
        SqlUpdate update = ebean.createSqlUpdate("delete from hsp_playerspawn where spawn_id = :spawn");
        update.setParameter("spawn", spawn.getId());
        update.execute();

        ebean.delete((SpawnImpl) spawn);
    }

    @Override
    public int purgeWorldData(String world) {
        return util.deleteRows(TABLE, "world", world);
    }
}
