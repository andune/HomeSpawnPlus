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
/** Code credit: Lennard Fonteijn
 *  Code author URL: http://www.lennardf1989.com/
 *  Bukkit forum thread: http://forums.bukkit.org/threads/bukkit-persistance-reimplemented-no-code-changes-required.24987/
 */
package com.andune.minecraft.hsp.storage.ebean;

import com.andune.minecraft.commonlib.server.api.Plugin;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.sqlite.SQLitePlatform;
import io.ebean.dbmigration.DdlGenerator;
import io.ebeaninternal.api.SpiEbeanServer;
import org.avaje.datasource.DataSourceConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This poorly named class is the "Persistence-reimplemented" storage method for
 * HSP, that was originally introduced to work around some issues with the
 * built-in ebeans for Bukkit. Now that Bukkit has ejected the built-in ebeans
 * and HSP is shading its own ebeans server, this class should be removed.
 * However, at the moment I believe there are some database install and upgrade
 * methods that depend on this class, so those all need to be cleaned up first.
 * - andune 2017/05/16
 */
@Singleton
public abstract class MyDatabase {
    private Plugin plugin;
    private ClassLoader classLoader;
    private Level loggerLevel;
    private boolean usingSQLite;
    private ServerConfig serverConfig;
    private EbeanServer ebeanServer;

    /**
     * Create an instance of MyDatabase
     *
     * @param plugin Plugin instancing this database
     */
    @Inject
    public MyDatabase(Plugin plugin) {
        this.plugin = plugin;
        this.classLoader = plugin.getClassLoader();
    }

    /**
     * Initialize the database using the passed arguments
     *
     * @param driver    Database-driver to use. For example: org.sqlite.JDBC
     * @param url       Location of the database. For example: jdbc:sqlite:{DIR}{NAME}.db
     * @param username  Username required to access the database
     * @param password  Password belonging to the username, may be empty
     * @param isolation Isolation type. For example: SERIALIZABLE, also see TransactionIsolation
     * @param logging   If set to false, all logging will be disabled
     * @param rebuild   If set to true, all tables will be dropped and recreated. Be sure to create a backup before doing so!
     */
    public void initializeDatabase(String driver, String url, String username, String password, String isolation, boolean logging, boolean rebuild) {
        //Logging needs to be set back to the original level, no matter what happens
        try {
            //Disable all logging
            disableDatabaseLogging(logging);

            //Prepare the database
            prepareDatabase(driver, url, username, password, isolation);

            //Load the database
            loadDatabase();

            //Create all tables
            installDatabase(rebuild);
        } catch (Exception ex) {
            throw new RuntimeException("An exception has occured while initializing the database", ex);
        } finally {
            //Enable all logging
            enableDatabaseLogging(logging);
        }
    }

    private void prepareDatabase(String driver, String url, String username, String password, String isolation) {
        //Setup the data source
        DataSourceConfig ds = new DataSourceConfig();
        ds.setDriver(driver);
        ds.setUrl(replaceDatabaseString(url));
        ds.setUsername(username);
        ds.setPassword(password);

        //Setup the server configuration
        ServerConfig sc = new ServerConfig();
        sc.setDefaultServer(false);
        sc.setRegister(true);
        sc.setName(ds.getUrl().replaceAll("[^a-zA-Z0-9]", ""));

        //Get all persistent classes
        List<Class<?>> classes = getDatabaseClasses();

        //Do a sanity check first
        if (classes.size() == 0) {
            //Exception: There is no use in continuing to load this database
            throw new RuntimeException("Database has been enabled, but no classes are registered to it");
        }

        //Register them with the EbeanServer
        sc.setClasses(classes);

        //Check if the SQLite JDBC supplied with Bukkit is being used
        if (ds.getDriver().equalsIgnoreCase("org.sqlite.JDBC")) {
            //Remember the database is a SQLite-database
            usingSQLite = true;

            //Modify the platform, as SQLite has no AUTO_INCREMENT field
            sc.setDatabasePlatform(new SQLitePlatform());
            
            // TODO: FIXME. getDbDdlSynxtax() doesn't exist anymore. Presumably something is broken
            // by not having this anymore, from the comments it seems SQLite auto-increment might
            // not work anymore. Or if we're lucky, it was implemented in a newer version of SQLite
            // and this code isn't necessary anymore...  testing is required.
            // sc.getDatabasePlatform().getDbDdlSyntax().setIdentity("");
        }

        prepareDatabaseAdditionalConfig(ds, sc);

        //Finally the data source
        sc.setDataSourceConfig(ds);

        //Store the ServerConfig
        serverConfig = sc;
    }

