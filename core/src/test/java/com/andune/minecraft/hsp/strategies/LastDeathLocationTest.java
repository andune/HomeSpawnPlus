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
package com.andune.minecraft.hsp.strategies;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.hsp.manager.DeathManager;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyContextImpl;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.strategy.StrategyResultImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author andune
 */
public class LastDeathLocationTest extends BaseStrategyTest {
    @InjectMocks
    private LastDeathLocation objectUnderTest;

    @Mock
    protected Player player;
    @Mock
    protected DeathManager deathManager;
    @Mock
    protected Location location;

    StrategyResultImpl mockResult;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        // setup mock result object
        mockResult = mock(StrategyResultImpl.class);
        when(resultFactory.create(isA(Location.class))).thenReturn(mockResult);
        when(resultFactory.create(isNull(Location.class))).thenReturn(mockResult);
        when(mockResult.getLocation()).thenReturn(location);
    }

    @Test
    public void testDeathLocation() throws Exception {
        // Given
        when(deathManager.getLastDeathLocation(player)).thenReturn(location);
        StrategyContext context = mock(StrategyContextImpl.class);
        when(context.getPlayer()).thenReturn(player);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        // validate we created a location result
        verify(resultFactory).create(isA(Location.class));
        // validate we got our expected mock result back
        assertEquals(mockResult, result);
        // validate the location is as expected
        assertEquals(mockResult.getLocation(), location);
    }
}
