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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class for looping expressions, and also for incrementing the numbering;
 * NOTE: For the Roman Numeral index, the current limit is 4,000
 *
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class Loop {

    boolean once = false;
    String variableName;
    Object prevValue = null;
    Iterator iterator;
    int index = -1;
    int length = -1;
    Loop loopParent = null;

    Loop (String expression, ExpressionEvaluator expressionEvaluator) throws PageTemplateException {
        if (expression == null) {
            variableName = null;
            iterator = null;
        }
        else {
            expression = expression.trim();
            int space = expression.indexOf(' ');
            if (space == -1) {
                throw new ExpressionSyntaxException("bad repeat expression: " + expression);
            }
            this.variableName = expression.substring(0, space);
            try {
                this.prevValue = expressionEvaluator.get(this.variableName);
            }
            catch (EvalException e) {
                throw new PageTemplateException(e);
            }

            String loopExpression = expression.substring(space + 1);
            Object loop = Expression.evaluate(loopExpression, expressionEvaluator);
            if (loop instanceof Iterator) {
                this.iterator = (Iterator) loop;
            }
            else if (loop instanceof Collection) {
                this.iterator = ((Collection) loop).iterator();
                this.length = ((Collection) loop).size();
            }
            else if (loop.getClass().isArray()) {
                if (loop.getClass().getComponentType().isPrimitive()) {
                    loop = Expression.convertPrimitiveArray(loop);
                }
                this.iterator = Arrays.asList((Object[]) loop).iterator();
                this.length = ((Object[]) loop).length;
            }
            else {
                throw new ClassCastException
                    ("result of repeat expression must evaluate to an array, java.util.Iterator " +
                         "or java.util.Collection: " +
                         expression + ": evaluates to " + loop.getClass().getName());
            }
            addToRepeat(expressionEvaluator);
        }
    }

    void addToRepeat (ExpressionEvaluator expressionEvaluator) throws PageTemplateException {
        try {
            // Add to 'repeat' map
            Map repeat = (Map) expressionEvaluator.get("repeat");
            if (repeat == null) {
                repeat = new TreeMap();
                expressionEvaluator.set("repeat", repeat);
            }
            this.loopParent = (Loop) repeat.put(this.variableName, this);
        }
        catch (EvalException e) {
            throw new PageTemplateException(e);
        }
    }

    void removeFromRepeat (ExpressionEvaluator expressionEvaluator) throws PageTemplateException {
        try {
            Map repeat = (Map) expressionEvaluator.get("repeat");
            if (this.loopParent == null) {
                repeat.remove(this.variableName);
            }
            else {
                repeat.put(this.variableName, this.loopParent);
                this.loopParent = null;
            }
            if (repeat.size() == 0) {
                expressionEvaluator.unset("repeat");
            }
        }
        catch (EvalException e) {
            throw new PageTemplateException(e);
        }
    }

    boolean repeat (ExpressionEvaluator expressionEvaluator) throws PageTemplateException {
        try {
            if (iterator == null) {
                if (!this.once) {
                    this.once = true;
                    return true;
                }
                return false;
            }
            else if (iterator.hasNext()) {
                index++;
                expressionEvaluator.set(variableName, iterator.next());
                return true;
            }
            else {
                if (this.prevValue == null) {
                    expressionEvaluator.unset(variableName);
                }
                else {
                    expressionEvaluator.set(variableName, prevValue);
                }
                removeFromRepeat(expressionEvaluator);
                return false;
            }
        }
        catch (EvalException e) {
            throw new PageTemplateException(e);
        }
    }

    public int getIndex () {
        return index;
    }

    public int getNumber () {
        return index + 1;
    }

    public boolean isEven () {
        return index % 2 == 0;
    }

    public boolean isOdd () {
        return index % 2 != 0;
    }

    public boolean isStart () {
        return index == 0;
    }

    public boolean isEnd () {
        return !iterator.hasNext();
    }

    /**
     * will be undefined (-1) if expression evaluates to Iterator
     */
    public int getLength () {
        return length;
    }

    public String getLetter () {
        return formatLetter(index, 'a');
    }

    public String getCapitalLetter () {
        return formatLetter(index, 'A');
    }

    static String formatLetter (int n) {
        return formatLetter(n - 1, 'a');
    }

    static String formatCapitalLetter (int n) {
        return formatLetter(n - 1, 'A');
    }

    private static String formatLetter (int index, char start) {
        StringBuffer buffer = new StringBuffer(2);
        int digit = index % 26;
        buffer.append((char) (start + digit));
        while (index > 25) {
            index /= 26;
            digit = (index - 1) % 26;
            buffer.append((char) (start + digit));
        }
        return buffer.reverse().toString();
    }

    public String getRoman () {
        return formatRoman(index + 1, 0);
    }

    public String getCapitalRoman () {
        return formatRoman(index + 1, 1);
    }

    static String formatRoman (int n) {
        return formatRoman(n, 0);
    }

    static String formatCapitalRoman (int n) {
        return formatRoman(n, 1);
    }

    /**
     * @param n
     * @param capital
     * @return
     */
    static String formatRoman (int n, int capital) {
        // Can't represent any number 4000 or greater
        if (n >= 4000) {
            return "<overflow>";
        }

        StringBuffer buf = new StringBuffer(12);
        for (int decade = 0; n != 0; decade++) {
            int digit = n % 10;
            if (digit > 0) {
                digit--;
                buf.append(roman[decade][digit][capital]);
            }
            n /= 10;
        }

        buf.reverse();
        return buf.toString();
    }

    static final String[][][] roman = {
        /* One's place */
        {
            {"i", "I"},
            {"ii", "II"},
            {"iii", "III"},
            {"vi", "VI"},
            {"v", "V"},
            {"iv", "IV"},
            {"iiv", "IIV"},
            {"iiiv", "IIIV"},
            {"xi", "XI"},
        },

        /* 10's place */
        {
            {"x", "X"},
            {"xx", "XX"},
            {"xxx", "XXX"},
            {"lx", "LX"},
            {"l", "L"},
            {"xl", "XL"},
            {"xxl", "XXL"},
            {"xxxl", "XXXL"},
            {"cx", "CX"},
        },

        /* 100's place */
        {
            {"c", "C"},
            {"cc", "CC"},
            {"ccc", "CCC"},
            {"dc", "DC"},
            {"d", "D"},
            {"cd", "CD"},
            {"ccd", "CCD"},
            {"cccd", "CCCD"},
            {"mc", "MC"},
        },

        /* 1000's place */
        {
            {"m", "M"},
            {"mm", "MM"},
            {"mmm", "MMM"}
        }
    };

    @Override
    public String toString () {
        return "Loop{" +
            "variableName='" + variableName + '\'' +
            '}';
    }
}