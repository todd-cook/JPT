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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertTrue;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class MacrosTest extends JptTestUtil {

    private MacroResolver macroResolver = createMacroResolver ();

    public static final String pageMacro =
            "<html xmlns:macro=\"http://xml.zope.org/namespaces/metal\" macro:define-macro=\"page\">\n" +
                    "<head>\n" +
                    "<title macro:define-slot=\"title\">The title of this page</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<table>\n" +
                    "<tr>\n" +
                    "<td colspan=\"2\"><h1>This is our page header</h1></td>\n" +
                    "</tr>\n" +
                    "<tr>\n" +
                    "<td>\n" +
                    "This is stuff over here on the side:\n" +
                    "<ul>\n" +
                    "<li>one</li>\n" +
                    "<li>two</li>\n" +
                    "<li>three</li>\n" +
                    "<li>four</li>\n" +
                    "</ul>\n" +
                    "</td>\n" +
                    "<td>\n" +
                    "<p macro:define-slot=\"content\">\n" +
                    "The content for this page goes here.\n" +
                    "</p>\n" +
                    "</td>\n" +
                    "</tr>\n" +
                    "</table>\n" +
                    "</body>\n" +
                    "</html>";


    private static final String baseMacro = "<html xmlns:macro=\"http://xml.zope.org/namespaces/metal\" macro:define-macro=\"page\">\n" +
            "<head>\n" +
            "<title macro:define-slot=\"title\">The title of this page</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<span macro:define-slot=\"content\">\n" +
            "This is a pretty generic page that can get customized by calling templates.\n" +
            "</span>\n" +
            "</body>\n" +
            "</html>";


    private static final String page2Macro =
            "  <html xmlns:macro=\"http://xml.zope.org/namespaces/metal\" macro:define-macro=\"page\">\n" +
                    "<head>\n" +
                    "<title macro:define-slot=\"title\">The title of this page</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<table>\n" +
                    "<tr>\n" +
                    "<td colspan=\"2\"><h1>This is our page header</h1></td>\n" +
                    "</tr>\n" +
                    "<tr>\n" +
                    "<td>\n" +
                    "This is stuff over here on the side:\n" +
                    "<ul>\n" +
                    "<li>one</li>\n" +
                    "<li>two</li>\n" +
                    "<li>three</li>\n" +
                    "<li>four</li>\n" +
                    "</ul>\n" +
                    "</td>\n" +
                    "<td>\n" +
                    "<p macro:define-slot=\"content\">\n" +
                    "The content for this page goes here.\n" +
                    "</p>\n" +
                    "</td>\n" +
                    "</tr>\n" +
                    "</table>\n" +
                    "</body>\n" +
                    "</html>";


    /**
     * Used also by IncludableTemplateTest
     * @return
     */
    public MacroResolver createMacroResolver () {
        Map<String, PageTemplate> macroMap =
                new ConcurrentHashMap<String, PageTemplate> ();
        macroMap.put ("page.jpt", new PageTemplateImpl.Builder ().templateText (pageMacro).build ());
        macroMap.put ("base.jpt", new PageTemplateImpl.Builder ().templateText (baseMacro).build ());
        macroMap.put ("page2.jpt", new PageTemplateImpl.Builder ().templateText (page2Macro).build ());
        return new MacroResolver (macroMap);
    }

   // used also by IncludableTemplateTest
    public static final String templateWithMacro =    "<html xmlns:tal=\"http://xml.zope.org/namespaces/tal\"\n" +
                        " xmlns:metal=\"http://xml.zope.org/namespaces/metal\"\n" +
                        " metal:use-macro=\"resolver/getPageTemplate( 'page.jpt' )/macros/page\">\n" +
                        "<head>\n" +
                        "<title metal:fill-slot=\"title\">Lovingly Handcrafted Content</title>\n" +
                        "</head>\n" +
                        "<p metal:fill-slot=\"content\">\n" +
                        " This extraordinarily interesting content has been lovingly handcrafted\n" +
                        " by highly qualified content managers.  tsongas is <span tal:replace=\"tsongas\"/>\n" +
                        "</p>\n" +
                        "</html>";


    @Test
    public void macroTest () {
        Map<String, Object> dictionary = new HashMap<String, Object> ();
        dictionary.put ("tsongas", "alive and well");
        dictionary.put ("algonquin", "books");
        String result = processTemplate (templateWithMacro, null, dictionary, macroResolver);
        String trueResult = "<html><head><title>Lovingly Handcrafted Content</title></head><body>" +
                "<table><tr><td colspan=\"2\"><h1>This is our page header</h1></td></tr>" +
                "<tr><td>This is stuff over here on the side:<ul><li>one</li><li>two</li><li>three</li><li>four</li></ul></td>" +
                "<td><p> This extraordinarily interesting content has been lovingly handcrafted by highly qualified content managers.  tsongas is alive and well</p></td></tr>" +
                "</table></body></html>";
        assertTrue (!templateWithMacro.equals (result));
        showTransformation (templateWithMacro, result);
        assertTrue (trueResult.equals (removeAllBreaks (result)));
    }

    @Test
    public void macro2Test () {
        String templateText =
                "<html xmlns:tal=\"http://xml.zope.org/namespaces/tal\"\n" +
                        " xmlns:metal=\"http://xml.zope.org/namespaces/metal\"\n" +
                        " metal:use-macro=\"resolver/getPageTemplate( 'page2.jpt' )/macros/page\">\n" +
                        "<title metal:fill-slot=\"title\">My screed</title>\n" +
                        "<td metal:fill-slot=\"sidebar\">\n" +
                        "Genuine contemplation is always pushed to the margins.\n" +
                        "</td>\n" +
                        "<td metal:fill-slot=\"main\">\n" +
                        "While vacuity and drivel take center stage.\n" +
                        "</td>\n" +
                        "</html>";
        String result = processTemplate (templateText, null, null, macroResolver);
        String trueResult = "<html><head><title>My screed</title></head><body><table>" +
                "<tr><td colspan=\"2\"><h1>This is our page header</h1></td></tr>" +
                "<tr><td>This is stuff over here on the side:<ul><li>one</li><li>two</li><li>three</li><li>four</li></ul></td>" +
                "<td><p>The content for this page goes here.</p></td></tr></table></body></html>";
        assertTrue (!templateText.equals (result));
        showTransformation (templateText, result);
        assertTrue (trueResult.equals (removeAllBreaks (result)));
    }
}