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
import com.andune.minecraft.hsp.entity.Version;
import io.ebean.EbeanServer;
import io.ebean.config.ServerConfig;
import io.ebean.dbmigration.DdlGenerator;
import io.ebeaninternal.api.SpiEbeanServer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class to handle setup of the database, including first time initialization
 * if necessary.
 *
 * @author andune
 */
@Singleton
public class DatabaseInstaller {
    protected static final Logger log = LoggerFactory.getLogger(DatabaseInstaller.class);

    @Inject
    private EbeanServer db;
    @Inject
    private Plugin plugin;
    
    /**
     */
    public void installDatabase() {
        try {
            if (db.find(Version.class).findCount() > 0) {
                return;  // database already exists, don't install again
            }
        } catch (Exception e) {
            // if there is an exception, that means the DB doesn't
            // exist yet, so fall through to DB creation below
        }

        //Create a DDL generator
        final SpiEbeanServer serv = (SpiEbeanServer) db;
        final ServerConfig sc = serv.getServerConfig();
        sc.setDdlGenerate(true);
        sc.setDdlCreateOnly(true);
        sc.setDdlRun(true);
        DdlGenerator gen = new DdlGenerator(serv, sc);
        
        log.debug("Executing database DDL");
        gen.execute(true);
    }
    
    private String replaceDatabaseString(String input) {
        input = input.replaceAll("\\{DIR\\}", plugin.getDataFolder().getPath().replaceAll("\\\\", "/") + "/");
        input = input.replaceAll("\\{NAME\\}", plugin.getName().replaceAll("[^\\w_-]", ""));

        return input;
    }
}
