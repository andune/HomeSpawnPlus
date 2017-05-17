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
package com.andune.minecraft.hsp;

import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.guice.BukkitInjectorFactory;
import com.andune.minecraft.hsp.server.bukkit.config.BukkitConfigBootstrap;
import com.andune.minecraft.hsp.storage.ebean.StorageEBeans;
import com.andune.minecraft.hsp.util.LogUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the implementation of Bukkit's Plugin interface. This is
 * abstracted from the rest of the plugin so as to minimize impact to the code
 * when Bukkit makes API changes and to simplify supporting MC-API, Spout or
 * other frameworks.
 *
 * @author andune
 */
public class HomeSpawnPlusBukkit extends JavaPlugin {
    private HomeSpawnPlus mainClass;

    @Override
    public void onEnable() {
        LoggerFactory.setLoggerPrefix("[HomeSpawnPlus] ");

        // disable reflections spam; it's a bug that prints warnings that look alarming
        Logger.getLogger("org.reflections").setLevel(Level.OFF);

        File debugFlagFile = new File(getDataFolder(), "devDebug");
        if (debugFlagFile.exists())
            LogUtil.enableDebug();

        try {
            getLogger().log(Level.FINE, "Initializing BukkitInjectorFactory");
            BukkitInjectorFactory factory = new BukkitInjectorFactory(this,
                    new BukkitConfigBootstrap(getBootstrapConfig()));

            getLogger().log(Level.FINE, "Instantiating HomeSpawmPlus mainClass");
            mainClass = new HomeSpawnPlus(factory);

            getLogger().log(Level.FINE, "invoking mainClass.onEnable()");
            mainClass.onEnable();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Caught exception loading plugin, shutting down", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * Find and load the bootstrap configuration, this is required prior to
     * handing off control to the core injection routines.
     *
     * @return
     * @throws Exception
     */
    private YamlConfiguration getBootstrapConfig() throws Exception {
        YamlConfiguration bootstrapConfig = new YamlConfiguration();
        YamlConfiguration defaultBootstrapConfig = new YamlConfiguration();

        // use old-style (1.7) config if it exists, it has priority
        File bootstrapConfigFile = new File(getDataFolder(), "config.yml");
        if (bootstrapConfigFile.exists()) {
            bootstrapConfig.load(new File(getDataFolder(), "config.yml"));
        } else {
            // otherwise use the new-style (2.0) config file
            bootstrapConfigFile = new File(getDataFolder(), "config/core.yml");
            if (bootstrapConfigFile.exists()) {
                bootstrapConfig.load(new File(getDataFolder(), "config/core.yml"));
            }
            // otherwise use the default file in the JAR
            else {
                bootstrapConfig.load(new InputStreamReader(super.getResource("config/core.yml")));
            }
        }

        // set defaults to in-JAR config to cover any missing values
        defaultBootstrapConfig.load(new InputStreamReader(super.getResource("config/core.yml")));
        bootstrapConfig.setDefaults(defaultBootstrapConfig);

        return bootstrapConfig;
    }

    @Override
    public void onDisable() {
        if (mainClass != null)
            mainClass.onDisable();
    }

//    @Override
    public List<Class<?>> getDatabaseClasses() {
        return StorageEBeans.getDatabaseClasses();
    }

    public File _getJarFile() {
        return super.getFile();
    }
}
