/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.osm.model;

public class OSMBounds
{
    public OSMBounds()
    {
    }

    public Float getMinLatitude()
    {
        return _minLatitude;
    }

    public void setMinLatitude(Float minLatitude)
    {
        _minLatitude = minLatitude;
    }

    public Float getMinLongitude()
    {
        return _minLongitude;
    }

    public void setMinLongitude(Float minLongitude)
    {
        _minLongitude = minLongitude;
    }

    public Float getMaxLatitude()
    {
        return _maxLatitude;
    }

    public void setMaxLatitude(Float maxLatitude)
    {
        _maxLatitude = maxLatitude;
    }

    public Float getMaxLongitude()
    {
        return _maxLongitude;
    }

    public void setMaxLongitude(Float maxLongitude)
    {
        _maxLongitude = maxLongitude;
    }

    private Float _minLatitude;
    private Float _minLongitude;
    private Float _maxLatitude;
    private Float _maxLongitude;
}
