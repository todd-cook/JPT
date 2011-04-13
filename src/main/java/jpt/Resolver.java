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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used to locate templates, and Expression Scripts
 * *
 *
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public abstract class Resolver implements Serializable {
    private static final long serialVersionUID = -8112477389545806195L;

    // Map of resources called by this template
    protected Map<String, PageTemplate> templates =
        new ConcurrentHashMap<String, PageTemplate>();
    protected Map<String, ExpressionScript> scripts =
        new ConcurrentHashMap<String, ExpressionScript>();
    protected boolean disableJptTemplateCache = false;

    protected abstract URL getResource (String paramString)
        throws IOException;

    protected Resolver () {
    }

    protected Resolver (Map<String, PageTemplate> templates) {
        this.templates = templates;
    }

    protected Resolver (Map<String, PageTemplate> templates,
                        Map<String, ExpressionScript> scripts) {
        this.templates = templates;
        this.scripts = scripts;
    }

    protected Resolver (Map<String, PageTemplate> templates,
                        Map<String, ExpressionScript> scripts,
                        boolean disableJptTemplateCache) {
        this.templates = templates;
        this.scripts = scripts;
        this.disableJptTemplateCache = disableJptTemplateCache;
    }

    protected PageTemplate getPageTemplate (String path)
        throws PageTemplateException, IOException {
        PageTemplate template = null;
        if ((!disableJptTemplateCache) && (templates.containsKey(path))) {
            template = templates.get(path);
        }
        else {
            URL resource = getResource(path);
            if (resource != null) {
                template =
                    new PageTemplateImpl.Builder().url(resource).build();
                if (!disableJptTemplateCache) {
                    templates.put(path, template);
                }
            }
            else {
                template = null;
            }
        }
        return template;
    }

    protected ExpressionScript getExpressionScript (String path)
        throws MalformedURLException, IOException {
        ExpressionScript script = scripts.get(path);
        if (script == null) {
            URL resource = getResource(path);
            if (resource != null) {
                script = new ExpressionScript(
                    new InputStreamReader(resource.openStream()));
                scripts.put(path, script);
            }
        }
        return script;
    }

    protected void setDisableJptTemplateCache (boolean disable) {
        disableJptTemplateCache = disable;
    }
}
