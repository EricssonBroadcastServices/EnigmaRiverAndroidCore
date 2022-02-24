package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/** Responsible for the parsing of a VAST document containing ad information for a live stream */
class NowtilusLiveParser implements INowtilusParser {

    @Nullable public VastAdEntrySet parseEntries(JSONObject resource) throws JSONException {

        String xmlString = resource.getString("vast");
        long adStartTime = resource.getLong("time");
        ArrayList<VastAdEntry> entries = new ArrayList<>();
        xmlString = unescapeXml(xmlString);
        Document xml = convertStringIntoXml(xmlString);

        if(xml == null) { return null; }

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList adsXml;
        try {
            adsXml = (NodeList)xPath.compile("/VAST/Ad").evaluate(xml, XPathConstants.NODESET);
        } catch(XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }

        EventParser eventParser = new EventParser();

        long startOffset = adStartTime;
        for(int i = 0; i < adsXml.getLength(); i++) {
            Element adNode = (Element)adsXml.item(i);

            String id = adNode.getAttributes().getNamedItem("id").getNodeValue();
            String durationString = adNode.getElementsByTagName("Duration").item(0).getTextContent();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsedDate;
            long duration;
            try {
                parsedDate = dateFormat.parse(durationString);
                duration = parsedDate.getTime();
            } catch(ParseException e) {
                e.printStackTrace();
                continue;
            }

            NodeList titleNodeList = adNode.getElementsByTagName("AdTitle");
            String title = titleNodeList.getLength() > 0 ? titleNodeList.item(0).getTextContent() : null;

            NodeList trackingNodes = adNode.getElementsByTagName("Tracking");
            HashMap<AdEventType, VastImpression> logEntrySets = new HashMap<>();
            HashMap<AdEventType, Collection<URL>> eventUrls = new HashMap<>();

            for(int tIndex = 0; tIndex < trackingNodes.getLength(); tIndex++) {
                Node trackingNode = trackingNodes.item(tIndex);
                AdEventType eventType = eventParser.parse(trackingNode.getAttributes().getNamedItem("event").getNodeValue());

                if(eventType != null && !eventUrls.containsKey(eventType)) {
                    eventUrls.put(eventType, new ArrayList<>());
                }

                URL url = eventParser.parseEventUrl(trackingNode.getTextContent());
                if(url != null) {
                    eventUrls.get(eventType).add(url);
                }
            }

            for(AdEventType type : eventUrls.keySet()) {
                logEntrySets.put(type, new VastImpression(type, eventUrls.get(type)));
            }
            VastAdEntry entry = new VastAdEntry(id, title, startOffset, duration, logEntrySets, null);
            startOffset += duration;
            entries.add(entry);
        }
        return new VastAdEntrySet(entries);
    }

    private String unescapeXml(String unescapedXmlString) {
        String xmlString = unescapedXmlString.replace("\\n", "\n");

        Pattern pattern = Pattern.compile("\\\\.{1}");
        Matcher matcher = pattern.matcher(xmlString);

        while(matcher.find()) {
            String unEscapeThis = matcher.group(0);
            xmlString = xmlString.replace(unEscapeThis, unEscapeThis.replace("\\", ""));
            matcher = pattern.matcher(xmlString);
        }
        return xmlString;
    }

    private Document convertStringIntoXml(String xmlString) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
            return db.parse(stream);
        }
        catch(ParserConfigurationException | SAXException | IOException e) { e.printStackTrace(); }
        return null;
    }
}
