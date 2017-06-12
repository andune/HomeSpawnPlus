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
package com.andune.minecraft.hsp.strategy;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;
import com.andune.minecraft.hsp.util.BedUtils;
import com.andune.minecraft.hsp.util.HomeUtil;

import javax.inject.Inject;
import java.util.Set;

/**
 * Methods that are useful for Home-related strategies.
 *
 * @author andune
 */
public abstract class HomeStrategy extends BaseStrategy {
    protected static Logger log = LoggerFactory.getLogger(HomeStrategy.class);

    //    protected Storage storage;
    protected HomeDAO homeDAO;
    protected HomeUtil homeUtil;
    protected BedUtils bedUtil;

    //    @Inject public void setStorage(Storage storage) { this.storage = storage; }
    @Inject
    public void setHomeDAO(HomeDAO homeDAO) {
        this.homeDAO = homeDAO;
    }

    @Inject
    public void setHomeUtil(HomeUtil homeUtil) {
        this.homeUtil = homeUtil;
    }

    @Inject
    public void setBedUtil(BedUtils bedUtil) {
        this.bedUtil = bedUtil;
    }

    /**
     * Taking mode into account, find the default home on a given world. This may
     * return just a bed home, or not a bed at all or even any home, all depending on the
     * home mode that is set.
     *
     * @return the home matching the current mode on the given world, or null
     */
    protected Home getModeHome(final StrategyContext context, final String worldNameArg) {
        final String playerName = context.getPlayer().getName();

        String worldName = worldNameArg;
        // if worldName is null, attempt to set it from context, if available
        if (worldName == null)
            if (context.getEventLocation().getWorld() != null)
                worldName = context.getEventLocation().getWorld().getName();

        log.debug("getModeHome() worldName={}, worldNameArg={}, location={}", worldName, worldNameArg, context.getEventLocation());

        Home home = null;
        // cache whether or not we are checking distance for efficiency
        final boolean distanceCheckEnabled = context.isModeEnabled(StrategyMode.MODE_DISTANCE_LIMITS);

        // If the mode is NORMAL, DEFAULT_ONLY or NO_BED, then we start by grabbing the
        // default home and then apply the modes based on the status of that home
        if (context.isInHomeDefaultMode()
                || context.isModeEnabled(StrategyMode.MODE_HOME_DEFAULT_ONLY)
                || context.isModeEnabled(StrategyMode.MODE_HOME_NO_BED)) {
            home = homeUtil.getDefaultHome(playerName, worldName);

            // if mode is MODE_HOME_NO_BED and the default home is a bed, don't use it
            if (home != null && home.isBedHome() && context.isModeEnabled(StrategyMode.MODE_HOME_NO_BED)) {
                logVerbose("Home ", home, " skipped because MODE_HOME_NO_BED is true and home was set by bed");
                home = null;
            }

            if (home != null && context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(home)) {
                logVerbose("Home ", home, " skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
                home = null;
            }

            if (home != null && distanceCheckEnabled && !context.checkDistance(home.getLocation())) {
                logVerbose("Home ", home, " skipped because MODE_DISTANCE_LIMITS is enabled and home is not within distance bounds");
                home = null;
            }
        }

        // If we haven't found a home by this point, check to see if we are in a NORMAL
        // or BED_ONLY mode and if so, look for a bedHome to satisfy this condition
        if (home == null && (context.isInHomeDefaultMode() ||
                context.isModeEnabled(StrategyMode.MODE_HOME_BED_ONLY)) &&
                !context.isModeEnabled(StrategyMode.MODE_HOME_NO_BED)) {
            home = getBedHome(playerName, worldName);

            if (context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(home)) {
                logVerbose("Home ", home, " skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
                home = null;
            }

            if (home != null && distanceCheckEnabled && !context.checkDistance(home.getLocation())) {
                logVerbose("Home ", home, " skipped because MODE_DISTANCE_LIMITS is enabled and home is not within distance bounds");
                home = null;
            }
        }

        // If we haven't found a home by this point and we are in MODE_ANY, then just
        // try to grab any home we can find.
        if (home == null && context.isModeEnabled(StrategyMode.MODE_HOME_ANY)) {
            Set<? extends Home> homes = homeDAO.findHomesByPlayer(playerName);
            // just grab the first one we find
            if (homes != null && homes.size() != 0) {
                home = homes.iterator().next();

                if (context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED) && !isBedNearby(home)) {
                    logVerbose("Home ", home, " skipped because MODE_HOME_REQUIRES_BED is true and no bed is nearby the home location");
                    home = null;
                }

                if (distanceCheckEnabled && !context.checkDistance(home.getLocation())) {
                    logVerbose("Home ", home, " skipped because MODE_DISTANCE_LIMITS is enabled and home is not within distance bounds");
                    home = null;
                }
            }
        }

        return home;
    }

    /**
     * Given a player and world, return the home that is defined as the
     * bedHome (if any).
     *
     * @param playerName
     * @param worldName
     * @return
     */
    private Home getBedHome(String playerName, String worldName) {
        Home bedHome = null;

        Set<? extends Home> homes = homeDAO.findHomesByWorldAndPlayer(worldName, playerName);
        if (homes != null && homes.size() != 0) {
            for (Home home : homes) {
                if (home.isBedHome()) {
                    bedHome = home;
                    break;
                }
            }
        }

        return bedHome;
    }

    /**
     * Look for a nearby bed to the given home.
     *
     * @param home
     * @return true if a bed is nearby, false if not
     */
    public boolean isBedNearby(final Home home) {
        return bedUtil.isBedNearby(home);
    }
}
