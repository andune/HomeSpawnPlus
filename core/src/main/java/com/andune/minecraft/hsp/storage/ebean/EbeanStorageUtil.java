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

import com.andune.minecraft.commonlib.server.api.OfflinePlayer;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.dao.PurgePlayer;
import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * Common routines utilized by storage subsystem objects.
 *
 * @author andune
 */
@Singleton
public class EbeanStorageUtil {
    private final EbeanServer ebean;
    private final Server server;

    @Inject
    public EbeanStorageUtil(Server server, EbeanServer ebean) {
        this.server = server;
        this.ebean = ebean;
    }

    public int purgePlayers(PurgePlayer purgeDAO, long purgeTime) {
        int rowsPurged = 0;

        Set<String> allPlayerNames = purgeDAO.getAllPlayerNames();
        for (String playerName : allPlayerNames) {
            OfflinePlayer player = server.getOfflinePlayer(playerName);

            if (player.getLastPlayed() < purgeTime) {
                rowsPurged += purgeDAO.purgePlayer(playerName);
            }
        }

        return rowsPurged;
    }

    /**
     * Given a table, column and unique value, delete those rows from the
     * tables.
     *
     * @param table
     * @param column
     * @param value
     * @return
     */
    public int deleteRows(String table, String column, String value) {
        int rowsPurged = 0;

        Transaction tx = ebean.beginTransaction();
        SqlUpdate update = ebean.createSqlUpdate("delete from " + table + " where " + column + " = :value");
        update.setParameter("value", value);
        rowsPurged += update.execute();
        tx.commit();

        return rowsPurged;
    }
}
