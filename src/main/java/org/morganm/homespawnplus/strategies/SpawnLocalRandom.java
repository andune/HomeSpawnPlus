/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.morganm.homespawnplus.config.ConfigOptions;
import org.morganm.homespawnplus.entity.Spawn;
import org.morganm.homespawnplus.strategy.BaseStrategy;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyMode;
import org.morganm.homespawnplus.strategy.StrategyResult;

/** Spawn at a random spawn point on the local world. For example, if
 * you have defined "spawn1", "spawn2" and "spawn3" on the local world,
 * this strategy will choose one of them at random.
 * 
 * @author morganm
 *
 */
public class SpawnLocalRandom extends BaseStrategy {
	private Random random = new Random(System.currentTimeMillis());
	
	@Override
	public StrategyResult evaluate(StrategyContext context) {
		Spawn spawn = null;
		
		final boolean excludeNewPlayerSpawn = context.isModeEnabled(StrategyMode.MODE_EXCLUDE_NEW_PLAYER_SPAWN);
		
		String playerLocalWorld = context.getEventLocation().getWorld().getName();
		Set<Spawn> allSpawns = plugin.getStorage().getSpawnDAO().findAllSpawns();
		ArrayList<Spawn> spawnChoices = new ArrayList<Spawn>(5);
		for(Spawn theSpawn : allSpawns) {
			// skip newPlayerSpawn if so directed
			if( excludeNewPlayerSpawn && ConfigOptions.VALUE_NEW_PLAYER_SPAWN.equals(theSpawn.getName()) ) {
				debug.debug("Skipped spawn choice ",theSpawn," because mode ",StrategyMode.MODE_EXCLUDE_NEW_PLAYER_SPAWN," is enabled");
				continue;
			}
			
			if( playerLocalWorld.equals(theSpawn.getWorld()) ) {
				spawnChoices.add(theSpawn);
			}
		}
		if( spawnChoices.size() > 0 ) {
			int randomChoice = random.nextInt(spawnChoices.size());
			spawn = spawnChoices.get(randomChoice);
		}
		
		return new StrategyResult(spawn);
	}

	@Override
	public String getStrategyConfigName() {
		return "spawnLocalRandom";
	}

}
