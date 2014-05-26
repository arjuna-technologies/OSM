/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.osm.model;

import java.util.Date;

public class OSMMeta
{
    public OSMMeta()
    {
    }

    public Date getOSMBase()
    {
        return _osmBase;
    }

    public void setOSMBase(Date osmBase)
    {
        _osmBase = osmBase;
    }

    private Date _osmBase;
}
