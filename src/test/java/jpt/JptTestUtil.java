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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Java Page Templates Utility class
 * <p/>
 * First time using JPT?
 * Set the System properties:
 * -DJptTestUtil.SHOW_TRANSFORMATIONS=true
 * -DJptTestUtil.WRITE_FILES=true
 * Show Tranformations will show the before and after transformations of each
 * Java Page Template.
 * Write Files will cause the before and after
 *
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class JptTestUtil {
    protected static Logger LOG = LoggerFactory.getLogger (JptTestUtil.class);

    public static final String startTag = "<html xmlns:tal=\"" + Constants.TAL_NAMESPACE_URI + "\">";
    public static final String endTag = "</html>";
    /**
     * If yu want to see a list of the transformations (before & after)
     * set this to true
     */
    private boolean SHOW_TRANSFORMATIONS = false;

    /**
     * First experience with JPT? Start here; set to true via setting a
     * System.property
     */
    private boolean WRITE_FILES = false;

    public JptTestUtil () {

        WRITE_FILES = Boolean.valueOf(
            System.getProperty("JptTestUtil.WRITE_FILES"));
        SHOW_TRANSFORMATIONS = Boolean.valueOf(
            System.getProperty("JptTestUtil.SHOW_TRANSFORMATIONS"));
    }

    /**
     * @param templateText is HTML with TAL expressions; must be not null
     * @param bean         an POJO object with Getters and public methods may be null
     * @param dictionary   is a Map of <String, Object>; may be null
     * @return String text of the TAL processed template; or null if failure
     */
    protected String processTemplate (String templateText, Object bean, Map<String, Object> dictionary) {

        PageTemplate template = new PageTemplateImpl.Builder()
            .templateText(templateText)
            .suppressDeclaration(true)
            .allowHtml(true)
            .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            template.process(buffer, bean, dictionary);
            byte[] resultBinary = buffer.toByteArray();
            return removeAllBreaks(new String(resultBinary, "UTF-8"));
        }
        catch (PageTemplateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param templateText is HTML with TAL expressions; must be not null
     * @param bean         an POJO object with Getters and public methods may be null
     * @param dictionary   is a Map of <String, Object>; may be null
     * @param resolver     custom resolver for loading macro from memory
     * @return String text of the TAL processed template
     */
    protected String processTemplate (String templateText, Object bean, Map<String, Object> dictionary, Resolver resolver) {

        PageTemplate template = new PageTemplateImpl.Builder()
            .templateText(templateText)
            .resolver(resolver)
            .suppressDeclaration(true)
            .allowHtml(true)
            .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            template.process(buffer, bean, dictionary);
            byte[] resultBinary = buffer.toByteArray();
            return removeAllBreaks(new String(resultBinary, "UTF-8"));
        }
        catch (PageTemplateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void showTransformation (String start, String finish) {
        if (SHOW_TRANSFORMATIONS) {
            LOG.info ( removeAllBreaks (start) + "\n becomes \n" + removeAllBreaks (finish));
        }

        if (WRITE_FILES) {
            long timestamp = System.nanoTime();
            String tmpDir = System.getProperty("java.io.tmpdir");
            String fileOne = tmpDir + timestamp + "_1a.html";
            String fileTwo = tmpDir + timestamp + "_1b.html";
            try {
                BufferedWriter fOne = new BufferedWriter(new FileWriter(fileOne));
                LOG.info  ("Writing file: " + fileOne);
                fOne.write(start);
                fOne.close();
                BufferedWriter fTwo = new BufferedWriter(new FileWriter(fileTwo));
                LOG.info  ("Writing file: " + fileTwo);
                fTwo.write(finish);
                fTwo.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected String removeAllBreaks (String text) {
        text = text.replaceAll("\n", "");
        return text.replaceAll("\r", "");
    }
}
