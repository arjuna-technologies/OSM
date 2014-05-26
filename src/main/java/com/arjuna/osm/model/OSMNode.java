/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.osm.model;

import java.util.Date;
import java.util.List;

public class OSMNode
{
    public OSMNode()
    {
    }

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public void setUid(String uid)
    {
        _uid = uid;
    }

    public String getUid()
    {
        return _uid;
    }

    public String getVersion()
    {
        return _version;
    }

    public void setVersion(String version)
    {
        _version = version;
    }

    public Float getLatitude()
    {
        return _latitude;
    }

    public void setLatitude(Float latitude)
    {
        _latitude = latitude;
    }

    public Float getLongitude()
    {
        return _longitude;
    }

    public void setLongitude(Float longitude)
    {
        _longitude = longitude;
    }

    public String getChangeSet()
    {
        return _changeSet;
    }

    public void setChangeSet(String changeSet)
    {
        _changeSet = changeSet;
    }

    public Boolean getVisible()
    {
        return _visible;
    }

    public void setVisible(Boolean visible)
    {
        _visible = visible;
    }

    public String getUser()
    {
        return _user;
    }

    public void setUser(String user)
    {
        _user = user;
    }

    public Date getTimestamp()
    {
        return _timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        _timestamp = timestamp;
    }

    public List<OSMTag> getTags()
    {
        return _tags;
    }

    public void setTags(List<OSMTag> tags)
    {
        _tags = tags;
    }

    private String       _id;
    private String       _uid;
    private String       _version;
    private Float        _latitude;
    private Float        _longitude;
    private String       _changeSet;
    private Boolean      _visible;
    private String       _user;
    private Date         _timestamp;
    private List<OSMTag> _tags;
}
