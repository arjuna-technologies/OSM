/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.osm.model;

public class OSMMember
{
    public OSMMember()
    {
    }

    public String getType()
    {
        return _type;
    }

    public void setType(String type)
    {
        _type = type;
    }

    public String getRef()
    {
        return _ref;
    }

    public void setRef(String ref)
    {
        _ref = ref;
    }

    public String getRole()
    {
        return _role;
    }

    public void setRole(String role)
    {
        _role = role;
    }

    private String _type;
    private String _ref;
    private String _role;
}
