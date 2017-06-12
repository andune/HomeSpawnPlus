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
package com.andune.minecraft.hsp.config;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.commonlib.server.api.event.EventPriority;
import com.andune.minecraft.hsp.util.LogUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;


/**
 * @author andune
 */
@Singleton
@ConfigOptions(fileName = "core.yml", basePath = "core")
public class ConfigCore extends ConfigBase implements Initializable {
    @Inject
    private Colors colors;
    /**
     * If true, it means the admin has overridden the config debug value,
     * so all future debug checks will use the admin-specified debug level.
     */
    private boolean debugOverride = false;
    private boolean debug = false;
    /**
     * If event priority is missing, we log a message about it once and
     * record that we did so we don't spam the log every time the priority
     * is checked.
     */
    private boolean missingEventPriorityLogMessage = false;

    @Override
    public int getInitPriority() {
        return 2;
    }  // core config has lower priority than default

    @Override
    public void init() throws Exception {
        super.init();

        log.debug("ConfigCore init: defaultColor={}", getDefaultColor());
        colors.setDefaultColor(getDefaultColor());

        missingEventPriorityLogMessage = false;
    }

    /**
     * Return the configured locale, such as "en", "de", "fr", etc.
     *
     * @return
     */
    public String getLocale() {
        return super.getString("locale");
    }

    public boolean isDebug() {
        // if admin has overridden config debug level, use that instead
        if (debugOverride)
            return debug;
        else
            return super.getBoolean("debug");
    }

    /**
     * Debug is unique in that it can be enabled/disabled at runtime and
     * that will override the config value.
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debugOverride = true;
        this.debug = debug;
        if (this.debug)
            LogUtil.enableDebug();
        else
            LogUtil.disableDebug();
    }

    /**
     * Is verbose logging enabled?
     *
     * @return true if verbose logging is enabled
     */
    public boolean isVerboseLogging() {
        return super.getBoolean("verboseLogging");
    }

    /**
     * Is verbose strategy logging enabled?
     *
     * @return
     */
    public boolean isVerboseStrategyLogging() {
        return super.getBoolean("verboseStrategyLogging");
    }

    /**
     * Is safe teleport mode enabled?
     *
     * @return true if safe teleport is enabled
     */
    public boolean isSafeTeleport() {
        return super.getBoolean("safeTeleport");
    }

    /**
     * Millisecond value for controlling performance-related warnings.
     *
     * @return
     */
    public int getPerformanceWarnMillis() {
        return super.getInt("warnPerformanceMillis");
    }

    /**
     * Determine if the last home on a given world is always considered
     * the default.
     *
     * @return true if the last home is the default
     */
    public boolean isLastHomeDefault() {
        return super.getBoolean("lastHomeIsDefault");
    }

    /**
     * Determine if teleport messages should be sent on home/spawn
     * commands to tell the player they have arrived.
     *
     * @return
     */
    public boolean isTeleportMessages() {
        return super.getBoolean("teleportMessages");
    }

    /**
     * Determine if named permissions should be used for spawns, such that
     * if a player types "/spawn spawn1", a specific permission for spawn1
     * will be checked, such as "hsp.command.spawn.named.spawn1"
     *
     * @return
     */
    public boolean isSpawnNamedPermissions() {
        return super.getBoolean("spawnNamedPermissions");
    }

    /**
     * Determine if setting default world spawn should also set the
     * spawn for that world on the backing server (ie. in the MC maps
     * itself).
     *
     * @return
     */
    public boolean isOverrideWorld() {
        return super.getBoolean("override_world");
    }

    /**
     * Determine if sleeping in a bed should overwrite the defaultHome if a
     * bed home isn't found. In practice, when set, this means the bedHome
     * and the defaultHome will be the same.
     *
     * @return
     */
    public boolean isBedHomeOverwriteDefault() {
        return super.getBoolean("bedHomeOverwritesDefault");
    }

    /**
     * Return a string representing the default color, of the form
     * "%yellow", "%red%", etc.
     *
     * @return
     */
    public String getDefaultColor() {
        return super.getString("defaultMessageColor");
    }

    /**
     * Return the default world, used anywhere "default world" is referenced
     * in spawn strategies.
     *
     * @return
     */
    public String getDefaultWorld() {
        return super.getString("defaultWorld");
    }

    /**
     * Boolean value to control whether or not HSP monitors events to see
     * if the player is appearing somewhere other than where HSP intended
     * and prints out warnings to the log if so.
     *
     * @return
     */
    public boolean isWarnLocationChange() {
        return super.getBoolean("warnLocationChange");
    }

    /**
     * Boolean value to control whether or not sleeping in a bed sets a
     * player's HSP home to that location. If this config value is false,
     * HSP ignores bed events entirely.
     *
     * @return
     */
    public boolean isBedSetHome() {
        return super.getBoolean("bedsethome");
    }

    /**
     * Boolean value to control whether or not homes can be set during
     * the day. If this value is true, HSP follows "vanilla Minecraft"
     * behavior and bed homes can only be set at night.
     *
     * @return
     */
    public boolean isBedHomeMustBeNight() {
        return super.getBoolean("bedHomeMustBeNight");
    }

    /**
     * By default, HSP has "2-click" protection enabled, which requires
     * a player to click twice to save their home to a bed; also by
     * default HSP will cancel the first click of the 2-click safety,
     * which avoids an extra "You can only sleep at night" message when
     * clicking during daylight.
     * <p/>
     * Some admins preferred the original behavior where HSP did not
     * cancel the first click, so this config parameter can be used
     * to bring back that behavior.
     *
     * @return
     */
    public boolean isBedHomeOriginalBehavior() {
        return super.getBoolean("bedHomeOriginalBehavior");
    }

