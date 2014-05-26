/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.osm.model;

import java.util.List;

public class OSMReport
{
    public OSMReport()
    {
    }

    public String getVersion()
    {
        return _version;
    }

    public void setVersion(String version)
    {
        _version = version;
    }

    public String getGenerator()
    {
        return _generator;
    }

    public void setGenerator(String generator)
    {
        _generator = generator;
    }

    public String getAttribution()
    {
        return _attribution;
    }

    public void setAttribution(String attribution)
    {
        _attribution = attribution;
    }

    public String getCopyright()
    {
        return _copyright;
    }

    public void setCopyright(String copyright)
    {
        _copyright = copyright;
    }

    public String getLicense()
    {
        return _license;
    }

    public void setLicense(String license)
    {
        _license = license;
    }

    public List<OSMNote> getNotes()
    {
        return _notes;
    }

    public void setNotes(List<OSMNote> notes)
    {
        _notes = notes;
    }

    public List<OSMMeta> getMetas()
    {
        return _metas;
    }

    public void setMetas(List<OSMMeta> metas)
    {
        _metas = metas;
    }

    public List<OSMBounds> getBounds()
    {
        return _bounds;
    }

    public void setBounds(List<OSMBounds> bounds)
    {
        _bounds = bounds;
    }

    public List<OSMNode> getNodes()
    {
        return _nodes;
    }

    public void setNodes(List<OSMNode> nodes)
    {
        _nodes = nodes;
    }

    public List<OSMWay> getWays()
    {
        return _ways;
    }

    public void setWays(List<OSMWay> ways)
    {
        _ways = ways;
    }

    public List<OSMRelation> getRelations()
    {
        return _relations;
    }

    public void setRelations(List<OSMRelation> relations)
    {
        _relations = relations;
    }

    private String            _version;
    private String            _generator;
    private String            _attribution;
    private String            _copyright;
    private String            _license;
    private List<OSMNote>     _notes;
    private List<OSMMeta>     _metas;
    private List<OSMBounds>   _bounds;
    private List<OSMNode>     _nodes;
    private List<OSMWay>      _ways;
    private List<OSMRelation> _relations;
}
