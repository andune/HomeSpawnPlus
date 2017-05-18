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
package com.andune.minecraft.hsp.guice;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.HomeSpawnPlusBukkit;
import com.andune.minecraft.hsp.storage.ebean.EBeanUtils;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.TxIsolation;
import io.ebean.config.ClassLoadConfig;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.sqlite.SQLitePlatform;
import org.avaje.datasource.DataSourceConfig;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;

/**
 * @author andune
 */
public class EbeanServerProvider implements Provider<EbeanServer> {
    protected static final Logger log = LoggerFactory.getLogger(EbeanServerProvider.class);
    
    @Inject
    private HomeSpawnPlusBukkit plugin;
    @Inject
    private EBeanUtils ebeanUtils;

    @Override
    public EbeanServer get() {

        ServerConfig config = prepareDatabase();
        config.setName("db");
        // load configuration from ebean.properties
        config.loadFromProperties();
        config.setDefaultServer(true);
        config.setClassLoadConfig(new ClassLoadConfig(this.getClass().getClassLoader()));

        log.debug("db user: {}", config.getDataSourceConfig().getUsername());

        return EbeanServerFactory.create(config);
    }

    private ServerConfig prepareDatabase() {
        //Setup the data source
        DataSourceConfig ds = new DataSourceConfig();
        ds.setDriver(ebeanUtils.getDriver());
        ds.setUrl(replaceDatabaseString(ebeanUtils.getUrl()));
        ds.setUsername(ebeanUtils.getUsername());
        ds.setPassword(ebeanUtils.getPassword());

        //Setup the server configuration
        ServerConfig sc = new ServerConfig();
        sc.setDefaultServer(true);
        sc.setRegister(true);
        sc.setName(ds.getUrl().replaceAll("[^a-zA-Z0-9]", ""));

        //Get all persistent classes
        List<Class<?>> classes = plugin.getDatabaseClasses();

        //Do a sanity check first
        if (classes.size() == 0) {
            //Exception: There is no use in continuing to load this database
            throw new RuntimeException("Database has been enabled, but no classes are registered to it");
        }

        //Register them with the EbeanServer
        sc.setClasses(classes);
        //Check if the SQLite JDBC supplied with Bukkit is being used
        if (ds.getDriver().equalsIgnoreCase("org.sqlite.JDBC")) {
            sc.setDatabasePlatform(new SQLitePlatform());
            ds.setIsolationLevel(TxIsolation.SERIALIZABLE.getLevel());
        }

        //Finally the data source
        sc.setDataSourceConfig(ds);
        return sc;
    }

    private String replaceDatabaseString(String input) {
        input = input.replaceAll("\\{DIR\\}", plugin.getDataFolder().getPath().replaceAll("\\\\", "/") + "/");
        input = input.replaceAll("\\{NAME\\}", plugin.getName().replaceAll("[^\\w_-]", ""));

        return input;
    }
}
