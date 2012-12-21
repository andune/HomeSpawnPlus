/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.io.File;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.homespawnplus.HomeSpawnPlus;
import org.morganm.homespawnplus.storage.ebean.StorageEBeans;

/** This class is the interface to Bukkit's Plugin interface. This is abstracted from
 * the rest of the plugin so as to minimize impact to the code when Bukkit makes
 * API changes and to simplify supporting MC-API, Spout or other frameworks.
 * 
 * @author morganm
 *
 */
public class HSPBukkit extends JavaPlugin {
    private HomeSpawnPlus mainClass;

    @Override
    public void onEnable() {
        enableDebug();

        mainClass = new HomeSpawnPlus(this);
        try {
            mainClass.onEnable();
        }
        catch(Exception e) {
            getLogger().log(Level.SEVERE, "Caught exception loading plugin, shutting down", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if( mainClass != null )
            mainClass.onDisable();
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        return StorageEBeans.getDatabaseClasses();
    }
    
    public File _getJarFile() {
        return super.getFile();
    }

    public ClassLoader _getClassLoader() {
        return super.getClassLoader();
    }

    // TODO: move to an interface
    private void enableDebug() {
        getConsoleHandler(Logger.getLogger("Minecraft")).setLevel(Level.FINEST);
//        Logger debugLog = Logger.getLogger("org.morganm.homespawnplus");
//        debugLog.setLevel(Level.FINEST);
//        debugLog.setUseParentHandlers(true);
    }
    private Handler getConsoleHandler(Logger log) {
        Handler[] handlers = log.getHandlers();
        for(int i=0; i < handlers.length; i++)
            if( handlers[i] instanceof ConsoleHandler )
                return handlers[i];

        Logger parent = log.getParent();
        if( parent != null )
            return getConsoleHandler(parent);
        else
            return null;
    }
}