    /**
     * HSP allows players to set their bedHome during the day, but
     * vanilla Minecraft will still print the "You can only sleep
     * at night" message. With this option enabled, HSP will
     * suppress that message.
     * <p/>
     * However, it comes at the cost of canceling the bed click
     * which means players can never actually sleep in a bed.
     *
     * @return
     */
    public boolean isBedNeverDisplayNightMessage() {
        return super.getBoolean("bedHomeNeverDisplayNightMessage");
    }

    /**
     * Boolean value that determines whether or not 2 clicks are
     * required to set a bed home. If false, only 1 click is
     * required.
     *
     * @return
     */
    public boolean isBedHome2Clicks() {
        return super.getBoolean("bedhome2clicks");
    }

    /**
     * Boolean value to control whether or not HSP records the
     * location when a player logs out.
     *
     * @return
     */
    public boolean isRecordLastLogout() {
        return super.getBoolean("recordLastLogout");
    }

    /**
     * Boolean value that determines whether Multiverse integration
     * is enabled or not.
     *
     * @return
     */
    public boolean isMultiverseEnabled() {
        return super.getBoolean("multiverseEnabled");
    }

    /**
     * If true, HSP will run UUID cleanup when it starts up. To be
     * used if there are duplicate UUIDs in the player table as a
     * result of name changes that didn't get migrated automatically.
     */
    public boolean isUuidCleanupOnStartup() {
        return super.getBoolean("uuidCleanupOnStartup");
    }

    /**
     * If true, HSP will treat child worlds ("_nether" and "_the_end") as
     * if they were running on the main world. This results in a default
     * experience more like stock, but for more advanced setups where you
     * expect strategies like "homeLocalWorld" to always refer to the
     * world the player is standing on, you would want to turn this off.
     */
    public boolean isAssociatedWorldsUseBaseStrategies() {
        return super.getBoolean("associatedWorldsUseBaseStrategies");
    }

    /**
     * Return the configured string for the storage type we should
     * use. Examples: "ebeans", "yaml"
     *
     * @return
     */
//    @deprecated moved to ConfigStorage
//    public String getStorageType() {
//        return super.getString("storage");
//    }

    public enum NewPlayerStrategy {
        ORIGINAL,
        BUKKIT,
        PLAYER_DAT,
        ORIGINAL_ONLY
    }

    /**
     * Return the strategy used to determine if a player is new.
     *
     * @return
     */
    public NewPlayerStrategy getNewPlayerStrategy() {
        String strategyString = super.getString("newPlayerStrategy");

        // default strategy if no other valid strategy is specified
        NewPlayerStrategy strategy = NewPlayerStrategy.PLAYER_DAT;

        if (NewPlayerStrategy.ORIGINAL.toString().equals(strategyString))
            strategy = NewPlayerStrategy.ORIGINAL;
        else if (NewPlayerStrategy.BUKKIT.toString().equals(strategyString))
            strategy = NewPlayerStrategy.BUKKIT;
        else if (NewPlayerStrategy.PLAYER_DAT.toString().equals(strategyString))
            strategy = NewPlayerStrategy.PLAYER_DAT;
        else if (NewPlayerStrategy.ORIGINAL_ONLY.toString().equals(strategyString))
            strategy = NewPlayerStrategy.ORIGINAL_ONLY;

        return strategy;
    }

    /**
     * Return default permission list. These permissions will be true for all
     * players, regardless of what permission system is in use (even if none at
     * all). Note this only applies to HSP permission checks.
     *
     * @return
     */
    public List<String> getDefaultPermissions() {
        return super.getStringList("defaultPermissions");
    }

    /**
     * Boolean value to control whether or not HSP records Last Location
     * on player teleport. This must be true in order for the SpawnLastLocation
     * strategy to work properly.
     *
     * @return
     */
    public boolean isRecordLastLocation() {
        return super.getBoolean("recordLastLocation");
    }

    /**
     * Boolean value to control whether or not HSP uses the database lower()
     * method when searching for player's names. Some installations don't
     * support or have access to this method, so it can be globally disabled
     * with this config options.
     *
     * @return
     */
    public boolean useEbeanSearchLower() {
        return super.getBoolean("useEbeanSearchLower");
    }

    /**
     * Boolean to determine whether we warn when missing configuration
     * items are detected and defaults are used.
     *
     * @return
     */
    public boolean isWarnMissingConfigItems() {
        return super.getBoolean("warnMissingConfigItems");
    }

    /**
     * Return the admin-defined event priority.
     *
     * @return
     */
    public EventPriority getEventPriority() {
        EventPriority priority = EventPriority.HIGHEST; // default if nothing else is defined
        if( missingEventPriorityLogMessage )
            return priority;

        String arg = super.getString("eventPriority");
        if( arg == null ) {
            missingEventPriorityLogMessage = true;
            log.warn("No event priority defined in config. Using default event priority {}", priority);
            return priority;
        }

        try {
            priority = EventPriority.valueOf(arg.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid eventPriority: \"{}\", defaulting to {}", arg, priority);
            missingEventPriorityLogMessage = true;
        } catch (Exception e) {
            log.warn("Caught exception getting Event priority, using default event priority", e);
            missingEventPriorityLogMessage = true;
        }

        return priority;
    }
}
