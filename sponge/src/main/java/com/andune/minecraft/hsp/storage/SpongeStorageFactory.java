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
package com.andune.minecraft.hsp.storage;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.hsp.config.ConfigStorage;
import com.andune.minecraft.hsp.config.ConfigStorage.Type;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author andune
 */
@Singleton
public class SpongeStorageFactory extends BaseStorageFactory implements Initializable {
    @Inject
    public SpongeStorageFactory(ConfigStorage configStorage, Injector injector, Plugin plugin) {
        super(configStorage, injector, plugin);
    }

    @Override
    public Storage getInstance() {
        if (storageInstance != null)
            return storageInstance;

        Type storageType = configStorage.getStorageType();
        log.debug("SpongeStorageFactory.getInstance(), type = {}", storageType);

        switch (storageType) {
            // TODO: At some point, provide a Sponge-native HOCON storage type

            default:
                // do nothing, super call below will do any additional storage resolution
        }

        final Storage storage = super.getInstance();
        log.debug("SpongeStorageFactory.getInstance() returning {}", storage);
        return storage;
    }
}
