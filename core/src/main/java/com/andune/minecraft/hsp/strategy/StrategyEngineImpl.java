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
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.commonlib.server.api.TeleportOptions;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.entity.PlayerSpawn;
import com.andune.minecraft.hsp.entity.Spawn;
import com.andune.minecraft.hsp.manager.EffectsManager;
import com.andune.minecraft.hsp.server.api.Factory;
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.PlayerSpawnDAO;
import com.andune.minecraft.hsp.strategies.mode.ModeEffect;
import com.andune.minecraft.hsp.util.SpawnUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * Class responsible for processing strategies at run-time.
 *
 * @author andune
 */
public class StrategyEngineImpl implements StrategyEngine {
    private final Logger log = LoggerFactory.getLogger(StrategyEngineImpl.class);
    private final ConfigCore config;
    private final StrategyConfig strategyConfig;
    private final Storage storage;
    private final Teleport teleport;
    private final Factory factory;
    private final StrategyResultFactory resultFactory;
    private final SpawnUtil spawnUtil;
    private final EffectsManager effectsManager;

    @Inject
    public StrategyEngineImpl(ConfigCore config, StrategyConfig strategyConfig, Storage storage,
                              Teleport teleport, Factory factory, StrategyResultFactory resultFactory,
                              SpawnUtil spawnUtil, EffectsManager effectsManager) {
        this.config = config;
        this.strategyConfig = strategyConfig;
        this.storage = storage;
        this.teleport = teleport;
        this.factory = factory;
        this.resultFactory = resultFactory;
        this.spawnUtil = spawnUtil;
        this.effectsManager = effectsManager;
    }

