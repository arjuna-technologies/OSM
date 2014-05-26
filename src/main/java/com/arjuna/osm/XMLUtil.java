/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.osm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.arjuna.osm.model.OSMBounds;
import com.arjuna.osm.model.OSMMember;
import com.arjuna.osm.model.OSMMeta;
import com.arjuna.osm.model.OSMNode;
import com.arjuna.osm.model.OSMNodeRef;
import com.arjuna.osm.model.OSMNote;
import com.arjuna.osm.model.OSMRelation;
import com.arjuna.osm.model.OSMReport;
import com.arjuna.osm.model.OSMTag;
import com.arjuna.osm.model.OSMWay;

public class XMLUtil
{
    private static final Logger logger = Logger.getLogger(XMLUtil.class.getName());

    public OSMReport parse(InputStream inputStream, List<Problem> problems)
    {
        try
        {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder        documentBuilder        = documentBuilderFactory.newDocumentBuilder();
            Document               document               = documentBuilder.parse(inputStream);
            
            return parseDocument(document, problems);
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Unexpected problem while parsing OSM XML", throwable);
            return null;
        }
    }
    
    public void generate(OutputStream outputStream, OSMReport osmReport, int indentationUnit)
    {
        PrintStream printStream = new PrintStream(outputStream);
        
        printStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        generateReport(printStream, osmReport, 0, indentationUnit);
    }

    private OSMReport parseDocument(Document document, List<Problem> problems)
    {
        Element element = document.getDocumentElement();

        return parseReport(element, problems);
    }

