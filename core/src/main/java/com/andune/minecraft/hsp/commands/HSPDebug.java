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
package com.andune.minecraft.hsp.commands;

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.config.ConfigCommand;
import com.andune.minecraft.hsp.config.ConfigCore;

import javax.inject.Inject;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author andune
 */
@UberCommand(uberCommand = "hsp", subCommand = "debug",
        aliases = {"d"}, help = "HSP debug commands")
public class HSPDebug extends BaseCommand {
    @Inject
    private ConfigCore configCore;
    @Inject
    private ConfigCommand configCommand;

    private boolean rootHandlerInstalled = false;

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_HSPDEBUG_USAGE);
    }

    @Override
    public boolean execute(final CommandSender sender, String cmd, String[] args) {
        if (!permissions.isAdmin(sender))
            return true;

        if (args.length < 1) {
            return false;
        } else if (args[0].startsWith("ccd")) {
            // config core dump
            sender.sendMessage("defaultColor: "+configCore.getDefaultColor());
            sender.sendMessage("defaultPermissions: "+configCore.getDefaultPermissions());
            sender.sendMessage("eventPriority: "+configCore.getEventPriority());
            sender.sendMessage("performanceWarnMillis: "+configCore.getPerformanceWarnMillis());
            sender.sendMessage("useEbeanSearchLower: "+configCore.useEbeanSearchLower());
            sender.sendMessage("isUberCommandsEnabled: "+configCommand.isUberCommandsEnabled());
        } else if (args[0].startsWith("t")) {    // toggle debug mode
            boolean current = configCore.isDebug();
            configCore.setDebug(!current);
            sender.sendMessage("HSP DEBUG flag toggled to " + !current);
        } else if (args[0].startsWith("log")) {
            java.util.logging.Logger log = java.util.logging.Logger.getLogger("Minecraft");
            java.util.logging.Logger rootLog = java.util.logging.Logger.getLogger("");

            if (!rootHandlerInstalled) {
                try {
                    java.util.logging.Handler handler = new java.util.logging.FileHandler("server_full.log");
                    handler.setLevel(Level.FINEST);
                    // do some smart filtering of messages later
                    java.util.logging.Formatter formatter = new java.util.logging.Formatter() {
                        @Override
                        public String format(LogRecord record) {
                            StringBuffer s = new StringBuffer();
                            s.append("[");
                            s.append(record.getLevel().toString());
                            s.append("] ");
                            s.append("[");
                            s.append(record.getSourceClassName());
                            s.append("] ");

                            s.append(super.formatMessage(record));
                            return s.toString();
                        }
                    };
                    handler.setFormatter(formatter);

                    // install new handler
                    rootLog.addHandler(handler);

                    rootHandlerInstalled = true;
                } catch (java.io.IOException e) {
                }
            }

            try {
                java.util.logging.Logger ebeansLogger = java.util.logging.Logger.getLogger("com.avaje");
                java.util.logging.Handler handler = new java.util.logging.FileHandler("ebean.log");
                handler.setLevel(Level.SEVERE);
                // do some smart filtering of messages later
                java.util.logging.Formatter formatter = new java.util.logging.Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        return super.formatMessage(record);
                    }
                };
                handler.setFormatter(formatter);

                // cleanup any old handlers first
                java.util.logging.Handler[] handlers = ebeansLogger.getHandlers();
                for (java.util.logging.Handler h : handlers) {
                    ebeansLogger.removeHandler(h);
                }

                // install new handler
                ebeansLogger.addHandler(handler);
            } catch (java.io.IOException e) {
            }

            sender.sendMessage("Minecraft logger Handlers:");
            java.util.logging.Handler[] handlers = log.getHandlers();
            for (java.util.logging.Handler handler : handlers) {
                sender.sendMessage("  Handler: " + handler);
            }

            sender.sendMessage("Root Handlers:");
            handlers = rootLog.getHandlers();
            for (java.util.logging.Handler handler : handlers) {
                sender.sendMessage("  Handler: " + handler);
                java.util.logging.Formatter formatter = handler.getFormatter();
                sender.sendMessage("  Formatter: " + formatter);
            }

            return true;
        }
        /* disabled since new version of Ebeans no longer has the isBeanCaching() method and I'd
           like to remove as much extraneous references to ebeans in the code as possible anyway.
           I highly doubt any admin ever used this capability or will notice/care that it's gone.
           
        else if (args[0].startsWith("eb")) {    // ebeans cache stats
            if (storage instanceof StorageEBeans) {
                StorageEBeans ebeansStorage = (StorageEBeans) storage;
                EbeanServer ebeans = ebeansStorage.getDatabase();
                ServerCacheManager scm = ebeans.getServerCacheManager();
                StringBuffer sb = new StringBuffer();
                List<Class<?>> classes = StorageEBeans.getDatabaseClasses();
                for (Class<?> clazz : classes) {
                    if (scm.isBeanCaching(clazz)) {
                        sb.append("Cache stats for class " + clazz.getName() + "\n");
                        ServerCacheStatistics scs = scm.getBeanCache(clazz).getStatistics(false);
                        sb.append("  Bean Cache size = " + scs.getSize() + "\n");
                        sb.append("  Bean Cache Hit ratio = " + scs.getHitRatio() + "\n");
                        scs = scm.getQueryCache(clazz).getStatistics(false);
                        sb.append("  Query Cache size = " + scs.getSize() + "\n");
                        sb.append("  Query Cache Hit ratio = " + scs.getHitRatio() + "\n");
                    } else
                        sb.append("Cache disabled for " + clazz.getName() + "\n");
                }
                sender.sendMessage(sb.toString());
            } else {
                sender.sendMessage("Ebeans storage not in use");
            }
            */
        else if (args[0].startsWith("uuid")) {    // ebeans cache stats
            if( sender instanceof Player ) {
                Player p = (Player) sender;
                UUID uuid = p.getUUID();
                sender.sendMessage("Your UUID is "+uuid);
            }
        } else {
            return false;
        }

        return true;
    }
}
