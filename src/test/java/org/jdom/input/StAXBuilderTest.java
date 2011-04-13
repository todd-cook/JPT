/**
 *  Java Page Templates
 *  Copyright (C) 2004-2011 Christopher M Rossi
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jdom.input;

import jpt.JptTestUtil;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class StAXBuilderTest extends JptTestUtil {

    private static final String egXmlDoc =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><html>" +
            "<p>'this is a string literal' should be this is a string literal</p>" +
            "<p>123l should be 123 and should have a class of java.lang.Long</p>" +
            "<p>123 should be 123 and should have a class of java.lang.Integer</p>" +
            "<p>123.45d should be 123.45 and should have a class of java.lang.Double</p>" +
            "<p>123.45f should be 123.45 and should have a class of java.lang.Float</p>" +
            "<p>123.45 should be 123.45 and should have a class of java.lang.Float</p>" +
            "<p>true should be true and should have a class of java.lang.Boolean</p>" +
            "<p>false should be false and should have a class of java.lang.Boolean</p></html>";

    @Test
    public void testBuilder () {
        try {
            StringReader stringReader = new StringReader(egXmlDoc);
            XMLInputFactory f = javax.xml.stream.XMLInputFactory.newInstance();
            XMLStreamReader sr = f.createXMLStreamReader(stringReader);
            StAXBuilder builder = new StAXBuilder();
            builder.setIgnoreWhitespace(false);
            builder.setRemoveIndentation(false);
            Document domDoc = builder.build(sr);
            XMLOutputter outputer = new org.jdom.output.XMLOutputter();
            StringWriter sw = new StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
            outputer.output(domDoc, sw);
            pw.flush();
            showTransformation(egXmlDoc, sw.toString());
            sw.close();
            String trueResult = removeAllBreaks(sw.toString());
            assertTrue(trueResult.equals(egXmlDoc));
        }
        catch (XMLStreamException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBuilderOptions () {
        try {
            StringReader stringReader = new StringReader(egXmlDoc);
            XMLInputFactory f = javax.xml.stream.XMLInputFactory.newInstance();
            XMLStreamReader sr = f.createXMLStreamReader(stringReader);
            StAXBuilder builder = new StAXBuilder();
            builder.setIgnoreWhitespace(true);
            builder.setRemoveIndentation(true);
            Document domDoc = builder.build(sr);
            XMLOutputter outputer = new org.jdom.output.XMLOutputter();
            StringWriter sw = new StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
            outputer.output(domDoc, sw);
            pw.flush();
            showTransformation(egXmlDoc, sw.toString());
            sw.close();
            String trueResult = removeAllBreaks(sw.toString());
            assertTrue(trueResult.equals(egXmlDoc));
        }
        catch (XMLStreamException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBuilderOptions3 () {
        try {
            StringReader stringReader = new StringReader(egXmlDoc);
            XMLInputFactory f = javax.xml.stream.XMLInputFactory.newInstance();
            XMLStreamReader sr = f.createXMLStreamReader(stringReader);
            StAXBuilder builder = new StAXBuilder();
            builder.setIgnoreWhitespace(false);
            builder.setRemoveIndentation(true);
            Document domDoc = builder.build(sr);
            XMLOutputter outputer = new org.jdom.output.XMLOutputter();
            StringWriter sw = new StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
            outputer.output(domDoc, sw);
            pw.flush();
            showTransformation(egXmlDoc, sw.toString());
            sw.close();
            String trueResult = removeAllBreaks(sw.toString());
            assertTrue(trueResult.equals(egXmlDoc));
        }
        catch (XMLStreamException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBuilderOptions4 () {
        try {
            StringReader stringReader = new StringReader(egXmlDoc);
            XMLInputFactory f = javax.xml.stream.XMLInputFactory.newInstance();
            XMLStreamReader sr = f.createXMLStreamReader(stringReader);
            StAXBuilder builder = new StAXBuilder();
            builder.setIgnoreWhitespace(true);
            builder.setRemoveIndentation(false);
            Document domDoc = builder.build(sr);
            XMLOutputter outputer = new org.jdom.output.XMLOutputter();
            StringWriter sw = new StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(System.out);
            outputer.output(domDoc, sw);
            pw.flush();
            showTransformation(egXmlDoc, sw.toString());
            sw.close();
            String trueResult = removeAllBreaks(sw.toString());
            assertTrue(trueResult.equals(egXmlDoc));
        }
        catch (XMLStreamException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