    private OSMReport parseReport(Element element, List<Problem> problems)
    {
        OSMReport       osmReport = new OSMReport();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("version"))
                osmReport.setVersion(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("generator"))
                osmReport.setGenerator(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("attribution"))
                osmReport.setAttribution(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("copyright"))
                osmReport.setCopyright(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("license"))
                osmReport.setLicense(attribute.getNodeValue());
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        List<OSMNote>     osmNotes     = new LinkedList<OSMNote>();
        List<OSMMeta>     osmMetas     = new LinkedList<OSMMeta>();
        List<OSMBounds>   osmBounds    = new LinkedList<OSMBounds>();
        List<OSMNode>     osmNodes     = new LinkedList<OSMNode>();
        List<OSMWay>      osmWays      = new LinkedList<OSMWay>();
        List<OSMRelation> osmRelations = new LinkedList<OSMRelation>();

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            if ((childNode.getNodeType() == Node.TEXT_NODE) && isWhiteSpace(childNode.getNodeValue()))
                continue;
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("note"))
                osmNotes.add(parseNote((Element) childNode, problems));
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("meta"))
                osmMetas.add(parseMeta((Element) childNode, problems));
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("bounds"))
                osmBounds.add(parseBounds((Element) childNode, problems));
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("node"))
                osmNodes.add(parseNode((Element) childNode, problems));
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("way"))
                osmWays.add(parseWay((Element) childNode, problems));
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("relation"))
                osmRelations.add(parseRelation((Element) childNode, problems));
            else
                processUnexpectedNode(childNode, problems);
        }
        osmReport.setNotes(osmNotes);
        osmReport.setMetas(osmMetas);
        osmReport.setBounds(osmBounds);
        osmReport.setNodes(osmNodes);
        osmReport.setWays(osmWays);
        osmReport.setRelations(osmRelations);

        return osmReport;
    }

    private OSMBounds parseBounds(Element element, List<Problem> problems)
    {
        OSMBounds osmBoulds = new OSMBounds();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("minlat"))
                osmBoulds.setMinLatitude(parseFloat(attribute.getNodeValue(), problems));
            else if (attribute.getNodeName().equals("maxlat"))
                osmBoulds.setMaxLatitude(parseFloat(attribute.getNodeValue(), problems));
            else if (attribute.getNodeName().equals("minlon"))
                osmBoulds.setMinLongitude(parseFloat(attribute.getNodeValue(), problems));
            else if (attribute.getNodeName().equals("maxlon"))
                osmBoulds.setMaxLongitude(parseFloat(attribute.getNodeValue(), problems));
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            processUnexpectedNode(childNode, problems);
        }

        return osmBoulds;
    }

    private OSMNode parseNode(Element element, List<Problem> problems)
    {
        OSMNode osmNode = new OSMNode();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("id"))
                osmNode.setId(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("uid"))
                osmNode.setUid(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("version"))
                osmNode.setVersion(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("lat"))
                osmNode.setLatitude(parseFloat(attribute.getNodeValue(), problems));
            else if (attribute.getNodeName().equals("lon"))
                osmNode.setLongitude(parseFloat(attribute.getNodeValue(), problems));
            else if (attribute.getNodeName().equals("changeset"))
                osmNode.setChangeSet(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("visible"))
                osmNode.setVisible(parseBoolean(attribute.getNodeValue(), problems));
            else if (attribute.getNodeName().equals("user"))
                osmNode.setUser(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("timestamp"))
                osmNode.setTimestamp(parseDate(attribute.getNodeValue(), problems));
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        List<OSMTag> osmTags = new LinkedList<OSMTag>();

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            if ((childNode.getNodeType() == Node.TEXT_NODE) && isWhiteSpace(childNode.getNodeValue()))
                continue;
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("tag"))
                osmTags.add(parseTag((Element) childNode, problems));
            else
                processUnexpectedNode(childNode, problems);
        }
        osmNode.setTags(osmTags);

        return osmNode;
    }

    private OSMWay parseWay(Element element, List<Problem> problems)
    {
        OSMWay osmWay = new OSMWay();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("id"))
                osmWay.setId(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("uid"))
                osmWay.setUid(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("version"))
                osmWay.setVersion(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("changeset"))
                osmWay.setChangeSet(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("visible"))
                osmWay.setVisible(parseBoolean(attribute.getNodeValue(), problems));
            else if (attribute.getNodeName().equals("user"))
                osmWay.setUser(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("timestamp"))
                osmWay.setTimestamp(parseDate(attribute.getNodeValue(), problems));
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        List<OSMNodeRef> osmNodeRefs = new LinkedList<OSMNodeRef>();
        List<OSMTag>     osmTags     = new LinkedList<OSMTag>();

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            if ((childNode.getNodeType() == Node.TEXT_NODE) && isWhiteSpace(childNode.getNodeValue()))
                continue;
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("nd"))
                osmNodeRefs.add(parseNodeRef((Element) childNode, problems));
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("tag"))
                osmTags.add(parseTag((Element) childNode, problems));
            else
                processUnexpectedNode(childNode, problems);
        }
        osmWay.setNodeRefs(osmNodeRefs);
        osmWay.setTags(osmTags);

        return osmWay;
    }

    private OSMRelation parseRelation(Element element, List<Problem> problems)
    {
        OSMRelation osmRelation = new OSMRelation();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("id"))
                osmRelation.setId(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("uid"))
                osmRelation.setUid(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("version"))
                osmRelation.setVersion(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("changeset"))
                osmRelation.setChangeSet(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("visible"))
                osmRelation.setVisible(parseBoolean(attribute.getNodeValue(), problems));
            else if (attribute.getNodeName().equals("user"))
                osmRelation.setUser(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("timestamp"))
                osmRelation.setTimestamp(parseDate(attribute.getNodeValue(), problems));
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        List<OSMMember> osmMembers = new LinkedList<OSMMember>();
        List<OSMTag>    osmTags    = new LinkedList<OSMTag>();

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            if ((childNode.getNodeType() == Node.TEXT_NODE) && isWhiteSpace(childNode.getNodeValue()))
                continue;
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("member"))
                osmMembers.add(parseMember((Element) childNode, problems));
            else if ((childNode.getNodeType() == Node.ELEMENT_NODE) && childNode.getNodeName().equals("tag"))
                osmTags.add(parseTag((Element) childNode, problems));
            else
                processUnexpectedNode(childNode, problems);
        }
        osmRelation.setMembers(osmMembers);
        osmRelation.setTags(osmTags);

        return osmRelation;
    }

    private OSMMember parseMember(Element element, List<Problem> problems)
    {
        OSMMember osmMember = new OSMMember();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("type"))
                osmMember.setType(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("ref"))
                osmMember.setRef(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("role"))
                osmMember.setRole(attribute.getNodeValue());
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            processUnexpectedNode(childNode, problems);
        }

        return osmMember;
    }

    private OSMTag parseTag(Element element, List<Problem> problems)
    {
        OSMTag osmTag = new OSMTag();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("k"))
                osmTag.setKey(attribute.getNodeValue());
            else if (attribute.getNodeName().equals("v"))
                osmTag.setValue(attribute.getNodeValue());
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            processUnexpectedNode(childNode, problems);
        }

        return osmTag;
    }

    private OSMNodeRef parseNodeRef(Element element, List<Problem> problems)
    {
        OSMNodeRef osmNodeRef = new OSMNodeRef();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("ref"))
                osmNodeRef.setRef(attribute.getNodeValue());
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            processUnexpectedNode(childNode, problems);
        }

        return osmNodeRef;
    }

    private OSMNote parseNote(Element element, List<Problem> problems)
    {
        OSMNote osmNote = new OSMNote();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            if (childNode.getNodeType() == Node.TEXT_NODE)
                osmNote.setText(childNode.getNodeValue().trim());
            else 
                processUnexpectedNode(childNode, problems);
        }

        return osmNote;
    }

    private OSMMeta parseMeta(Element element, List<Problem> problems)
    {
        OSMMeta osmMeta = new OSMMeta();

        NamedNodeMap attributes = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++)
        {
            Node attribute = attributes.item(attributeIndex);

            if (attribute.getNodeName().equals("osm_base"))
                osmMeta.setOSMBase(parseDate(attribute.getNodeValue(), problems));
            else
                problems.add(new Problem("Unexpected attribute \"" + attribute.getNodeName() + "\" with value \"" + attribute.getNodeValue() + "\""));
        }

        NodeList childNodes = element.getChildNodes();
        for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++)
        {
            Node childNode = childNodes.item(childNodeIndex);

            processUnexpectedNode(childNode, problems);
        }

        return osmMeta;
    }

    private Float parseFloat(String value, List<Problem> problems)
    {
        try
        {
            return Float.valueOf(value);
        }
        catch (NumberFormatException numberFormatException)
        {
            problems.add(new Problem("Invalid number \"" + value + "\""));

            return null;
        }
    }

    private Boolean parseBoolean(String value, List<Problem> problems)
    {
        if (value.equals("true"))
            return Boolean.TRUE;
        else if (value.equals("false"))
            return Boolean.FALSE;
        else
        {
            problems.add(new Problem("Invalid boolean \"" + value + "\""));

            return null;
        }
    }

    private Date parseDate(String value, List<Problem> problems)
    {
        try
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            return dateFormat.parse(value);
        }
        catch (ParseException parseException)
        {
            problems.add(new Problem("Invalid date \"" + value + "\""));

            return null;
        }
    }

    private void processUnexpectedNode(Node node, List<Problem> problems)
    {
        if (node.getNodeType() == Node.TEXT_NODE)
        {
            String unexpectedText = node.getNodeValue().trim();
            if (unexpectedText.length() > 8)
                problems.add(new Problem("Unexpected text \"" + unexpectedText.substring(0, 8) + "...\""));
            else
                problems.add(new Problem("Unexpected text \"" + unexpectedText + "\""));
        }
        else
            problems.add(new Problem("Unexpected node \"" + node.getNodeName() + "\" with value \"" + node.getNodeValue() + "\"" + "\" of type \"" + node.getNodeType() + "\""));
    }

    public boolean isWhiteSpace(String text)
    {
        for (char ch: text.toCharArray())
            if (! Character.isWhitespace(ch))
                return false;
        
        return true;
    }
    
    public void generateReport(PrintStream printStream, OSMReport osmReport, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<osm");
        if (osmReport.getVersion() != null)
            printStream.print(" version=\"" + escape(osmReport.getVersion()) + "\"");
        if (osmReport.getGenerator() != null)
            printStream.print(" generator=\"" + escape(osmReport.getGenerator()) + "\"");
        if (osmReport.getCopyright() != null)
            printStream.print(" copyright=\"" + escape(osmReport.getCopyright()) + "\"");
        if (osmReport.getAttribution() != null)
            printStream.print(" attribution=\"" + escape(osmReport.getAttribution()) + "\"");
        if (osmReport.getLicense() != null)
            printStream.print(" license=\"" + escape(osmReport.getLicense()) + "\"");
        printStream.println(">");
        
        for (OSMNote osmNote: osmReport.getNotes())
            generateNote(printStream, osmNote, currentIndentation + indentationUnit, indentationUnit);
        for (OSMMeta osmMeta: osmReport.getMetas())
            generateMeta(printStream, osmMeta, currentIndentation + indentationUnit, indentationUnit);
        for (OSMBounds osmBounds: osmReport.getBounds())
            generateBounds(printStream, osmBounds, currentIndentation + indentationUnit, indentationUnit);
        for (OSMNode osmNode: osmReport.getNodes())
            generateNode(printStream, osmNode, currentIndentation + indentationUnit, indentationUnit);
        for (OSMWay osmWay: osmReport.getWays())
            generateWay(printStream, osmWay, currentIndentation + indentationUnit, indentationUnit);
        for (OSMRelation osmRelation: osmReport.getRelations())
            generateRelation(printStream, osmRelation, currentIndentation + indentationUnit, indentationUnit);

        generateIndentation(printStream, currentIndentation);
        printStream.println("</osm>");
    }

    public void generateNote(PrintStream printStream, OSMNote osmNote, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<note>");
        printStream.print(escape(osmNote.getText()));
        printStream.println("</note>");
    }
    
    public void generateMeta(PrintStream printStream, OSMMeta osmMeta, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<meta");
        if (osmMeta.getOSMBase() != null)
        {
            printStream.print(" osm_base=\"");
            generateDate(printStream, osmMeta.getOSMBase());
            printStream.print("\"");
        }
        printStream.println("/>");
    }
    
    public void generateBounds(PrintStream printStream, OSMBounds osmBounds, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<bounds");
        if (osmBounds.getMinLatitude() != null)
            printStream.print(" minlat=\"" + osmBounds.getMinLatitude() + "\"");
        if (osmBounds.getMaxLatitude() != null)
            printStream.print(" maxlat=\"" + osmBounds.getMaxLatitude() + "\"");
        if (osmBounds.getMinLongitude() != null)
            printStream.print(" minlon=\"" + osmBounds.getMinLongitude() + "\"");
        if (osmBounds.getMaxLongitude() != null)
            printStream.print(" minlon=\"" + osmBounds.getMaxLongitude() + "\"");
        printStream.println("/>");
    }

    public void generateNode(PrintStream printStream, OSMNode osmNode, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<node");
        if (osmNode.getId() != null)
            printStream.print(" id=\"" + escape(osmNode.getId()) + "\"");
        if (osmNode.getUid() != null)
            printStream.print(" uid=\"" + escape(osmNode.getUid()) + "\"");
        if (osmNode.getVersion() != null)
            printStream.print(" version=\"" + escape(osmNode.getVersion()) + "\"");
        if (osmNode.getLatitude() != null)
            printStream.print(" lat=\"" + osmNode.getLatitude() + "\"");
        if (osmNode.getLongitude() != null)
            printStream.print(" lon=\"" + osmNode.getLongitude() + "\"");
        if (osmNode.getChangeSet() != null)
            printStream.print(" changeset=\"" + escape(osmNode.getChangeSet()) + "\"");
        if (osmNode.getVisible() != null)
        {
            printStream.print(" visible=\"");
            generateBoolean(printStream, osmNode.getVisible());
            printStream.print("\"");
        }
        if (osmNode.getUser() != null)
            printStream.print(" user=\"" + escape(osmNode.getUser()) + "\"");
        if (osmNode.getTimestamp() != null)
        {
            printStream.print(" timestamp=\"");
            generateDate(printStream, osmNode.getTimestamp());
            printStream.print("\"");
        }
        if (! osmNode.getTags().isEmpty())
        {
            printStream.println(">");

            for (OSMTag osmTag: osmNode.getTags())
                generateTag(printStream, osmTag, currentIndentation + indentationUnit, indentationUnit);

            generateIndentation(printStream, currentIndentation);
            printStream.println("</node>");
        }
        else
            printStream.println("/>");
    }

    public void generateWay(PrintStream printStream, OSMWay osmWay, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<way");
        if (osmWay.getId() != null)
            printStream.print(" id=\"" + escape(osmWay.getId()) + "\"");
        if (osmWay.getUid() != null)
            printStream.print(" uid=\"" + escape(osmWay.getUid()) + "\"");
        if (osmWay.getVersion() != null)
            printStream.print(" version=\"" + escape(osmWay.getVersion()) + "\"");
        if (osmWay.getChangeSet() != null)
            printStream.print(" changeset=\"" + escape(osmWay.getChangeSet()) + "\"");
        if (osmWay.getVisible() != null)
        {
            printStream.print(" visible=\"");
            generateBoolean(printStream, osmWay.getVisible());
            printStream.print("\"");
        }
        if (osmWay.getUser() != null)
            printStream.print(" user=\"" + escape(osmWay.getUser()) + "\"");
        if (osmWay.getTimestamp() != null)
        {
            printStream.print(" timestamp=\"");
            generateDate(printStream, osmWay.getTimestamp());
            printStream.print("\"");
        }
        if ((! osmWay.getNodeRefs().isEmpty()) && (! osmWay.getTags().isEmpty()))
        {
            printStream.println(">");

            for (OSMNodeRef osmNodeRef: osmWay.getNodeRefs())
                generateNodeRef(printStream, osmNodeRef, currentIndentation + indentationUnit, indentationUnit);
            for (OSMTag osmTag: osmWay.getTags())
                generateTag(printStream, osmTag, currentIndentation + indentationUnit, indentationUnit);

            generateIndentation(printStream, currentIndentation);
            printStream.println("</way>");
        }
        else
            printStream.println("/>");
    }

    public void generateRelation(PrintStream printStream, OSMRelation osmRelation, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<relation");
        if (osmRelation.getId() != null)
            printStream.print(" id=\"" + escape(osmRelation.getId()) + "\"");
        if (osmRelation.getVisible() != null)
        {
            printStream.print(" visible=\"");
            generateBoolean(printStream, osmRelation.getVisible());
            printStream.print("\"");
        }
        if (osmRelation.getVersion() != null)
            printStream.print(" version=\"" + escape(osmRelation.getVersion()) + "\"");
        if (osmRelation.getChangeSet() != null)
            printStream.print(" changeset=\"" + escape(osmRelation.getChangeSet()) + "\"");
        if (osmRelation.getTimestamp() != null)
        {
            printStream.print(" timestamp=\"");
            generateDate(printStream, osmRelation.getTimestamp());
            printStream.print("\"");
        }
        if (osmRelation.getUser() != null)
            printStream.print(" user=\"" + escape(osmRelation.getUser()) + "\"");
        if (osmRelation.getUid() != null)
            printStream.print(" uid=\"" + escape(osmRelation.getUid()) + "\"");
        if ((! osmRelation.getMembers().isEmpty()) && (! osmRelation.getTags().isEmpty()))
        {
            printStream.println(">");

            for (OSMMember osmMember: osmRelation.getMembers())
                generateMember(printStream, osmMember, currentIndentation + indentationUnit, indentationUnit);
            for (OSMTag osmTag: osmRelation.getTags())
                generateTag(printStream, osmTag, currentIndentation + indentationUnit, indentationUnit);

            generateIndentation(printStream, currentIndentation);
            printStream.println("</relation>");
        }
        else
            printStream.println("/>");
    }

    public void generateNodeRef(PrintStream printStream, OSMNodeRef osmNodeRef, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<nd");
        if (osmNodeRef.getRef() != null)
            printStream.print(" ref=\"" + escape(osmNodeRef.getRef()) + "\"");
        printStream.println("/>");
    }

    public void generateTag(PrintStream printStream, OSMTag osmTag, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<tag");
        if (osmTag.getKey() != null)
            printStream.print(" k=\"" + escape(osmTag.getKey()) + "\"");
        if (osmTag.getValue() != null)
            printStream.print(" v=\"" + escape(osmTag.getValue()) + "\"");
        printStream.println("/>");
    }

    public void generateMember(PrintStream printStream, OSMMember osmMember, int currentIndentation, int indentationUnit)
    {
        generateIndentation(printStream, currentIndentation);
        printStream.print("<member");
        if (osmMember.getType() != null)
            printStream.print(" type=\"" + escape(osmMember.getType()) + "\"");
        if (osmMember.getRef() != null)
            printStream.print(" ref=\"" + escape(osmMember.getRef()) + "\"");
        if (osmMember.getRole() != null)
            printStream.print(" role=\"" + escape(osmMember.getRole()) + "\"");
        printStream.println("/>");
    }

    public void generateIndentation(PrintStream printStream, int currentIndentation)
    {
        for (int count = 0; count < currentIndentation; count++)
            printStream.print(" ");
    }

    public void generateBoolean(PrintStream printStream, Boolean value)
    {
        if (value)
            printStream.print("true");
        else
            printStream.print("false");
    }

    public void generateDate(PrintStream printStream, Date value)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
 
        printStream.print(dateFormat.format(value));
    }

    public String escape(String text)
    {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
