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
package com.andune.minecraft.hsp.server.bukkit;

import com.andune.minecraft.commonlib.i18n.Colors;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.ServerConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author andune
 */
@Singleton
public class BukkitServerConfig implements ServerConfig {
    private final ConfigCore configCore;
    private final Colors colors;

    @Inject
    public BukkitServerConfig(ConfigCore configCore, Colors colors) {
        this.configCore = configCore;
        this.colors = colors;
    }

    @Override
    public String getDefaultColor() {
        String configuredColor = configCore.getDefaultColor();
        return colors.getColorString(configuredColor);
    }

}
