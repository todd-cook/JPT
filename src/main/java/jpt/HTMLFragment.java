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
package jpt;

import com.ctc.wstx.sw.SimpleNsStreamWriter;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.JDOMException;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.input.StAXBuilder;
import org.jdom.output.XMLOutputter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class HTMLFragment implements Serializable {

    private static final long serialVersionUID = 1816909269653999257L;
    private final String html;
    private final boolean allowHtml;
    private Element dom;

    public HTMLFragment (String html, boolean allowHtml) {
        this.html = html;
        this.allowHtml = allowHtml;
    }

    private Element getParsedFragment () throws PageTemplateException {
        if (this.dom == null) {
            this.dom = parseFragment(this.html, this.allowHtml);
        }
        return this.dom;
    }

    public String getHtml () {
        return this.html;
    }

    public String getXhtml ()
        throws PageTemplateException {
        try {
            Element dom = getParsedFragment();
            StringWriter sw = new StringWriter();
            new XMLOutputter().outputElementContent(dom, sw);
            return sw.toString();
        }
        catch (IOException e) {
            throw new PageTemplateException("Unable to write XHTML from parsed fragment " + this.html, e);
        }
    }

    public void toXhtml (XMLStreamWriter writer)
        throws PageTemplateException, XMLStreamException {
        if ((writer instanceof SimpleNsStreamWriter)) {
            SimpleNsStreamWriter wstxWriter = (SimpleNsStreamWriter) writer;
            wstxWriter.writeRaw(this.html);
        }
        else {
            throw new PageTemplateException("For structured content, we currently require the use of com.ctc.wstx.sw.SimpleNsStreamWriter");
        }
    }

    private void writeContent (List<Content> nodeIterator, XMLStreamWriter writer)
        throws XMLStreamException {
        for (Object aNodeIterator : nodeIterator) {
            Content node = (Content) aNodeIterator;

            if ((node instanceof Element)) {
                writeElement((Element) node, writer);
            }
            else if ((node instanceof Comment)) {
                writer.writeComment(node.getValue());
            }
            else if ((node instanceof ProcessingInstruction)) {
                ProcessingInstruction processingInstruction = (ProcessingInstruction) node;
                writer.writeProcessingInstruction(processingInstruction.getTarget(), processingInstruction.getData());
            }
            else if ((node instanceof CDATA)) {
                writer.writeCData(node.getValue());
            }
            else if ((node instanceof Text)) {
                writer.writeCharacters(node.getValue());
            }
            else if ((node instanceof EntityRef)) {
                writer.writeEntityRef(((EntityRef) node).getName());
            }
        }
    }

    private void writeElement (Element element, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(element.getNamespacePrefix(), element.getName(), element.getNamespaceURI());

        for (Object o : element.getAttributes()) {
            Attribute attribute = (Attribute) o;
            writer.writeAttribute(attribute.getNamespacePrefix(), attribute.getNamespaceURI(), attribute.getName(), attribute.getValue());
        }

        writeContent(element.getContent(), writer);
        writer.writeEndElement();
    }

    private static Element parseFragment (String html, boolean allowHtml) throws PageTemplateException {
        String fragment = Constants.HTML_PREFIX + html + Constants.HTML_SUFFIX;

        if (!allowHtml) {
            fragment = replaceAmpersand(fragment);
        }
        StringReader reader = new StringReader(fragment);
        Element dom;
        try {
            XMLInputFactory inputFactory = PageTemplateImpl.xmlInputFactory;
            StAXBuilder parser = new StAXBuilder();

            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(reader);
            Document document = parser.build(streamReader);
            dom = document.getRootElement().getChild("body");
        }
        catch (XMLStreamException e) {
            if (!allowHtml) {
                throw new PageTemplateException("Unable to parse XML fragment with strict XML reader; however loose HTML has been disabled: " + html, e);
            }
            SAXBuilder builder = new SAXBuilder("org.cyberneko.html.parsers.SAXParser");
            builder.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
            builder.setProperty("http://cyberneko.org/html/properties/names/attrs", "no-change");
            builder.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");
            try {
                dom = builder.build(reader).getRootElement().getChild("body");
            }
            catch (NoClassDefFoundError ee) {
                throw new PageTemplateException("No nekohtml found in classpath; required for non-XML data", e);
            }
            catch (JDOMException ee) {
                throw new PageTemplateException(e);
            }
            catch (IOException ee) {
                throw new PageTemplateException(e);
            }
        }
        return dom;
    }

    static String replaceAmpersand (String string) {
        return string.replaceAll("&(?!((\\w+|(#\\d+));))", "&amp;");
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HTMLFragment that = (HTMLFragment) o;

        if (html != null ? !html.equals(that.html) : that.html != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode () {
        return html != null ? html.hashCode() : 0;
    }

    public String toString () {
        return this.html;
    }
}
