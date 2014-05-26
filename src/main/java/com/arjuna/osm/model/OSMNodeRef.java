/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.osm.model;

public class OSMNodeRef
{
    public OSMNodeRef()
    {
    }

    public String getRef()
    {
        return _ref;
    }

    public void setRef(String ref)
    {
        _ref = ref;
    }

    private String _ref;
}
