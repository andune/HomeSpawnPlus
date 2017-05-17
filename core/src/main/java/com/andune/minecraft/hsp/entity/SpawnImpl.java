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
import com.andune.minecraft.hsp.storage.Storage;
import com.andune.minecraft.hsp.storage.dao.SpawnDAO;
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

/**
 * @author andune
 */
@Entity()
@Table(name = "hsp_spawn",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"}),
                @UniqueConstraint(columnNames = {"world", "group_name"})
        }
)
public class SpawnImpl implements EntityWithLocation, Spawn {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    @Size(max = 32)
    private String world;

    @Size(max = 32)
    private String name;

    @NotNull
    @Size(max = 32)
    private String updatedBy;

    /* Optional group associated with this spawn.
     */
    @Size(max = 32)
    @Column(name = "group_name")
    private String group;

    @NotNull
    private Double x;
    @NotNull
    private Double y;
    @NotNull
    private Double z;

    private Float pitch;
    private Float yaw;

    @Version
    private Timestamp lastModified;

    @CreatedTimestamp
    private Timestamp dateCreated;

    @Transient
    private transient Location location;

    public SpawnImpl() {
    }

    /**
     * Create a new spawn object.
     *
     * @param l
     * @param updatedBy
     */
    public SpawnImpl(Location l, String updatedBy) {
        setLocation(l);
        setUpdatedBy(updatedBy);
//    	setGroup(group);
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

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#isNewPlayerSpawn()
	 */
    @Override
    public boolean isNewPlayerSpawn() {
        return SpawnDAO.NEW_PLAYER_SPAWN.equals(getName());
    }

    /* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.entity.Spawn#isDefaultSpawn()
	 */
    @Override
    public boolean isDefaultSpawn() {
        return Storage.HSP_WORLD_SPAWN_GROUP.equals(getGroup());
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getId()
     */
    @Override
    public int getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setId(int)
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getWorld()
     */
    @Override
    public String getWorld() {
        return world;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setWorld(java.lang.String)
     */
    @Override
    public void setWorld(String world) {
        this.world = world;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getX()
     */
    @Override
    public Double getX() {
        return x;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setX(java.lang.Double)
     */
    @Override
    public void setX(Double x) {
        this.x = x;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getY()
     */
    @Override
    public Double getY() {
        return y;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setY(java.lang.Double)
     */
    @Override
    public void setY(Double y) {
        this.y = y;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getZ()
     */
    @Override
    public Double getZ() {
        return z;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setZ(java.lang.Double)
     */
    @Override
    public void setZ(Double z) {
        this.z = z;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getGroup()
     */
    @Override
    public String getGroup() {
        return group;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setGroup(java.lang.String)
     */
    @Override
    public void setGroup(String group) {
        this.group = group;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getUpdatedBy()
     */
    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setUpdatedBy(java.lang.String)
     */
    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.entity.Spawn#getPitch()
	 */
    @Override
    public Float getPitch() {
        return pitch;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setPitch(java.lang.Float)
     */
    @Override
    public void setPitch(Float pitch) {
        this.pitch = pitch;
        location = null;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getYaw()
     */
    @Override
    public Float getYaw() {
        return yaw;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setYaw(java.lang.Float)
     */
    @Override
    public void setYaw(Float yaw) {
        this.yaw = yaw;
        location = null;
    }

    /* (non-Javadoc)
	 * @see com.andune.minecraft.hsp.entity.Spawn#getLastModified()
	 */
    @Override
    public Timestamp getLastModified() {
        return lastModified;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setLastModified(java.sql.Timestamp)
     */
    @Override
    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#getDateCreated()
     */
    @Override
    public Timestamp getDateCreated() {
        return dateCreated;
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.entity.Spawn#setDateCreated(java.sql.Timestamp)
     */
    @Override
    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }
}
