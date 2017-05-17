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
package com.andune.minecraft.hsp.entity;

import com.andune.minecraft.commonlib.server.api.Location;
import io.ebean.annotation.CreatedTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

/**
 * Class to keep track of players we've seen before, so we can tell if it's a
 * new player or not.
 *
 * @author andune
 */
@Entity()
@Table(name = "hsp_player",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"})
        }
)
public class Player implements EntityWithLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    @Size(max = 32)
    private String name;

    /*
     * We store UUID as a String in the database so it's easily text readable by an admin,
     * as opposed to storing the binary uuid object. It's 20 more bytes, but even on a
     * server with 10,000 players, that's only 160k in extra DB storage. Well worth
     * the trade-off for admin readability.
     */
    @Size(max = 36)
    @Column(name = "uuid")
//    @NotNull
    private String UUIDString;

    @Size(max = 32)
    private String world;
    private Double x;
    private Double y;
    private Double z;

    private Float pitch;
    private Float yaw;

    @Version
    private Timestamp lastModified;

    @CreatedTimestamp
    private Timestamp dateCreated;

    public Player() {
    }

    public Player(com.andune.minecraft.commonlib.server.api.Player player) {
        this.name = player.getName();
        this.UUIDString = player.getUUID().toString();
    }

    /**
     * Update last logout location to the given location.
     *
     * @param l the location of the player
     */
    public void updateLastLogoutLocation(Location l) {
        setWorld(l.getWorld().getName());
        setX(l.getX());
        setY(l.getY());
        setZ(l.getZ());
        setYaw(l.getYaw());
        setPitch(l.getPitch());
    }

    public Location getLastLogoutLocation() {
        if (getWorld() == null)
            return null;

        return ObjectFactory.newLocation(getWorld(), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUUIDString() {
        return UUIDString;
    }

    public void setUUIDString(String UUIDString) {
        this.UUIDString = UUIDString;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public Float getPitch() {
        return pitch;
    }

    public void setPitch(Float pitch) {
        this.pitch = pitch;
    }

    public Float getYaw() {
        return yaw;
    }

    public void setYaw(Float yaw) {
        this.yaw = yaw;
    }
}
