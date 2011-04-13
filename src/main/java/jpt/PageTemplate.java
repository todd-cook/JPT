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

import jpt.util.MacroProvider;
import org.jdom.Element;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Deque;
import java.util.Map;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */

public abstract interface PageTemplate extends MacroProvider {

    public abstract void process (OutputStream paramOutputStream, Object paramObject)
        throws PageTemplateException, IOException;

    public abstract void process (OutputStream paramOutputStream, Object paramObject, Map<String, Object> paramMap)
        throws PageTemplateException, IOException;

    public abstract Resolver getResolver ();

    public abstract void setResolver (Resolver paramResolver);

    public abstract Map<String, Macro> getMacros ();

    public abstract void processIncludedTemplate (XMLStreamWriter writer, Object context, Map<String, Object> dictionary) throws XMLStreamException, PageTemplateException;

    public abstract void setSuppressDeclaration (Boolean bool);

    public abstract void process (Element element, XMLStreamWriter writer,
                                  Object context, Map<String, Object> dictionary,
                                  Deque<Map<String, Slot>> slotStack)
        throws XMLStreamException, PageTemplateException;

}
