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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.text.DecimalFormat;

/**
 * @author andune
 */
@Entity()
@Table(name = "hsp_home",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"player_name", "name"})
        }
)
public class HomeImpl implements EntityWithLocation, Home {
    static private final transient DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    @Size(min = 1)
    @Column(name = "player_name", length = 32)
    private String playerName;

    @Size(max = 32)
    private String name;

    @NotNull
    @Size(min = 1, max = 32)
    private String updatedBy;

    @NotNull
    @Size(min = 1, max = 32)
    private String world;

    @NotNull
    private Double x;
    @NotNull
    private Double y;
    @NotNull
    private Double z;

    private Float pitch;
    private Float yaw;

    @NotNull
    @Column(name = "bed_home")
    private boolean bedHome = false;
    @NotNull
    @Column(name = "default_home")
    private boolean defaultHome = false;

    @Version
    private Timestamp lastModified;

    @CreatedTimestamp
    private Timestamp dateCreated;

    @Transient
    private transient Location location;

    public HomeImpl() {
    }

    /**
     * Create a new Home object given the player and location.
     *
     * @param playerName
     * @param l
     */
    public HomeImpl(String playerName, Location l, String updatedBy) {
        setPlayerName(playerName);
        setLocation(l);
        setUpdatedBy(updatedBy);
    }

    @Override
    public void setLocation(Location l) {
        setWorld(l.getWorld().getName());
        setX(l.getX());
        setY(l.getY());
        setZ(l.getZ());
        setYaw(l.getYaw());
        setPitch(l.getPitch());

        location = l;
    }

    @Override
    public Location getLocation() {
        if (location == null) {
            location = ObjectFactory.newLocation(getWorld(), getX(), getY(), getZ(), getYaw(), getPitch());
        }

        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof HomeImpl))
            return false;
        if (o == this)            // java object equality check
            return true;

        if (((HomeImpl) o).getId() == getId())
            return true;
        else
            return false;
    }

    /**
     * We simple return the parent hashCode, since our .equals() check is simply checking ids.
     *
     * @return
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return "{id=" + getId()
                + ",name=" + getName()
                + ",playerName=" + getPlayerName()
                + ",world=" + getWorld()
                + ",x=" + decimalFormat.format(getX())
                + ",y=" + decimalFormat.format(getY())
                + ",z=" + decimalFormat.format(getZ())
                + ",yaw=" + decimalFormat.format(getYaw())
                + ",pitch=" + decimalFormat.format(getPitch())
                + ",bedHome=" + isBedHome()
                + ",defaultHome=" + isDefaultHome()
                + "}";
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getId()
     */
    @Override
    public int getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setId(int)
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getPlayerName()
     */
    @Override
    public String getPlayerName() {
        return playerName;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setPlayerName(java.lang.String)
     */
    @Override
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getWorld()
     */
    @Override
    public String getWorld() {
        return world;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setWorld(java.lang.String)
     */
    @Override
    public void setWorld(String world) {
        this.world = world;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getX()
     */
    @Override
    public Double getX() {
        return x;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setX(java.lang.Double)
     */
    @Override
    public void setX(Double x) {
        this.x = x;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getY()
     */
    @Override
    public Double getY() {
        return y;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setY(java.lang.Double)
     */
    @Override
    public void setY(Double y) {
        this.y = y;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getZ()
     */
    @Override
    public Double getZ() {
        return z;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setZ(java.lang.Double)
     */
    @Override
    public void setZ(Double z) {
        this.z = z;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getUpdatedBy()
     */
    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setUpdatedBy(java.lang.String)
     */
    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getLastModified()
     */
    @Override
    public Timestamp getLastModified() {
        return lastModified;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setLastModified(java.sql.Timestamp)
     */
    @Override
    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getDateCreated()
     */
    @Override
    public Timestamp getDateCreated() {
        return dateCreated;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setDateCreated(java.sql.Timestamp)
     */
    @Override
    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getPitch()
     */
    @Override
    public Float getPitch() {
        return pitch;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setPitch(java.lang.Float)
     */
    @Override
    public void setPitch(Float pitch) {
        this.pitch = pitch;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getYaw()
     */
    @Override
    public Float getYaw() {
        return yaw;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setYaw(java.lang.Float)
     */
    @Override
    public void setYaw(Float yaw) {
        this.yaw = yaw;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#isBedHome()
     */
    @Override
    public boolean isBedHome() {
        return bedHome;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setBedHome(boolean)
     */
    @Override
    public void setBedHome(boolean bedHome) {
        this.bedHome = bedHome;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#isDefaultHome()
     */
    @Override
    public boolean isDefaultHome() {
        return defaultHome;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setDefaultHome(boolean)
     */
    @Override
    public void setDefaultHome(boolean defaultHome) {
        this.defaultHome = defaultHome;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Home#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }
}
