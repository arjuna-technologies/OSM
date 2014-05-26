/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.osm;

public class Problem
{
    public Problem(String message)
    {
        _message  = message;
    }

    public String getMessage()
    {
        return _message;
    }

    public void setMessage(String message)
    {
        _message = message;
    }

    public String toString()
    {
        return _message;
    }

    private String _message;
}
