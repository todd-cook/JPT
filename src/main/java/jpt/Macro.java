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

import jpt.util.ExpressionEvaluator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Deque;
import java.util.Map;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public abstract interface Macro {

    public abstract void process (XMLStreamWriter paramXMLStreamWriter,
                                  ExpressionEvaluator paramExpressionEvaluator,
                                  Deque<Map<String, Slot>> paramStack)
        throws PageTemplateException, XMLStreamException;

}
