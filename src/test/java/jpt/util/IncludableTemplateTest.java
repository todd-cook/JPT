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

import jpt.JptTestUtil;
import jpt.MacrosTest;
import jpt.PageTemplate;
import jpt.PageTemplateImpl;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class IncludableTemplateTest extends JptTestUtil {

    MacrosTest mt = new MacrosTest();

    class TestData {
        public String getName () {
            return "testObject";
        }
    }

    @Test
    public void testIncludableTemplate () {
        String templateText = MacrosTest.templateWithMacro;
        PageTemplate template = new PageTemplateImpl.Builder()
            .templateText(templateText)
            .suppressDeclaration(true)
            .resolver(mt.createMacroResolver())
            .build();
        Map<String, Object> dictionary = new HashMap<String, Object>();
        dictionary.put("addressNumber", 123456);
        TestData td = new TestData();
        IncludableTemplate it = new IncludableTemplate(template, td, dictionary);
        assertNotNull(it.getContext());
        assertEquals(td, it.getContext());
        assertNotNull(it.getMacros());
        assertNotNull(it.toString());
    }
}
