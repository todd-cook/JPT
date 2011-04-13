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

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class MiscellaneousTests extends JptTestUtil {

    @Test
    public void ExpressionScriptCreation () {

        try {
            ExpressionScript es = new ExpressionScript(new StringReader("java: 5 + 4"));
            assertNotNull(es.getScript());
            assertNotNull(es.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void HTMLFragmentCreation () {

        String templateText = "<p>Markdown is the new buzzword</p>";
        HTMLFragment hf = new HTMLFragment(templateText, true);
        assertNotNull(hf.getHtml());
        assertNotNull(hf.toString());
        try {
            assertNotNull(hf.getXhtml());
            assertTrue(templateText.equals(hf.getXhtml()));

        }
        catch (PageTemplateException e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(templateText.equals(hf.getHtml()));
        assertTrue(templateText.equals(hf.toString()));
    }
}