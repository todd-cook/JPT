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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * A unit test designed to be read; everything should be:
 * right on the screen, easy to follow--as much as possible.
 *
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class ExpressionsTest extends JptTestUtil {

    @Test
    public void blankExpression () {
        String templateText = startTag + "<p>A blank expression should be <i tal:content=\"\">blank</i></p>" + endTag;
        String result = processTemplate(templateText, null, null);
        String trueResult = "<html><p>A blank expression should be <i></i></p></html>";
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    /**
     * Used below to show how JPT gets data from bean-like objects;
     * note: setters not required
     */
    class Friend {
        public Map<String, Object> map = new HashMap<String, Object>();

        Friend () {
            map.put("what", false);
        }

        public Map<String, Object> getMap () {
            return map;
        }

        public int getNumber () {
            return 5;
        }

    }

    class TestBean {
        private Friend friend = new Friend();

        public String getFavoriteColor () {
            return "red";
        }

        public Friend getFriend () {
            return friend;
        }

        // note: public member variables not accessible to JPT; need a getter
        public int id = 123456;
    }

    @Test
    public void beanValuesInsertion () {
        String templateText = startTag +
            "<p>here/favoriteColor should be <b tal:content=\"here/favoriteColor\">a color</b></p>\n" +
            "<p>here/friend/number should be <div tal:content=\"here/friend/number\">a number</div></p>" + endTag;
        String result = processTemplate(templateText, new TestBean(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>here/favoriteColor should be <b>red</b></p>" +
            "<p>here/friend/number should be <div>5</div></p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    /**
     * To insert HTML the TAL tag must start with structure,
     * e.g.
     * <code> <b tal:content="structure favoriteColor">a color</b> </code>
     * This means that the following data structure should be preserved as an
     * HTML fragment
     */

    @Test
    public void mapValuesInsertion () {
        String templateText = startTag +
            "<p>favoriteColor should be <b tal:content=\"structure favoriteColor\">a color</b></p>\n" +
            "<p>friend should be <div tal:content=\"structure friend\">a person</div></p>" + endTag;
        //String result = processTemplate (templateText, new TestBean (), null);
        Map<String, Object> dictionary = new HashMap<String, Object>();
        dictionary.put("favoriteColor", "<a href=\"www.red.com\">red site</a>");
        dictionary.put("friend", "<a href=\"www.banksy.com\">banksy site</a>");

        String result = processTemplate(templateText, null, dictionary);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>favoriteColor should be <b><a href=\"www.red.com\">red site</a></b></p>" +
            "<p>friend should be <div><a href=\"www.banksy.com\">banksy site</a></div></p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void literals () {
        String templateText = startTag + "<p>'this is a string literal' should be <span tal:replace=\"'this is a string literal'\">the literal</span></p>\n" +
            "<p>123l should be <span tal:replace=\"123l\">a long</span> and should have a class of \n" +
            "<span tal:replace=\"123l/class/name\">class</span></p>\n" +
            "<p>123 should be <span tal:replace=\"123\">an integer</span> and should have a class of \n" +
            "<span tal:replace=\"123/class/name\">class</span></p>\n" +
            "<p>123.45d should be <span tal:replace=\"123.45d\">a double</span> and should have a class of \n" +
            "<span tal:replace=\"123.45d/class/name\">class</span></p>\n" +
            "<p>123.45f should be <span tal:replace=\"123.45f\">a float</span> and should have a class of \n" +
            "<span tal:replace=\"123.45f/class/name\">class</span></p>\n" +
            "<p>123.45 should be <span tal:replace=\"123.45\">a float</span> and should have a class of \n" +
            "<span tal:replace=\"123.45/class/name\">class</span></p>\n" +
            "<p>true should be <span tal:replace=\"true\">not false</span> and should have a class of \n" +
            "<span tal:replace=\"true/class/name\">class</span></p>\n" +
            "<p>false should be <span tal:replace=\"false\">not true</span> and should have a class of \n" +
            "<span tal:replace=\"false/class/name\">class</span></p>" + endTag;
        String result = processTemplate(templateText, new TestBean(), null);
        assertTrue(!templateText.equals(result));
        String trueResult = "<html>" +
            "<p>'this is a string literal' should be this is a string literal</p>" +
            "<p>123l should be 123 and should have a class of java.lang.Long</p>" +
            "<p>123 should be 123 and should have a class of java.lang.Integer</p>" +
            "<p>123.45d should be 123.45 and should have a class of java.lang.Double</p>" +
            "<p>123.45f should be 123.45 and should have a class of java.lang.Float</p>" +
            "<p>123.45 should be 123.45 and should have a class of java.lang.Float</p>" +
            "<p>true should be true and should have a class of java.lang.Boolean</p>" +
            "<p>false should be false and should have a class of java.lang.Boolean</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    /**
     * Class used to show that JPT can call methods on a provided object
     */
    class MyMath {
        public int add (int a, int b) {
            return a + b;
        }

        public int subtract (int a, int b) {
            return a - b;
        }

        public int divide (int a, int b) {
            if (b == 0) {
                throw new IllegalArgumentException();
            }
            else {
                return a / b;
            }
        }

        private int X = 1024;
        private int y = 55;

        public int getX () {
            return X;
        }

        public int getY () {
            return y;
        }
    }

    @Test
    public void objectMethodCalls () {
        String templateText = startTag + "<p>2 + 3 = <span tal:replace=\"here/add( 2, 3 )\">a number</span></p>\n" +
            "<p>this is equivalent to <span tal:replace=\"here/getX()\">here/getX()</span></p>\n" +
            "<p>1024 - 55 = <span tal:replace=\"here/subtract ( here/getX(), here/getY() )\">a number</span></p>\n" +
            "<p>1024 / 55 = <span tal:replace=\"here/divide ( here/getX(), here/getY() )\">a number</span></p>\n" + endTag;
        String result = processTemplate(templateText, new MyMath(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>2 + 3 = 5</p><p>this is equivalent to 1024</p><p>1024 - 55 = 969</p><p>1024 / 55 = 18</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    /**
     * If a provided object's method throws an exception,
     * then JPT logs the Exception, but continues generating the page,
     * even if it's a Runtime Exception, e.g. IllegalArgumentException
     */
    @Test
    public void objectMethodExceptionSwallowing () {
        String templateText = startTag + "<p>1024 / 0 = <span tal:replace=\"here/divide ( here/getX(), 0)\">a number</span></p>\n" + endTag;
        LOG.info("NOTE: the following stack trace is expected:");
        String result = processTemplate(templateText, new MyMath(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>1024 / 0 = </p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class Enemy {
        Map<String, Object> space = new HashMap<String, Object>();

        Enemy () {
            space.put("cowboy", "");
        }

        public Map<String, Object> getSpace () {
            return space;
        }
    }

    class ProvidedObject {
        private Map<String, Object> map = new HashMap<String, Object>();

        private Friend friend = new Friend();

        public Friend getFriend () {
            return friend;
        }

        private Enemy enemy = new Enemy();

        public Enemy getEnemy () {
            return enemy;
        }

        ProvidedObject () {
            map.put("friend", "kevin");
            //     map.put( "enemy", "mc2" );
            map.put("hello", "ninety nine");
        }

        public Map<String, Object> getMap () {
            return map;
        }
    }

    @Test
    public void mapReferences () {
        String templateText = startTag + "<p>here/map/friend should be <span tal:replace=\"here/map/friend\">a name</span></p>\n" +
            "<p>here/map/friend/length() should be <span tal:replace=\"here/map/friend/length()\">five</span></p>\n" +
            "<p>here/map/dummy should be <span tal:replace=\"here/map/dummy\"> empty </span></p>\n" +
            "<p>here/map/hello should be <span tal:replace=\"here/map/hello\">ninety nine</span></p>\n" + endTag;
        String result = processTemplate(templateText, new ProvidedObject(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>here/map/friend should be kevin</p><p>here/map/friend/length() should be 5</p>" +
            "<p>here/map/dummy should be </p><p>here/map/hello should be ninety nine</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void ExistsExpressions () {
        String templateText = startTag + "<p>exists:here should be <span tal:replace=\"exists:here\">true </span></p>\n" +
            "<p>exists:here/map/friend should be <span tal:replace=\"exists:here/map/friend\"> true </span></p>\n" +
            "<p>exists:here/map/enemy should be <span tal:replace=\"exists:here/map/enemy\">not true</span></p>" + endTag;
        String result = processTemplate(templateText, new ProvidedObject(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>exists:here should be true</p>" +
            "<p>exists:here/map/friend should be true</p>" +
            "<p>exists:here/map/enemy should be false</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void NotExpressions () {
        String templateText = startTag + "<p>not:here/friend/map/what should be <span tal:replace=\"not:here/friend/map/what\">not false</span></p>" + endTag;
        String result = processTemplate(templateText, new ProvidedObject(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>not:here/friend/map/what should be true</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    /**
     * Data object for string expressions
     */
    class DataObject {

        DataObject2 do2 = new DataObject2();

        public DataObject2 getFriend () {
            return do2;
        }
    }

    class DataObject2 {
        public int getNumber () {
            return 6;
        }
    }

    @Test
    public void StringExpressions () {
        String templateText = startTag + "<p>string: should be <span tal:replace=\"string:\">blank</span></p>\n" +
            "<p>string:hello should be <span tal:replace=\"string:hello\">a greeting</span></p>\n" +
            "<p>string:www.${opinions}.org should be <span tal:replace=\"string:www.${opinions}.org\">are like</span></p>\n" +
            "<p>string:give me $$${helper/friend/number} or else should be <span tal:replace=\"string:give me $$${helper/friend/number} or else\">a threat</span>!</p>" + endTag;
        Map<String, Object> dictionary = new HashMap<String, Object>();
        dictionary.put("hello", "hey");
        dictionary.put("opinions", "everybodysgotone");
        dictionary.put("helper", new DataObject());
        String result = processTemplate(templateText, new MyMath(), dictionary);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>string: should be </p><p>string:hello should be hello</p>" +
            "<p>string:www.${opinions}.org should be www.everybodysgotone.org</p>" +
            "<p>string:give me $$${helper/friend/number} or else should be give me $6 or else!</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class StructuredText {
        public String getDiatribe () {
            return "<b>The cabinet</b> has <i>usurped</i> the authority of the <h3>president</h3>";
        }

        public String getDiatribe2 () {
            return "<b>The cabinet</b> has <i>usurped</i> the authority of the <h3>president</h3>";
        }

        public HTMLFragment getDiatribe3 () throws Exception {
            return new HTMLFragment("<b>The cabinet</b> has <i>usurped</i> the authority of the <h3>president</h3>", true);
        }
    }

    @Test
    public void StructuredText () {
        String templateText = startTag + "<p>escaped: <span tal:replace=\"here/diatribe3\">escaped text</span></p>\n" +
            "<p>escaped: <span tal:replace=\"text here/diatribe3/html\">escaped text</span></p>\n" +
            "<p>escapedx: <span tal:replace=\"text here/diatribe3/xhtml\">escaped text</span></p>\n" +
            "<p>structured: <span tal:replace=\"structure here/diatribe\">structured text</span></p>\n" +
            "<p>structured: <span tal:replace=\"structure here/diatribe3\">structured text</span></p>\n" +
            "<p>structured2: <span tal:replace=\"structure here/diatribe2\">structured text</span></p>" + endTag;
        String result = processTemplate(templateText, new StructuredText(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>escaped: &lt;b>The cabinet&lt;/b> has &lt;i>usurped&lt;/i> the authority of the &lt;h3>president&lt;/h3></p>" +
            "<p>escaped: &lt;b>The cabinet&lt;/b> has &lt;i>usurped&lt;/i> the authority of the &lt;h3>president&lt;/h3></p>" +
            "<p>escapedx: &lt;b>The cabinet&lt;/b> has &lt;i>usurped&lt;/i> the authority of the &lt;h3>president&lt;/h3></p>" +
            "<p>structured: <b>The cabinet</b> has <i>usurped</i> the authority of the <h3>president</h3></p>" +
            "<p>structured: <b>The cabinet</b> has <i>usurped</i> the authority of the <h3>president</h3></p>" +
            "<p>structured2: <b>The cabinet</b> has <i>usurped</i> the authority of the <h3>president</h3></p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class DataObject3 {
        public String getFriend () {
            return "Albert";
        }

        public Object getEnemy () {
            return null;
        }
    }

    @Test
    public void AlternatePathSegments () {
        String templateText = startTag + "<p>use first path: <span tal:replace=\"here/friend | string:no friends\">any friends?</span></p>\n" +
            "<p>null: <span tal:replace=\"here/enemy | string:no enemies\">any enemies?</span></p>\n" +
            "<p>no such path: <span tal:replace=\"here/enemy/space/cowboy | string:no space for cowboys\">any space for cowboys?</span></p>\n" +
            "<p>both: <span tal:replace=\"here/enemy | here/enemy/space/cowboy | here/friend\">anybody?</span></p>" + endTag;
        String result = processTemplate(templateText, new DataObject3(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>use first path: Albert</p>" +
            "<p>null: no enemies</p>" +
            "<p>no such path: no space for cowboys</p>" +
            "<p>both: Albert</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class Stuff {
        Map<String, Object> map = new HashMap<String, Object>();

        public Map<String, Object> getMap () {
            return map;
        }

        Stuff () {
            map.put("friend", "kevin");
        }
    }

    class PathTokenIndirectionObject {
        public Stuff getFriend () {
            return new Stuff();
        }
    }

    @Test
    public void PathTokenIndirection () {
        String templateText = startTag + "<p>a friend of a friend: <span tal:replace=\"here/?acquaintance/map/friend\">friend</span></p>" + endTag;
        Map<String, Object> dictionary = new HashMap<String, Object>();
        dictionary.put("acquaintance", "friend");
        String result = processTemplate(templateText, new PathTokenIndirectionObject(), dictionary);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>a friend of a friend: kevin</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class NumberFriend {
        public int getNumber () {
            return 5;
        }
    }

    class FunWithClassesObject {
        public NumberFriend getFriend () {
            return new NumberFriend();
        }
    }

    @Test
    public void FunWithClasses () {
        String templateText = startTag + "<p>number instanceof Integer: <span tal:replace=\"java.lang.Integer.class/isInstance( here/friend/number )\">should be</span></p>\n" +
            "<p>categorically false: <span tal:replace=\"java.lang.Integer.class/isAssignableFrom( java.lang.String.class )\">i don't think so</span></p>" + endTag;
        String result = processTemplate(templateText, new FunWithClassesObject(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>number instanceof Integer: true</p><p>categorically false: false</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    /**
     * Placing code inside a page isn't a good idea, but
     * JPT will give yourself "enough rope to shoot yourself in the foot"
     */
    @Test
    public void JavaExpressionLangauge () {
        String templateText = startTag + "<p> 9 <span tal:replace=\"java: 5 + 4\">5 + 4</span></p>\n" +
            "<p> 1 <span tal:replace=\"java: 5 -4\">5 - 4</span></p>\n" +
            "<p> 20 <span tal:replace=\"java: 5 * 4\">5 * 4</span></p>\n" +
            "<p> 23 <span tal:replace=\"java: 69 / 3\">69 / 3</span></p>\n" +
            "<p> 2 <span tal:replace=\"java:23 % 7\">23 % 7</span></p>" + endTag;
        String result = processTemplate(templateText, new MyMath(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p> 9 9</p><p> 1 5</p><p> 20 20</p><p> 23 23</p><p> 2 2</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class DateObject {
        public Date getBirthday () {
            return new Date(323424000007L);  // Tue April 1, 1980 12:00:00 AM
        }
    }

    @Test
    public void DateHelper () {
        String templateText = startTag + "<span tal:define=\"dateFormatString 'EEE MMMM d, yyyy h:mm:ss a';\n" +
            "    dateFormat java:new java.text.SimpleDateFormat( dateFormatString )\" />\n" +
            "<p>my birthday <span tal:replace=\"dateFormat/format( here/birthday )\">a date</span></p>" + endTag;
        String result = processTemplate(templateText, new DateObject(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><span/><p>my birthday Tue April 1, 1980 12:00:00 AM</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    /**
     * Data object to show TAL array access
     */
    class MyArrays {
        String[] animals = {
            "horse",
            "dog",
            "cat",
            "pig",
            "crocodile"
        };

        int[][] table = {
            {1, 2, 3, 4},
            {5, 6, 7, 8},
            {9, 10}
        };

        public int[][] getTable () {
            return table;
        }

        public String[] getAnimals () {
            return animals;
        }
    }

    @Test
    public void AnArray () {
        String templateText = startTag + "<span tal:define=\"animals here/animals\" />\n" +
            "<p>crocodile <span tal:replace=\"animals[4]\">an animal</span></p>\n" +
            "<p>cat <span tal:replace=\"animals[2]\">an animal</span></p>" + endTag;
        String result = processTemplate(templateText, new MyArrays(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><span/><p>crocodile crocodile</p><p>cat cat</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void MoreComplicatedArray () {
        String templateText = startTag + "<span tal:define=\"array here/table\" />\n" +
            "<p>four <span tal:replace=\"array[0][3]\">a number</span></p>\n" +
            "<p>seven <span tal:replace=\"array[1][2]\">a number</span></p>\n" +
            "<p>eleven <span tal:replace=\"math/add( array[0][3], array[1][2] )\">a number</span></p>\n" +
            "<p>seven <span tal:replace=\"here/getTable()[1][2]\">a number</span></p>" + endTag;
        Map<String, Object> dictionary = new HashMap<String, Object>();
        dictionary.put("math", new MyMath());
        String result = processTemplate(templateText, new MyArrays(), dictionary);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><span/><p>four 4</p><p>seven 7</p><p>eleven 11</p><p>seven 7</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void UseDefaultVariable () {
        String templateText = startTag + "<p>My aunt is named <span tal:replace=\"myaunt|default\">Renee</span></p>\n" +
            "<p>Sometimes <p class=\"sometimes\" tal:attributes=\"class myclass|default\">I feel sad.</p></p>" + endTag;
        String result = processTemplate(templateText, new MyMath(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>My aunt is named Renee</p><p>Sometimes <p class=\"sometimes\">I feel sad.</p></p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void UseNothingVariable () {
        String templateText = startTag + "<p>I have a little doggy named <span tal:replace=\"mydoggy|nothing\">Actually I don't have a doggy</span></p>\n" +
            "<p>Sometimes <p class=\"sometimes\" tal:attributes=\"class myclass|nothing\">I feel happy</p></p>" + endTag;
        String result = processTemplate(templateText, new MyMath(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>I have a little doggy named </p><p>Sometimes <p>I feel happy</p></p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }
}