    private void loadDatabase() {
        //Declare a few local variables for later use
        ClassLoader currentClassLoader = null;
        Field cacheField = null;
        boolean cacheValue = true;

        try {
            //Store the current ClassLoader, so it can be reverted later
            currentClassLoader = Thread.currentThread().getContextClassLoader();

            //Set the ClassLoader to Plugin ClassLoader
            Thread.currentThread().setContextClassLoader(classLoader);

            //Get a reference to the private static "defaultUseCaches"-field in URLConnection
            cacheField = URLConnection.class.getDeclaredField("defaultUseCaches");

            //Make it accessible, store the default value and set it to false
            cacheField.setAccessible(true);
            cacheValue = cacheField.getBoolean(null);
            cacheField.setBoolean(null, false);

            //Setup Ebean based on the configuration
            ebeanServer = EbeanServerFactory.create(serverConfig);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create a new instance of the EbeanServer", ex);
        } finally {
            //Revert the ClassLoader back to its original value
            if (currentClassLoader != null) {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }

            //Revert the "defaultUseCaches"-field in URLConnection back to its original value
            try {
                if (cacheField != null) {
                    cacheField.setBoolean(null, cacheValue);
                }
            } catch (Exception e) {
                System.out.println("Failed to revert the \"defaultUseCaches\"-field back to its original value, URLConnection-caching remains disabled.");
            }
        }
    }

    private void installDatabase(boolean rebuild) {
        //Check if the database already (partially) exists
        boolean databaseExists = false;

        List<Class<?>> classes = getDatabaseClasses();
        for (int i = 0; i < classes.size(); i++) {
            try {
                //Do a simple query which only throws an exception if the table does not exist
                ebeanServer.find(classes.get(i)).findCount();

                //Query passed without throwing an exception, a database therefore already exists
                databaseExists = true;
                break;
            } catch (Exception ex) {
                //Do nothing
            }
        }

        //Check if the database has to be created or rebuilt
        if (!rebuild && databaseExists) {
            return;
        }

        //Create a DDL generator
        SpiEbeanServer serv = (SpiEbeanServer) ebeanServer;
        DdlGenerator gen = new DdlGenerator(serv, serverConfig);
//        DdlGenerator gen = serv.getDdlGenerator();
//        serverConfig.getDatabasePlatform().getPlatformDdl().drop

        //Fire "before drop" event
        try {
            beforeDropDatabase();
        } catch (Exception ex) {
            //If the database exists, dropping has to be canceled to prevent data-loss
            if (databaseExists) {
                throw new RuntimeException("An unexpected exception occured", ex);
            }
        }

        /** TODO: figure out best way to do this with new versions of Ebeans
         *
        //Generate a DropDDL-script
        gen.runScript(true, gen.generateDropDdl());

        //If SQLite is being used, the database has to reloaded to release all resources
        if (usingSQLite) {
            loadDatabase();
        }

        //Generate a CreateDDL-script
        if (usingSQLite) {
            //If SQLite is being used, the CreateDLL-script has to be validated and potentially fixed to be valid
            gen.runScript(false, validateCreateDDLSqlite(gen.generateCreateDdl()));
        } else {
            gen.runScript(false, gen.generateCreateDdl());
        }
         */

        //Fire "after create" event
        try {
            afterCreateDatabase();
        } catch (Exception ex) {
            throw new RuntimeException("An unexpected exception occured", ex);
        }
    }

    private String replaceDatabaseString(String input) {
        input = input.replaceAll("\\{DIR\\}", plugin.getDataFolder().getPath().replaceAll("\\\\", "/") + "/");
        input = input.replaceAll("\\{NAME\\}", plugin.getName().replaceAll("[^\\w_-]", ""));

        return input;
    }

