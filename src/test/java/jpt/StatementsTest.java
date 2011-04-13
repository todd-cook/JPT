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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class StatementsTest extends JptTestUtil {

    class DataObject {

        private String goodLooking = "top model";

        public String getGoodLooking () {
            return goodLooking;
        }

        public String getFavoriteColor () {
            return "red";
        }
    }

    @Test
    public void omit () {
        String templateText = startTag + "<p>This should not omit the span tag: <span tal:omit-tag=\"\">contents</span>.</p>" +
            "<p>This should omit the span tag: <span tal:omit-tag=\"here/dumb\"> contents </span>.</p>" +
            "<p>This should not omit the span tag: <span tal:omit-tag=\"here/goodLooking\"> contents</span>.</p>" +
            "<p>This is the equivalent of a replace: <span tal:omit-tag=\"\" tal:content=\"here/favoriteColor\"> contents</span>.</p>" + endTag;
        String result = processTemplate(templateText, new DataObject(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>This should not omit the span tag: contents.</p><p>This should omit the span tag: .</p><p>This should not omit the span tag:  contents.</p><p>This is the equivalent of a replace: red.</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void attributes () {
        String templateText = startTag + "<p>This should generate some <span tal:attributes=\"class 'myclass'; href string:http://captainsaturn.com/${here/favoriteColor};\">attributes</span></p>" +
            "<p>So should <span tal:attributes=\"html:src here/friend/number\">this</span></p>" +
            "<p>This should <span enemy=\"my enemy\" tal:attributes=\"enemy here/enemy\">get rid of my enemy</span></p>" +
            "<p>This should <span tal:attributes=\"noone here/enemy\">have no attributes</span></p>" +
            "<p>This contains <span tal:attributes=\"how-are-you string:I am fine;; How are you?;good-looking here/goodLooking\">a semicolon</span></p>" +
            "<p>A limiting case: <span tal:attributes=\"hello string:;;\">semicolon at end</span></p>" + endTag;
        String result = processTemplate(templateText, new DataObject(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>This should generate some <span class=\"myclass\" href=\"http://captainsaturn.com/red\">attributes</span></p><p>So should </p><p>This should </p><p>This should </p><p>This contains <span how-are-you=\"I am fine; How are you?\" good-looking=\"top model\">a semicolon</span></p><p>A limiting case: <span hello=\";\">semicolon at end</span></p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class DataObject2 {

        private String goodLooking = "top model";
//        private String dumb = "dumb";

//        public String getDumb () {
//            return dumb;
//        }

        public String getGoodLooking () {
            return goodLooking;
        }
    }

    @Test
    public void condition () {
        String templateText = startTag +
            "<p tal:condition=\"here/goodLooking\">This paragraph can stay.</p>" +
            "<p tal:condition=\"here/dumb\">This paragraph should be removed.</p>" +
            "<p>This text should be intact.</p> none of this will print!!@#$! " +
            endTag;
        String result = processTemplate(templateText, new DataObject2(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>This paragraph can stay.</p></html>";
        // TODO correct or sleuth out the reason for this odd behavior
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class AnotherFriend {
        Map<String, String> map = new HashMap<String, String>();

        AnotherFriend () {
            map.put("hello", "greeting");
            map.put("enemy", "alligator");
        }
    }

    class Definitions {
        AnotherFriend friend = new AnotherFriend();

        public AnotherFriend getFriend () {
            return friend;
        }
    }

    @Test
    public void define () {
        String templateText = startTag + "<p><span tal:define=\"howard here/friend/map; animal string:zebra;\" " +
            "tal:attributes=\"number howard/hello\" tal:content=\"string:my $animal is ${howard/enemy}\">variables</span>" +
            "<span tal:define=\"semicolon string:;;; period string:.\" tal:replace=\"string:this is a '${semicolon}' and this is a '${period}'\">punctuation</span></p>" +
            "<p>Do our variables stay in <b tal:content=\"howard/friend\">scope</b>?</p>" + endTag;
        String result = processTemplate(templateText, new Definitions(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p><p>Do our variables stay in ?</p></p></html>";
        // TODO find out what the hell is happening here...
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class DataObject3 {
        List<String> people = Arrays.asList(
            "Chris", "Karen", "Mike", "Marsha", "Christiane");

        public List<String> getPeople () {
            return people;
        }
    }

    @Test
    public void repeat () {
        String templateText = startTag + "Some people<ul><li tal:repeat=\"person here/people\" tal:content=\"person\" tal:attributes=\"length person/length()\">Person</li>" +
            "<li tal:content=\"person\">Somebody</li></ul>" + endTag;
        String result = processTemplate(templateText, new DataObject3(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html>Some people<ul><li length=\"5\">Chris</li><li length=\"5\">Karen</li><li length=\"4\">Mike</li><li length=\"6\">Marsha</li><li length=\"10\">Christiane</li><li></li></ul></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    class NumberData {
        int[][] table = {
            {1, 2, 3, 4},
            {5, 6, 7, 8},
            {9, 10}};

        public int[][] getTable () {
            return table;
        }

        public List<Integer> getNumbers () {
            List<Integer> numbers = new ArrayList<Integer>(10);
            for (int i = 10; i > 0; i--) {
                numbers.add(i);
            }
            return numbers;
        }
    }

    @Test
    public void gridTable () {
        String templateText = startTag + "<table><tr tal:repeat=\"y here/table\">" +
            "<td tal:repeat=\"x y\" tal:content=\"x\">a number</td></tr></table>" + endTag;
        String result = processTemplate(templateText, new NumberData(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><table><tr><td>1</td><td>2</td><td>3</td><td>4</td></tr><tr><td>5</td><td>6</td><td>7</td><td>8</td></tr><tr><td>9</td><td>10</td></tr></table></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void repeatNumberTest () {
        String templateText = startTag + "<p tal:define=\"numbers here/numbers\"><table><tr><th>Value</th><th>Index</th><th>Number</th><th>Even index</th><th>Odd index</th><th>Start</th><th>End</th><th>Length</th><th>Letter</th><th>Capital Letter</th><th>Roman</th><th>Capital Roman</th></tr>" +
            "<tr tal:repeat=\"number numbers\"><td tal:content=\"number\">value</td><td tal:content=\"repeat/number/index\">index</td><td tal:content=\"repeat/number/number\">number</td><td tal:content=\"repeat/number/even\">even</td><td tal:content=\"repeat/number/odd\">odd</td><td tal:content=\"repeat/number/start\">start</td><td tal:content=\"repeat/number/end\">end</td><td tal:content=\"repeat/number/length\">length</td><td tal:content=\"repeat/number/letter\">letter</td><td tal:content=\"repeat/number/capitalLetter\">capital letter</td><td tal:content=\"repeat/number/roman\">roman</td><td tal:content=\"repeat/number/capitalRoman\">capitalRoman</td></tr></table></p>" + endTag;
        String result = processTemplate(templateText, new NumberData(), null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p><table><tr><th>Value</th><th>Index</th><th>Number</th><th>Even index</th><th>Odd index</th><th>Start</th><th>End</th><th>Length</th><th>Letter</th><th>Capital Letter</th><th>Roman</th><th>Capital Roman</th></tr>" +
            "<tr><td>10</td><td>0</td><td>1</td><td>true</td><td>false</td><td>true</td><td>false</td><td>10</td><td>a</td><td>A</td><td>i</td><td>I</td></tr><tr><td>9</td><td>1</td><td>2</td><td>false</td><td>true</td><td>false</td><td>false</td><td>10</td><td>b</td><td>B</td><td>ii</td><td>II</td></tr><tr><td>8</td><td>2</td><td>3</td><td>true</td><td>false</td><td>false</td><td>false</td><td>10</td><td>c</td><td>C</td><td>iii</td><td>III</td></tr><tr><td>7</td><td>3</td><td>4</td><td>false</td><td>true</td><td>false</td><td>false</td><td>10</td><td>d</td><td>D</td><td>iv</td><td>IV</td></tr><tr><td>6</td><td>4</td><td>5</td><td>true</td><td>false</td><td>false</td><td>false</td><td>10</td><td>e</td><td>E</td><td>v</td><td>V</td></tr><tr><td>5</td><td>5</td><td>6</td><td>false</td><td>true</td><td>false</td><td>false</td><td>10</td><td>f</td><td>F</td><td>vi</td><td>VI</td></tr><tr><td>4</td><td>6</td><td>7</td><td>true</td><td>false</td><td>false</td><td>false</td><td>10</td><td>g</td><td>G</td><td>vii</td><td>VII</td></tr><tr><td>3</td><td>7</td><td>8</td><td>false</td><td>true</td><td>false</td><td>false</td><td>10</td><td>h</td><td>H</td><td>viii</td><td>VIII</td></tr><tr><td>2</td><td>8</td><td>9</td><td>true</td><td>false</td><td>false</td><td>false</td><td>10</td><td>i</td><td>I</td><td>ix</td><td>IX</td></tr><tr><td>1</td><td>9</td><td>10</td><td>false</td><td>true</td><td>false</td><td>true</td><td>10</td><td>j</td><td>J</td><td>x</td><td>X</td></tr></table></p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }

    @Test
    public void evaluate () {
        String templateText = startTag + "<p tal:evaluate=\"java: joemama = 1 + 3\" tal:content=\"joemama\">Joe Mama</p>" + endTag;
        String result = processTemplate(templateText, null, null);
        assertTrue(!templateText.equals(result));
        showTransformation(templateText, result);
        String trueResult = "<html><p>4</p></html>";
        assertTrue(trueResult.equals(removeAllBreaks(result)));
    }
}