    public StrategyConfig getStrategyConfig() {
        return strategyConfig;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyEngine#getStrategyResult(com.andune.minecraft.hsp.strategy.StrategyContext, java.lang.String)
     */
    @Override
    public StrategyResult getStrategyResult(StrategyContext context, String... args) {
        if (context == null) {
            log.warn("null context received, doing nothing");
            return null;
        }

        if (args != null && args.length > 0)
            context.setArg(args[0]);

        StrategyResult result = evaluateStrategies(context);
        // decided these warnings are just annoying and of no value. -andune 7/26/12
//    	if( result == null && plugin.getConfig().getBoolean(ConfigOptions.WARN_NULL_STRATEGY, true) ) {
//			log.info(logPrefix + " strategy result is null for event "+context.getEventType()+"."
//					+ " This could indicate a configuration mistake."
//					+ " Either include \"default\" as the end of your strategy chains to avoid this warning, or set "
//					+ ConfigOptions.WARN_NULL_STRATEGY + " in your config.yml to false");
//    	}

        return result;
    }

    /**
     * Convenience method for routines only interested in an output location (as
     * opposed to other result details).
     *
     * @param event
     * @param player
     * @return
     */
    public Location getStrategyLocation(String event, Player player, String... args) {
        StrategyResult result = getStrategyResult(event, player, args);
        if (result != null)
            return result.getLocation();
        else
            return null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyEngine#getStrategyLocation(com.andune.minecraft.hsp.strategy.EventType, com.andune.minecraft.hsp.server.api.Player, java.lang.String)
     */
    @Override
    public Location getStrategyLocation(EventType event, Player player, String... args) {
        return getStrategyLocation(event.toString(), player, args);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyEngine#getStrategyResult(java.lang.String, com.andune.minecraft.hsp.server.api.Player, java.lang.String)
     */
    @Override
    public StrategyResult getStrategyResult(String event, Player player, String... args) {
        final StrategyContext context = factory.newStrategyContext();
        context.setPlayer(player);
        context.setEventType(event);
        return getStrategyResult(context, args);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyEngine#getStrategyResult(com.andune.minecraft.hsp.strategy.EventType, com.andune.minecraft.hsp.server.api.Player, java.lang.String)
     */
    @Override
    public StrategyResult getStrategyResult(EventType event, Player player, String... args) {
        return getStrategyResult(event.toString(), player, args);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.strategy.StrategyEngine#evaluateStrategies(com.andune.minecraft.hsp.strategy.StrategyContext)
     */
    @Override
    public StrategyResult evaluateStrategies(StrategyContext context) {
        long start = System.currentTimeMillis();
        log.debug("evaluateStrategies: INVOKED. type={} player={}", context.getEventType(), context.getPlayer());
        log.debug("evaluateStrategies: context={}", context);
        StrategyResult result = null;

        logVerbose("Strategy evaluation started, type=", context.getEventType(), " player=", context.getPlayer());

        correctEventLocation(context);    // modify event location if required

        log.debug("evaluateStrategies: checking for permission-based strategies");
        List<Set<Strategy>> permStrategies = strategyConfig.getPermissionStrategies(context.getEventType(), context.getPlayer());
        if (permStrategies != null && permStrategies.size() > 0) {
            log.debug("evaluateStrategies: evaluating {} permission strategies", permStrategies.size());
            for (Set<Strategy> set : permStrategies) {
                context.resetCurrentModes();

                result = evaluateStrategies(context, set);
                if (result != null && result.isSuccess())
                    break;
            }
            applyModeEffects(context);
            log.debug("evaluateStrategies: permission-based strategies result = {}", result);
        }

        log.debug("evaluateStrategies: checking for world-based strategies");
        // need to check world strategies if we don't yet have a successful result
        if (result == null || !result.isSuccess()) {
            // is it possible for player to have a null world when they first login? not sure
            // but lets be sure we don't blow up if it is. Only process world strategy if
            // player is in a world.
            if (context.getEventLocation().getWorld() != null) {
                final String locationWorld = context.getEventLocation().getWorld().getName();
                log.debug("evaluateStrategies: evaluating world-based strategies. locationWorld={}", locationWorld);
                Set<Strategy> worldStrategies = strategyConfig.getWorldStrategies(context.getEventType(), locationWorld);
                if (worldStrategies != null && worldStrategies.size() > 0) {
                    log.debug("evaluateStrategies: evaluating {} world strategies", worldStrategies.size());
                    context.resetCurrentModes();
                    result = evaluateStrategies(context, worldStrategies);
                }
                log.debug("evaluateStrategies: world-based strategies result = {}", result);
            }
            applyModeEffects(context);
        }

        // need to check default strategies if we don't yet have a successful result
        if (result == null || !result.isSuccess()) {
            log.debug("evaluateStrategies: evaluating default strategies");
            Set<Strategy> defaultStrategies = strategyConfig.getDefaultStrategies(context.getEventType());
            if (defaultStrategies != null && defaultStrategies.size() > 0) {
                log.debug("evaluateStrategies: evaluating {} default strategies", defaultStrategies.size());
                context.resetCurrentModes();
                result = evaluateStrategies(context, defaultStrategies);
            }
            log.debug("evaluateStrategies: default strategies result = {}", result);
            applyModeEffects(context);
        }

        if (result == null) {
            // decided these warnings are just annoying and of no value. -andune 7/26/12
//			log.warning(logPrefix + " Warning: no event strategy defined for event "+context.getEventType()+". If this is intentional, just define the event in config.yml with the single strategy \"default\" to avoid this warning.");
        } else {
            result.setContext(context);    // associate context with the result

            if (result.isExplicitDefault())
                logVerbose("Evaluation chain complete, result = explicit default");
            else
                logVerbose("Evaluation chain complete, result = ", result);
        }

        // if we have a result, make sure it's a safe location
        if (result != null && result.getLocation() != null && config.isSafeTeleport()) {
            final Location oldLocation = result.getLocation();
            final TeleportOptions options = context.getTeleportOptions();

            log.debug("evaluateStrategies(): Invoking safeLocation() for event={}, current startegy result={}", context.getEventType(), result);
            log.debug("evaluateStrategies(): options={}", options);
            Location safeLocation = teleport.safeLocation(oldLocation, options);
            if (safeLocation != null)
                result.setLocation(safeLocation);

            if (!oldLocation.equals(result.getLocation())) {
                logVerbose("result changed to safeLocation, new result = ", result);
                log.debug("evaluateStrategies: safeLocation changed to {} from {}", result.getLocation(), oldLocation);
            }
        }

        // are we supposed to remember a spawn?
        if (result != null && result.getSpawn() != null && context.isModeEnabled(StrategyMode.MODE_REMEMBER_SPAWN)) {
            PlayerSpawnDAO dao = storage.getPlayerSpawnDAO();
            PlayerSpawn ps = dao.findByWorldAndPlayerName(result.getSpawn().getWorld(), context.getPlayer().getName());
            if (ps == null) {
                ps = new PlayerSpawn();
                ps.setPlayerName(context.getPlayer().getName());
            }
            ps.setSpawn(result.getSpawn());
            try {
                dao.save(ps);
            } catch (StorageException e) {
                e.printStackTrace();
            }
            log.debug("evaluateStrategies: recorded PlayerSpawn spawn as directed by {}", StrategyMode.MODE_REMEMBER_SPAWN);
        }
        // no.. are we supposed to remember a location?
        else if (result != null && result.getLocation() != null && context.isModeEnabled(StrategyMode.MODE_REMEMBER_LOCATION)) {
            PlayerSpawnDAO dao = storage.getPlayerSpawnDAO();
            PlayerSpawn ps = dao.findByWorldAndPlayerName(result.getLocation().getWorld().getName(), context.getPlayer().getName());
            if (ps == null) {
                ps = new PlayerSpawn();
                ps.setPlayerName(context.getPlayer().getName());
            }
            ps.setSpawn(null);    // be sure to clear out spawn so this new location is used instead
            ps.setLocation(result.getLocation());
            try {
                dao.save(ps);
            } catch (StorageException e) {
                e.printStackTrace();
            }
            log.debug("evaluateStrategies: recorded PlayerSpawn location as directed by {}", StrategyMode.MODE_REMEMBER_LOCATION);
        }

        int warnMillis = config.getPerformanceWarnMillis();
        if (warnMillis > 0) {
            long totalTime = System.currentTimeMillis() - start;
            if (totalTime > warnMillis) {
                log.info("**LONG STRATEGY** Strategy took " + totalTime + " ms to run. (> warning threshold of " + warnMillis + "ms) Context: " + context);
            }
        }

        // we guarantee to return a result so the caller never has to worry about
        // a null result
        if (result == null) {
            log.debug("evaluateStrategies: no StrategyResult found, creating empty result");
            result = resultFactory.create(false, false);
        }

        applyModeEffects(context);

        log.debug("evaluateStrategies: exit result = {}", result);
        return result;
    }

    /**
     * Effect modes are unique in that they can be applied per-world or
     * per-permission and even if no location is found for that chain, the
     * mode can still apply. This allows an admin to easily create teleport
     * effects for all moderators, or everyone on world B, for example, without
     * having to duplicate entire event chains for each of them.
     *
     * @param context
     */
    private void applyModeEffects(final StrategyContext context) {
        List<ModeStrategy> modes = context.getModeList(StrategyMode.MODE_EFFECT);
        if (modes != null && modes.size() > 0) {
            for (ModeStrategy mode : modes) {
                ModeEffect modeEffect = (ModeEffect) mode;
                log.debug("applying mode effect {}", modeEffect.getEffect());
                effectsManager.addPlayerEffect(context.getPlayer(), modeEffect.getEffect(),
                        modeEffect.isToEffect(), modeEffect.isFromEffect());
                logVerbose("Special effect mode applied: {}", modeEffect.getEffect());
            }
        }

        context.clearEffectModes();
    }

    /**
     * Private method that takes an input Set of strategies and iterates through the set
     * looking for a positive result match.
     *
     * @param context
     * @param strategies
     * @return the StrategyResult of executing the strategies, possibly null
     */
    private StrategyResult evaluateStrategies(final StrategyContext context, final Set<Strategy> strategies) {
        StrategyResult result = null;
        log.debug("evaluateStrategies(private): ENTER hash={}, context={}", (context != null ? context.hashCode() : null), context);

        for (Strategy strat : strategies) {
            log.debug("evaluateStrategies(private): checking strategy {}", strat.getStrategyConfigName());

            // mode strategies are always allowed. otherwise, check to see if we are allowed
            // to process strategies based on the current modes
            if (!(strat instanceof ModeStrategyImpl) && !context.isStrategyProcessingAllowed()) {
                logVerboseStrategy(strat, "Strategy skipped due to current mode settings");
                continue;
            }

            result = strat.evaluate(context);

            if (result != null && !context.checkDistance(result.getLocation())) {
                logVerbose("Result ", result, " skipped because MODE_DISTANCE_LIMITS is enabled and result is not within distance bounds");
                result = null;
            }

            logStrategyResult(strat, result);
            if (result != null && result.isSuccess())
                break;
        }

        log.debug("evaluateStrategies(private): EXIT hash={}", (context != null ? context.hashCode() : null));
        return result;
    }

    /**
     * For worlds ending with "_nether" or "_the_end", we treat them as if
     * they were running on their base world. The only exception is if the
     * admin has defined a specific per-world event chain for those worlds,
     * then we will use the per-world events instead.
     *
     * @param context
     */
    private void correctEventLocation(final StrategyContext context) {
        log.debug("coorectEventLocation(): entry context={}", context);

        // we only consider changing the context if AssociatedWorldsUseBaseStrategies
        // configuration is enabled
        if (config.isAssociatedWorldsUseBaseStrategies()) {
            final Location eventLocation = context.getEventLocation();
            if (eventLocation != null) {
                final String worldName = eventLocation.getWorld().getName();

                // determine if this is a special world we should look at further
                int endLength = -1;
                if (worldName.endsWith("_nether")) {
                    endLength = 7;
                }
                if (worldName.endsWith("_the_end")) {
                    endLength = 8;
                }

                if (endLength > -1) {
                    // this is a special world, check for world-specific strategies
                    Set<Strategy> worldStrategies = strategyConfig.getWorldStrategies(context.getEventType(), worldName);
                    if (worldStrategies == null || worldStrategies.size() == 0) {
                        // if we get here, no world-specific strategies exist, so we
                        // change to the base world name instead and modify the
                        // context location.
                        final String baseWorld = worldName.substring(0, worldName.length() - endLength);
                        log.debug("No per-world strategies found for world {}, using base world {} as context location", worldName, baseWorld);

                        // use the spawn as the location if there is one
                        Spawn spawn = spawnUtil.getDefaultWorldSpawn(baseWorld);
                        if (spawn != null && spawn.getLocation() != null) {
                            context.setLocation(spawn.getLocation());
                        }
                        else {  // otherwise just use x=0,z=0
                            Location l = factory.newLocation(baseWorld, 0, 70, 0, 0, 0);
                            context.setLocation(l);
                        }
                    }
                }
            }
        }

        log.debug("coorectEventLocation(): exit context={}", context);
    }

    protected boolean isVerbose() {
        return config.isVerboseStrategyLogging();
    }

    protected void logVerbose(final Object... args) {
        if (isVerbose() || log.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i]);
            }
            final String msg = sb.toString();

            if (isVerbose())
                log.info(msg);

            log.debug(msg);
        }
    }

    /**
     * Log a verbose message, including the strategy identifier.
     *
     * @param strat
     * @param args
     */
    protected void logVerboseStrategy(final Strategy strat, final Object... args) {
        if (isVerbose()) {
            final String prefix = "(strategy " + strat.getStrategyConfigName() + ") ";
            Object[] newArgs = prepend(args, prefix);
            logVerbose(newArgs);
        }
    }

    protected void logStrategyResult(final Strategy strat, final StrategyResult result) {
        // we ignore logging for mode strategies
        if (!(strat instanceof ModeStrategyImpl))
            logVerboseStrategy(strat, "result is ", result);
    }

    /**
     * prepend an element in front of an existing array, creating and returning
     * the new array.
     *
     * @param arr
     * @param firstElement
     * @return
     */
    static private <T> T[] prepend(T[] arr, T firstElement) {
        final int N = arr.length;
        arr = java.util.Arrays.copyOf(arr, N + 1);
        System.arraycopy(arr, 0, arr, 1, N);
        arr[0] = firstElement;
        return arr;
    }
}