    private String validateCreateDDLSqlite(String oldScript) {
        try {
            //Create a BufferedReader out of the potentially invalid script
            BufferedReader scriptReader = new BufferedReader(new StringReader(oldScript));

            //Create an array to store all the lines
            List<String> scriptLines = new ArrayList<String>();

            //Create some additional variables for keeping track of tables
            HashMap<String, Integer> foundTables = new HashMap<String, Integer>();
            String currentTable = null;
            int tableOffset = 0;

            //Loop through all lines
            String currentLine;
            while ((currentLine = scriptReader.readLine()) != null) {
                //Trim the current line to remove trailing spaces
                currentLine = currentLine.trim();

                //Add the current line to the rest of the lines
                scriptLines.add(currentLine.trim());

                //Check if the current line is of any use
                if (currentLine.startsWith("create table")) {
                    //Found a table, so get its name and remember the line it has been encountered on
                    currentTable = currentLine.split(" ", 4)[2];
                    foundTables.put(currentLine.split(" ", 3)[2], scriptLines.size() - 1);
                } else if (currentLine.startsWith(";") && currentTable != null && !currentTable.equals("")) {
                    //Found the end of a table definition, so update the entry
                    int index = scriptLines.size() - 1;
                    foundTables.put(currentTable, index);

                    //Remove the last ")" from the previous line
                    String previousLine = scriptLines.get(index - 1);
                    previousLine = previousLine.substring(0, previousLine.length() - 1);
                    scriptLines.set(index - 1, previousLine);

                    //Change ";" to ");" on the current line
                    scriptLines.set(index, ");");

                    //Reset the table-tracker
                    currentTable = null;
                } else if (currentLine.startsWith("alter table")) {
                    //Found a potentially unsupported action
                    String[] alterTableLine = currentLine.split(" ", 4);

                    if (alterTableLine[3].startsWith("add constraint")) {
                        //Found an unsupported action: ALTER TABLE using ADD CONSTRAINT
                        String[] addConstraintLine = alterTableLine[3].split(" ", 4);

                        //Check if this line can be fixed somehow
                        if (addConstraintLine[3].startsWith("foreign key")) {
                            //Calculate the index of last line of the current table
                            int tableLastLine = foundTables.get(alterTableLine[2]) + tableOffset;

                            //Add a "," to the previous line
                            scriptLines.set(tableLastLine - 1, scriptLines.get(tableLastLine - 1) + ",");

                            //Add the constraint as a new line - Remove the ";" on the end
                            String constraintLine = String.format("%s %s %s", addConstraintLine[1], addConstraintLine[2], addConstraintLine[3]);
                            scriptLines.add(tableLastLine, constraintLine.substring(0, constraintLine.length() - 1));

                            //Remove this line and raise the table offset because a line has been inserted
                            scriptLines.remove(scriptLines.size() - 1);
                            tableOffset++;
                        } else {
                            //Exception: This line cannot be fixed but is known the be unsupported by SQLite
                            throw new RuntimeException("Unsupported action encountered: ALTER TABLE using ADD CONSTRAINT with " + addConstraintLine[3]);
                        }
                    }
                }
            }

            //Turn all the lines back into a single string
            StringBuffer newScript = new StringBuffer();
            for (String newLine : scriptLines) {
                newScript.append(newLine);
                newScript.append("\n");
            }

            //Print the new script
            System.out.println(newScript);

            //Return the fixed script
            return newScript.toString();
        } catch (Exception ex) {
            //Exception: Failed to fix the DDL or something just went plain wrong
            throw new RuntimeException("Failed to validate the CreateDDL-script for SQLite", ex);
        }
    }

    private void disableDatabaseLogging(boolean logging) {
        //If logging is allowed, nothing has to be changed
        if (logging) {
            return;
        }

        //Retrieve the level of the root logger
        loggerLevel = Logger.getLogger("").getLevel();

        //Set the level of the root logger to OFF
        Logger.getLogger("").setLevel(Level.OFF);
    }

    private void enableDatabaseLogging(boolean logging) {
        //If logging is allowed, nothing has to be changed
        if (logging) {
            return;
        }

        //Set the level of the root logger back to the original value
        Logger.getLogger("").setLevel(loggerLevel);
    }

    /**
     * Get a list of classes which should be registered with the EbeanServer
     *
     * @return List List of classes which should be registered with the EbeanServer
     */
    protected List<Class<?>> getDatabaseClasses() {
        return new ArrayList<Class<?>>();
    }

    /**
     * Method called before the loaded database is being dropped
     */
    protected void beforeDropDatabase() {
    }

    /**
     * Method called after the loaded database has been created
     */
    protected void afterCreateDatabase() {
    }

    /**
     * Method called near the end of prepareDatabase, before the dataSourceConfig is attached to the serverConfig.
     *
     * @param dataSourceConfig
     * @param serverConfig
     */
    protected void prepareDatabaseAdditionalConfig(DataSourceConfig dataSourceConfig, ServerConfig serverConfig) {
    }

    /**
     * Get the instance of the EbeanServer
     *
     * @return EbeanServer Instance of the EbeanServer
     */
    public EbeanServer getDatabase() {
        return ebeanServer;
    }
}
