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

package jpt.util;

import jpt.Macro;
import jpt.PageTemplate;
import jpt.PageTemplateException;
import jpt.PageTemplateImpl;
import jpt.Slot;
import org.jdom.Element;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */

public class IncludableTemplate implements MacroProvider {
    private final PageTemplate template;
    private final Object context;
    private final Map<String, Object> dictionary = new ConcurrentHashMap<String, Object>();
    private final Map<String, Macro> macros = new ConcurrentHashMap<String, Macro>();

    /**
     * @param template PageTemplateImpl
     * @param context  the bean-like data transfer object
     */
    public IncludableTemplate (PageTemplate template, Object context) {
        this.template = template;
        this.context = context;

        Map<String, Macro> templateMacros = template.getMacros();
        for (Map.Entry<String, Macro> entry : templateMacros.entrySet()) {
            PageTemplateImpl.MacroImpl macro = (PageTemplateImpl.MacroImpl) entry.getValue();
            this.macros.put(entry.getKey(), new IncludableMacro(macro.getElement()));
        }
    }

    /**
     * @param template
     * @param context
     * @param dictionary
     */
    public IncludableTemplate (PageTemplate template,
                               Object context, Map<String, Object> dictionary) {
        this(template, context);
        this.dictionary.putAll(dictionary);
    }

    /**
     * @return Context object; the Bean-like Data Transfer Object
     */
    public final Object getContext () {
        return this.context;
    }

    /**
     * @param writer
     * @throws PageTemplateException
     * @throws XMLStreamException
     */
    public void process (XMLStreamWriter writer)
        throws PageTemplateException, XMLStreamException {
        this.template.processIncludedTemplate(writer, context, dictionary);
    }

    /**
     * @return Map of Macros
     */
    public Map<String, Macro> getMacros () {
        return this.macros;
    }

    /**
     *
     */
    class IncludableMacro implements Macro {
        private final Element element;

        private IncludableMacro (Element element) {
            this.element = element;
        }

        public void process (XMLStreamWriter writer,
                             ExpressionEvaluator evaluator,
                             Deque<Map<String, Slot>> slotStack)
            throws PageTemplateException, XMLStreamException {
            IncludableTemplate.this.template.setSuppressDeclaration(true);
            IncludableTemplate.this.template.process(this.element,
                                                     writer, IncludableTemplate.this.context,
                                                     IncludableTemplate.this.dictionary, slotStack);
        }
    }

    @Override
    public String toString () {
        return "IncludableTemplate{" +
            "template=" + template +
            ", context=" + context +
            ", dictionary=" + dictionary +
            ", macros=" + macros +
            '}';
    }
}
