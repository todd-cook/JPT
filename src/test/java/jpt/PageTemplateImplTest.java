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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class PageTemplateImplTest extends JptTestUtil {

    /**
     * Show what the suppress declaration parameter does
     * Note, in either case the zope startTag disappears
     */
    @Test
    public void pageTemplateConfigurationSuppressDeclarationFalse () {

        /** Of course, if there is no text data between the tags,
         * the parser will replace the tag thus: <html />
         */
        String templateText = "<html xmlns:tal=\"http://xml.zope.org/namespaces/tal\">abc</html>";
        // Without suppressing the declaration
        String expectedResult = "<?xml version='1.0' encoding='UTF-8'?><html>abc</html>";
        try {
            PageTemplate template = new PageTemplateImpl.Builder()
                .templateText(templateText)
                .suppressDeclaration(false)
                .build();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            template.process(buffer, null, null);
            byte[] resultBinary = buffer.toByteArray();
            String result = removeAllBreaks(new String(resultBinary, "UTF-8"));
            showTransformation(templateText, result);
            assertTrue(result.equals(expectedResult));

            // Now trying with a suppressed declaration
            expectedResult = "<html>abc</html>";
            template = new PageTemplateImpl.Builder()
                .templateText(templateText)
                .suppressDeclaration(true)
                .build();
            buffer = new ByteArrayOutputStream();
            template.process(buffer, null, null);
            resultBinary = buffer.toByteArray();
            result = removeAllBreaks(new String(resultBinary, "UTF-8"));
            showTransformation(templateText, result);
            assertTrue(result.equals(expectedResult));
        }
        catch (PageTemplateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
