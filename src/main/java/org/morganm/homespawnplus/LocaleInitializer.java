/**
 * 
 */
package org.morganm.homespawnplus;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.morganm.homespawnplus.config.ConfigCore;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.mBukkitLib.i18n.Locale;
import org.morganm.mBukkitLib.i18n.LocaleConfig;

/** Class responsible for initializing our Locale object. Priority
 * guarantees it runs after the config files have been loaded, which
 * is important so that we know what locale to use.
 *  
 * @author morganm
 *
 */
@Singleton
public class LocaleInitializer implements Initializable {
    private final ConfigCore configCore;
    private final Plugin plugin;
    private final Locale locale;
    
    @Inject
    public LocaleInitializer(ConfigCore configCore, Plugin plugin, Locale locale) {
        this.configCore = configCore;
        this.plugin = plugin;
        this.locale = locale;
    }

    @Override
    public void init() throws Exception {
        LocaleConfig localeConfig = new LocaleConfig(configCore.getLocale(),
                plugin.getDataFolder(), "hsp", plugin.getJarFile(), null);        
        locale.load(localeConfig);
    }

    @Override
    public int getPriority() {
        return 5;
    }

}