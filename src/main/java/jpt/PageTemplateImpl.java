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

import jpt.util.EvalException;
import jpt.util.ExpressionEvaluator;
import jpt.util.IncludableTemplate;
import jpt.util.MetalStatement;
import jpt.util.MvelExpressionEvaluator;
import jpt.util.TalStatement;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.input.StAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Java implementation of Plone's TAL processing:
 * TAL = Template Attribute Language
 * TALES = Template Attribute Language Expression Syntax
 * see http://plone.org
 *
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public final class PageTemplateImpl implements PageTemplate, Serializable {
    private static final long serialVersionUID = -3403565057108448817L;

    private static Logger LOG = LoggerFactory.getLogger(PageTemplateImpl.class);

    /**
     * The following object has package level access for the HTMLFragment class
     */
    static final XMLInputFactory xmlInputFactory = makeInputFactory();

    private static final Object DEFAULT = new Object();

    private boolean allowHtml = true;
    /**
     * The URI needs to be populated because it's used in resolving
     * locations of templates; e.g. when a macro requests another template.
     */
    private URI uri;
    private URL url;
    private Resolver userResolver = null;
    private Document doc;
    private boolean logErrorAsWarn = false;

    private final Map<String, String> prefixNamespaceUri =
        new ConcurrentHashMap<String, String>();

    private final Map<String, Macro> macros = new HashMap<String, Macro>();

    private boolean SUPPRESS_ERRORS = true;

    private boolean suppressDeclaration;

    /**
     * Instances of this class are created using the provided Builder class
     */
    private PageTemplateImpl () {
    }

    /**
     * Tired of looking for the right constructor from among a vague series?
     * Unclear which parameter are necessary when?
     * Well, then try the Builder pattern, for more info
     * read Joshua Bloch _Effective Java_Second_Edition_
     */
    public static class Builder {
        // Required Parameters; see build()
        // Optional parameters - initialized to default values
        private String templateText;
        private InputStream inputStream;
        private URL url;
        private URI uri;
        private boolean suppressDeclaration = true;
        private boolean allowHtml = true;
        private boolean suppressErrors = true;
        private Resolver resolver = null;

        public Builder () {
        }

        /**
         * a JTP template may be provided as a String, a URI, or an InputStream;
         * however they all get converted to being an input stream
         *
         * @param val InputStream representing the template
         * @return Builder instance
         */
        public Builder inputStream (InputStream val) {
            inputStream = val;
            return this;
        }

        public Builder url (URL val) {
            url = val;
            return this;
        }

        public Builder templateText (String val) {
            this.templateText = val;
            return this;
        }

        /**
         * Suppresses the TAL startTag declaration:
         * <code><?xml version='1.0' encoding='UTF-8'?></code>
         *
         * @param bool boolean
         * @return Builder object
         */
        public Builder suppressDeclaration (boolean bool) {
            suppressDeclaration = bool;
            return this;
        }

        /**
         * Changes TAL data marked as "structure ..."
         * so that ampersand characters are escaped.
         * For more info, see the test cases that use "structure " and
         * the HTMLFragment class
         *
         * @param bool
         * @return Builder object
         */
        public Builder allowHtml (boolean bool) {
            allowHtml = bool;
            return this;
        }

        public Builder suppressErrors (boolean bool) {
            suppressErrors = bool;
            return this;
        }

        public Builder resolver (Resolver val) {
            resolver = val;
            return this;
        }

        /**
         * Call this to create your template
         *
         * @return PageTemplateImpl
         */
        public PageTemplateImpl build () {
            if (templateText == null && inputStream == null && url == null) {
                throw new IllegalArgumentException(
                    "Must provide template as an InputStream or as a String or as a URL");
            }
            if (inputStream == null && templateText != null) {
                inputStream = makeInputStream(templateText);
            }
            if (url != null) {
                try {
                    inputStream = url.openStream();
                    uri = new URI(url.toString());
                }
                catch (IOException e) {
                    throw new IllegalArgumentException(
                        "failure to open stream from URL: " +
                            url.toString(), e);
                }
                catch (URISyntaxException e) {
                    throw new IllegalArgumentException(
                        "failure to create URI from URL: " +
                            url.toString(), e);
                }
            }
            return new PageTemplateImpl(this);
        }
    }

    private PageTemplateImpl (Builder builder) {
        SUPPRESS_ERRORS = builder.suppressErrors;
        suppressDeclaration = builder.suppressDeclaration;
        allowHtml = builder.allowHtml;
        InputStream inputStream = builder.inputStream;
        url = builder.url;
        userResolver = builder.resolver;
        uri = builder.uri;

        try {
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(inputStream);
            StAXBuilder parser = new StAXBuilder();
            doc = parser.build(reader);
            inputStream.close();
        }
        catch (XMLStreamException e) {
            throw new IllegalArgumentException(e);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static ByteArrayInputStream makeInputStream (String template) {
        try {
            return new ByteArrayInputStream(template.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to convert UTF-8 string to bytes", e);
        }
    }

    static XMLInputFactory makeInputFactory () throws FactoryConfigurationError {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.FALSE);
        inputFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.FALSE);
        inputFactory.setProperty("com.ctc.wstx.minTextSegment", Integer.MAX_VALUE);
        return inputFactory;
    }

    /**
     * @return Resolver used to find the templates
     */
    public Resolver getResolver () {
        return this.userResolver;
    }

    public void setResolver (Resolver resolver) {
        this.userResolver = resolver;
    }

    public boolean isLogErrorAsWarn () {
        return this.logErrorAsWarn;
    }

    /**
     * For quieting the logging output, since template errors aren't showstoppers
     *
     * @param logErrorAsWarn boolean
     */
    public void setLogErrorAsWarn (boolean logErrorAsWarn) {
        this.logErrorAsWarn = logErrorAsWarn;
    }

    /**
     * @param output
     * @param context
     * @throws PageTemplateException
     * @throws IOException
     */
    public void process (OutputStream output, Object context)
        throws PageTemplateException, IOException {
        process(output, context, null);
    }

    /**
     * @param output
     * @param context
     * @param dictionary
     * @throws IOException
     * @throws PageTemplateException
     */
    public void process (OutputStream output, Object context,
                         Map<String, Object> dictionary)
        throws IOException, PageTemplateException {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(output);
            process(writer, context, dictionary);
            writer.close();
        }
        catch (XMLStreamException e) {
            throw ((IOException) new IOException().initCause(e));
        }
    }

    private void process (XMLStreamWriter writer, Object context,
                          Map<String, Object> dictionary)
        throws XMLStreamException, PageTemplateException {
        process(this.doc.getRootElement(), writer, context, dictionary,
                new ArrayDeque<Map<String, Slot>>());
    }

    /**
     * @param element
     * @param writer
     * @param context
     * @param dictionary
     * @param slotStack
     * @throws XMLStreamException
     * @throws PageTemplateException
     */
    public void process (Element element, XMLStreamWriter writer,
                         Object context, Map<String, Object> dictionary,
                         Deque<Map<String, Slot>> slotStack)
        throws XMLStreamException, PageTemplateException {
        try {
            ExpressionEvaluator expressionEvaluator = new MvelExpressionEvaluator();
            if (dictionary != null) {
                for (Map.Entry<String, Object> entry : dictionary.entrySet()) {
                    expressionEvaluator.set(entry.getKey(), entry.getValue());
                }
            }
            expressionEvaluator.set("here", context);
            expressionEvaluator.set("template", this);
            expressionEvaluator.set("resolver", new DefaultResolver());
            expressionEvaluator.set("default", DEFAULT);
            if (!suppressDeclaration) {
                writer.writeStartDocument();
                writer.writeCharacters("\n");
                DocType docType = this.doc.getDocType();
                if (docType != null) {
                    writer.writeDTD("<!DOCTYPE " + docType.getElementName() + " PUBLIC \"" + docType.getPublicID() + "\" \"" + docType.getSystemID() + "\">");
                    writer.writeCharacters("\n");
                }
            }
            processElement(element, writer, expressionEvaluator, slotStack);
            if (!suppressDeclaration) {
                writer.writeEndDocument();
            }
        }
        catch (EvalException e) {
            throw new PageTemplateException(e);
        }
    }

    /**
     * @param writer
     * @param context
     * @param dictionary
     * @throws XMLStreamException
     * @throws PageTemplateException
     */
    public void processIncludedTemplate (XMLStreamWriter writer, Object context, Map<String, Object> dictionary)
        throws XMLStreamException, PageTemplateException {
        suppressDeclaration = true;
        process(this.doc.getRootElement(), writer, context, dictionary,
                new ArrayDeque<Map<String, Slot>>()
        );
    }

    //@Override
    public void setSuppressDeclaration (Boolean bool) {
        this.suppressDeclaration = bool;
    }

    /**
     * @param element
     * @param writer
     * @param expressionEvaluator
     * @param slotStack
     * @throws PageTemplateException
     * @throws XMLStreamException
     */
    public void processElement (Element element, XMLStreamWriter writer,
                                ExpressionEvaluator expressionEvaluator,
                                Deque<Map<String, Slot>> slotStack)
        throws PageTemplateException, XMLStreamException {
        // Get attributes
        Expressions expressions = new Expressions();
        List<SavedAttribute> attributes = getAttributes(element, expressions);
        try {
            Map<String, String> attrs = new HashMap<String, String>();
            for (SavedAttribute savedAttribute : attributes) {
                attrs.put(savedAttribute.getLocalPart(), savedAttribute.getValue());
            }
            expressionEvaluator.set("attrs", attrs);
        }
        catch (EvalException e) {
            throw new PageTemplateException("Unable to add 'attrs' to expression evaluator", e);
        }
        // Process instructions

        // evaluate expression
        if (expressions.evaluate != null) {
            Expression.evaluate(expressions.evaluate, expressionEvaluator);
        }
        // use macro
        if (expressions.useMacro != null) {
            processMacro(expressions.useMacro, element, writer, expressionEvaluator, slotStack);
            return;
        }
        // fill slot
        if (expressions.defineSlot != null) {
            if (!slotStack.isEmpty()) {
                Map<String, Slot> slots = slotStack.pop();
                Slot slot = slots.get(expressions.defineSlot);
                if (slot != null) {
                    slot.process(writer, expressionEvaluator, slotStack);
                    slotStack.push(slots);
                    return;
                }
                // else { use content in macro }
                slotStack.push(slots);
            }
            else {
                throw new PageTemplateException("slot definition not allowed outside of macro");
            }
        }
        // define
        if (expressions.define != null) {
            processDefine(expressions.define, expressionEvaluator);
        }
        // condition
        if ((expressions.condition != null) && (!Expression.evaluateBoolean(expressions.condition, expressionEvaluator))) {
            return;
        }
        // repeat
        try {
            Loop loop = new Loop(expressions.repeat, expressionEvaluator);
            while (loop.repeat(expressionEvaluator)) {
                // content or replace
                Object jptContent = null;
                if (expressions.content != null) {
                    jptContent = processContent(expressions.content, expressionEvaluator);
                }
                // attributes
                if (expressions.attributes != null) {
                    processAttributes(element, attributes, expressions.attributes, expressionEvaluator);
                }
                // omit-tag
                boolean jptOmitTag = false;
                if (expressions.omitTag != null) {
                    if (expressions.omitTag.equals("")) {
                        jptOmitTag = true;
                    }
                    else {
                        jptOmitTag = Expression.evaluateBoolean(expressions.omitTag, expressionEvaluator);
                    }
                }
                // Declare element
                //Namespace startTag = element.getNamespaceURI(); //   .getNamespace();
                if (!jptOmitTag) {
                    writer.writeStartElement(element.getNamespacePrefix(), element.getName(), element.getNamespaceURI());
                    for (Iterator it = element.getAdditionalNamespaces().iterator(); it.hasNext();) {
                        Namespace namespace = (Namespace) it.next();
                        if ((!namespace.getURI().equals("http://xml.zope.org/namespaces/tal"))
                            && (!namespace.getURI().equals("http://xml.zope.org/namespaces/metal"))) {
                            writer.writeNamespace(namespace.getPrefix(), namespace.getURI());
                        }
                    }
                    for (SavedAttribute attribute : attributes) {
                        writer.writeAttribute(attribute.getPrefix(),
                                              attribute.getNamespaceUri(),
                                              attribute.getLocalPart(),
                                              attribute.getValue());
                    }
                }
                // Process content
                if ((jptContent != null) && (jptContent != DEFAULT)) {
                    // Content for this element has been generated dynamically
                    if ((jptContent instanceof HTMLFragment)) {
                        HTMLFragment html = (HTMLFragment) jptContent;
                        html.toXhtml(writer);
                    }
                    else if ((jptContent instanceof IncludableTemplate)) {
                        IncludableTemplate template = (IncludableTemplate) jptContent;
                        template.process(writer);
                    }
                    else {
                        // plain text
                        writer.writeCharacters((String) jptContent);
                    }
                }
                else {
                    defaultContent(element, writer, expressionEvaluator, slotStack);
                }
                // End element
                if (!jptOmitTag) {
                    writer.writeEndElement();
                }
            }
        }
        catch (ExpressionEvaluationException e) {
            if (SUPPRESS_ERRORS) {
                if (!isLogErrorAsWarn()) {
                    LOG.error("Error processing expression in page template; continuing with rest of page", e);
                }
                else {
                    LOG.warn("Error processing expression in page template; continuing with rest of page", e);
                }
            }
            else {
                throw e;
            }
        }
    }

    private void defaultContent (Element element, XMLStreamWriter writer, ExpressionEvaluator expressionEvaluator,
                                 Deque<Map<String, Slot>> slotStack)
        throws PageTemplateException, XMLStreamException {
        addNamespace(element.getNamespace());
        for (Object o : element.getAdditionalNamespaces()) {
            Namespace additionalNamespace = (Namespace) o;
            addNamespace(additionalNamespace);
        }
        for (Object o : element.getContent()) {
            Content node = (Content) o;
            if ((node instanceof Element)) {
                processElement((Element) node, writer, expressionEvaluator, slotStack);
            }
            else if ((node instanceof Comment)) {
                writer.writeComment(node.getValue());
            }
            else if ((node instanceof ProcessingInstruction)) {
                ProcessingInstruction processingInstruction = (ProcessingInstruction) node;
                writer.writeProcessingInstruction(processingInstruction.getTarget(),
                                                  processingInstruction.getData());
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

    private void addNamespace (Namespace additionalNamespace) {
        this.prefixNamespaceUri.put(additionalNamespace.getPrefix(), additionalNamespace.getURI());
    }

    List<SavedAttribute> getAttributes (Element element, Expressions expressions)
        throws PageTemplateException {
        List<SavedAttribute> attributes = new ArrayList<SavedAttribute>();

        for (Object o : element.getAttributes()) {
            Attribute attribute = (Attribute) o;
            Namespace namespace = attribute.getNamespace();
            String name = attribute.getName();

            if ("http://xml.zope.org/namespaces/tal".equals(namespace.getURI())) {
                addNamespace(namespace);
                // tal:define
                if (name.equals(TalStatement.DEFINE.getAttribute())) {
                    expressions.define = attribute.getValue();
                }
                // tal:condition
                else if (name.equals(TalStatement.CONDITION.getAttribute())) {
                    expressions.condition = attribute.getValue();
                }
                // tal:repeat
                else if (name.equals(TalStatement.REPEAT.getAttribute())) {
                    expressions.repeat = attribute.getValue();
                }
                // tal:content
                else if (name.equals(TalStatement.CONTENT.getAttribute())) {
                    expressions.content = attribute.getValue();
                }
                // tal:replace
                else if (name.equals(TalStatement.REPLACE.getAttribute())) {
                    if (expressions.omitTag == null) {
                        expressions.omitTag = "";
                    }
                    expressions.content = attribute.getValue();
                }
                // tal:attributes
                else if (name.equals(TalStatement.ATTRIBUTES.getAttribute())) {
                    expressions.attributes = attribute.getValue();
                }
                // tal:omit-tag
                else if (name.equals(TalStatement.OMIT_TAG.getAttribute())) {
                    expressions.omitTag = attribute.getValue();
                }
                // tal:evaluate
                else if (name.equals("evaluate")) {
                    expressions.evaluate = attribute.getValue();
                }
                // error
                else {
                    throw new PageTemplateException("unknown tal attribute: " + name);
                }
            }
            else if ("http://xml.zope.org/namespaces/metal".equals(namespace.getURI())) {
                addNamespace(namespace);
                // metal:use-macro
                if (name.equals(MetalStatement.USE_MACRO.getAttribute())) {
                    expressions.useMacro = attribute.getValue();
                }
                // metal:define-slot
                else if (name.equals(MetalStatement.DEFINE_SLOT.getAttribute())) {
                    expressions.defineSlot = attribute.getValue();
                }
                // metal:define-macro
                // metal:fill-slot
                else if ((!name.equals(MetalStatement.DEFINE_MACRO.getAttribute()))
                    && (!name.equals(MetalStatement.FILL_SLOT.getAttribute()))) {
                    throw new PageTemplateException("unknown metal attribute: " + name);
                }
            }
            // Pass on all other attributes
            else {
                attributes.add(new SavedAttribute(attribute.getName(),
                                                  attribute.getNamespaceURI(),
                                                  attribute.getNamespacePrefix(),
                                                  attribute.getQualifiedName(),
                                                  attribute.getValue()));
            }
        }
        return attributes;
    }

    /**
     * If the TAL expression starts with "structure " and the following
     * expression is not an IncludableTemplate then
     * it will be treated as a HTML fragment.
     *
     * @param expression
     * @param expressionEvaluator
     * @return
     * @throws ExpressionEvaluationException
     */
    private Object processContent (String expression, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        // Structured text, preserve xml structure
        Object content;
        String textExpression;
        if (expression.startsWith("structure ")) {
            String structureExpression = expression.substring("structure ".length());
            Object structureContent = Expression.evaluate(structureExpression, expressionEvaluator);
            if ((structureContent == DEFAULT)
                || ((structureContent instanceof IncludableTemplate))
                || ((structureContent instanceof HTMLFragment))) {
                content = structureContent;
            }
            else {
                content = new HTMLFragment(structureContent == null ? ""
                                               : String.valueOf(structureContent), this.allowHtml);
            }
        }    // Unstructured text, return as plain old string
        else {
            if (expression.startsWith("text ")) {
                textExpression = expression.substring("text ".length());
            }
            else {
                textExpression = expression;
            }
            Object textContent = Expression.evaluate(textExpression, expressionEvaluator);
            if (textContent != DEFAULT) {
                content = textContent == null ? ""
                    : String.valueOf(textContent);
            }
            else {
                content = textContent;
            }
        }
        return content;
    }

    private void processDefine (String expression, ExpressionEvaluator beanShell) throws ExpressionEvaluationException {
        try {
            ExpressionTokenizer tokens = new ExpressionTokenizer(expression, ';', true);
            while (tokens.hasMoreTokens()) {
                String variable = tokens.nextToken().trim();
                int space = variable.indexOf(32);
                if (space == -1) {
                    throw new ExpressionSyntaxException("bad variable definition: " + variable);
                }
                String name = variable.substring(0, space);
                String valueExpression = variable.substring(space + 1).trim();
                Object value = Expression.evaluate(valueExpression, beanShell);
                beanShell.set(name, value);
            }
        }
        catch (EvalException e) {
            throw new ExpressionEvaluationException(e).setExpression(expression);
        }
    }

    private void processAttributes (Element element,
                                    List<SavedAttribute> attributes,
                                    String expression,
                                    ExpressionEvaluator beanShell)
        throws ExpressionEvaluationException {
        ExpressionTokenizer tokens = new ExpressionTokenizer(expression, ';', true);
        while (tokens.hasMoreTokens()) {
            String attribute = tokens.nextToken().trim();
            int space = attribute.indexOf(32);
            if (space == -1) {
                throw new ExpressionSyntaxException("bad attributes expression: " + attribute);
            }
            String qualifiedName = attribute.substring(0, space);
            String valueExpression = attribute.substring(space + 1).trim();
            Object value = Expression.evaluate(valueExpression, beanShell);

            if (value != DEFAULT) {
                removeAttribute(attributes, qualifiedName);
            }
            if ((value != null) && (value != DEFAULT)) {
                int colon = qualifiedName.indexOf(":");
                String prefix;
                String name;
                String uri;
                if (colon != -1) {
                    prefix = qualifiedName.substring(0, colon);
                    name = qualifiedName.substring(colon + 1);
                    uri = (String) this.prefixNamespaceUri.get(prefix);
                }
                else {
                    prefix = null;
                    name = qualifiedName;
                    uri = null;
                }
                attributes.add(new SavedAttribute(name, uri, prefix, qualifiedName, String.valueOf(value)));
            }
        }
    }

    private void removeAttribute (List<SavedAttribute> attributes, String qualifiedName) {
        boolean found = false;
        for (Iterator it = attributes.iterator(); (it.hasNext()) && (!found);) {
            SavedAttribute attribute = (SavedAttribute) it.next();
            if (attribute.getQualifiedName().equals(qualifiedName)) {
                it.remove();
                found = true;
            }
        }
    }

    private void processMacro (String expression,
                               Element element,
                               XMLStreamWriter writer,
                               ExpressionEvaluator beanShell,
                               Deque<Map<String, Slot>> slotStack)
        throws PageTemplateException, XMLStreamException {
        Object object = Expression.evaluate(expression, beanShell);
        if (object == null) {
            throw new NoSuchPathException("could not find macro: " + expression);
        }
        if ((object instanceof Macro)) {
            // Find slots to fill inside this macro call
            Map<String, Slot> slots = new HashMap<String, Slot>();
            findSlots(element, slots);
            // Slots filled in later templates (processed earlier)
            // Take precedence over slots filled in intermediate
            // templates.
            if (!slotStack.isEmpty()) {
                Map<String, Slot> laterSlots = slotStack.peek();
                slots.putAll(laterSlots);
            }
            slotStack.push(slots);
            // Call macro
            Macro macro = (Macro) object;
            macro.process(writer, beanShell, slotStack);
        }
        else {
            throw new ExpressionEvaluationException(
                "Expression '" + expression + "' does not evaluate to macro: "
                    + object.getClass().getName()).setExpression(expression);
        }
    }

    /**
     * With all of our startTag woes, getting an XPath expression
     * to work has proven futile, so we'll recurse through the tree
     * ourselves to find what we need.
     *
     * @param element
     * @param slots
     */
    private void findSlots (Element element, Map<String, Slot> slots) {
        // Look for our attribute
        Attribute attribute = findAttribute(element, "http://xml.zope.org/namespaces/metal", "fill-slot");
        if (attribute != null) {
            slots.put(attribute.getValue(), new SlotImpl(element));
        }
        // Recurse into child elements
        for (Object o : element.getChildren()) {
            findSlots((Element) o, slots);
        }
    }

    /**
     * * With all of our startTag woes, getting an XPath expression
     * to work has proven futile, so we'll recurse through the tree
     * ourselves to find what we need.
     *
     * @param element
     * @param macros
     */
    private void findMacros (Element element, Map<String, Macro> macros) {
        // Process any declared namespaces
        for (Object o : element.getAdditionalNamespaces()) {
            Namespace namespace = (Namespace) o;
            addNamespace(namespace);
        }
        // Look for our attribute
        Attribute attribute = findAttribute(element, "http://xml.zope.org/namespaces/metal", "define-macro");
        if (attribute != null) {
            macros.put(attribute.getValue(), new MacroImpl(element));
        }
        // Recurse into child elements
        for (Iterator i = element.getChildren().iterator(); i.hasNext();) {
            findMacros((Element) i.next(), macros);
        }
    }

    private Attribute findAttribute (Element element, String uri, String name) {
        Attribute foundAttribute = null;
        Iterator it = element.getAttributes().iterator();
        while ((it.hasNext()) && (foundAttribute == null)) {
            Attribute attribute = (Attribute) it.next();
            if ((attribute.getName().equals(name))
                && (attribute.getNamespace().getURI().equals(uri))) {
                foundAttribute = attribute;
            }
        }
        return foundAttribute;
    }

    /**
     * @return macros used by this template
     */
    public Map<String, Macro> getMacros () {
        findMacros(this.doc.getRootElement(), this.macros);
        return this.macros;
    }

    public boolean isSuppressDeclaration () {
        return suppressDeclaration;
    }

    /**
     * Used by includable Template, for macros
     *
     * @param suppressDeclaration
     */
    public void setSuppressDeclaration (boolean suppressDeclaration) {
        this.suppressDeclaration = suppressDeclaration;
    }

    @Override
    public String toString () {
        return "PageTemplateImpl{" +
            "url=" + url +
            '}';
    }

    /**
     * Data structure placeholder class
     */
    class SlotImpl implements Slot {
        Element element;

        SlotImpl (Element element) {
            this.element = element;
        }

        public void process (XMLStreamWriter writer,
                             ExpressionEvaluator expressionEvaluator,
                             Deque<Map<String, Slot>> slotStack)
            throws PageTemplateException, XMLStreamException {
            PageTemplateImpl.this.processElement(this.element, writer,
                                                 expressionEvaluator, slotStack);
        }
    }

    /**
     * Data structure placeholder for macro implementation
     */
    public class MacroImpl implements Macro {
        private Element element;

        private MacroImpl (Element element) {
            this.element = element;
        }

        public Element getElement () {
            return this.element;
        }

        public void process (XMLStreamWriter writer,
                             ExpressionEvaluator expressionEvaluator,
                             Deque<Map<String, Slot>> slotStack)
            throws PageTemplateException, XMLStreamException {
            PageTemplateImpl.this.processElement(this.element, writer,
                                                 expressionEvaluator, slotStack);
        }
    }

    /**
     * Default Resolver for loading template from URL
     */
    class DefaultResolver extends Resolver {
        URIResolver uriResolver;

        DefaultResolver () {
            if (PageTemplateImpl.this.uri != null) {
                this.uriResolver = new URIResolver(PageTemplateImpl.this.uri);
            }
        }

        public URL getResource (String path) throws IOException {
            URL resource = null;
            // If user has supplied resolver, use it
            if (PageTemplateImpl.this.userResolver != null) {
                resource = PageTemplateImpl.this.userResolver.getResource(path);
            }

            /**
             * If resource not found by user resolver
             * fall back to resolving by uri
             */
            if ((resource == null) && (this.uriResolver != null)) {
                resource = this.uriResolver.getResource(path);
            }
            return resource;
        }

        public PageTemplate getPageTemplate (String path)
            throws PageTemplateException, IOException {
            PageTemplate template = null;
            // If user has supplied resolver, use it
            if (PageTemplateImpl.this.userResolver != null) {
                template = PageTemplateImpl.this.userResolver.getPageTemplate(path);
                // template inherits user resolver
                template.setResolver(PageTemplateImpl.this.userResolver);
            }
            // If template not found by user resolver
            // fall back to resolving by uri
            if ((template == null) && (this.uriResolver != null)) {
                template = this.uriResolver.getPageTemplate(path);
            }
            return template;
        }
    }

    /**
     * Data structure placeholder class
     */
    static class SavedAttribute {
        private final String prefix;
        private final String localName;
        private final String namespaceUri;
        private final String qualifiedName;
        private final String value;

        SavedAttribute (String localName, String namespaceUri, String prefix,
                        String qualifiedName, String value) {
            this.localName = localName;
            this.namespaceUri = namespaceUri;
            this.prefix = prefix;
            this.qualifiedName = qualifiedName;
            this.value = value;
        }

        public String getPrefix () {
            return this.prefix;
        }

        public String getLocalPart () {
            return this.localName;
        }

        public String getNamespaceUri () {
            return this.namespaceUri;
        }

        public String getValue () {
            return this.value;
        }

        public String getQualifiedName () {
            return this.qualifiedName;
        }
    }
}
