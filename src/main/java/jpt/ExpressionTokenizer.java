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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * More or less functions like the standard StringTokenizer
 * except that delimiters which are buried inside parentheses
 * or single quotes are skipped.
 *
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
class ExpressionTokenizer {
    String expression;
    Iterator<Integer> iterator;
    int currIndex = 0;
    int delimiterCount = 0;

    ExpressionTokenizer (String expression, char delimiter)
        throws ExpressionSyntaxException {
        this(expression, delimiter, false);
    }

    ExpressionTokenizer (String expression, char delimiter, boolean escape)
        throws ExpressionSyntaxException {
        // Go ahead and find delimiters, if any, at construction time

        List delimiters = new ArrayList(10);

        int parenLevel = 0;
        boolean inQuote = false;
        // Scan for delimiters
        String remainingExpression = expression;
        int length = remainingExpression.length();
        for (int i = 0; i < length; i++) {
            char ch = remainingExpression.charAt(i);

            if (ch == delimiter) {
                // If delimiter is not buried in parentheses or a quote
                if ((parenLevel == 0) && (!inQuote)) {
                    char nextCh = i + 1 < length ? remainingExpression.charAt(i + 1) : '\000';
                    // And if delimiter is not escaped
                    if ((!escape) || (nextCh != delimiter)) {
                        this.delimiterCount += 1;
                        delimiters.add(Integer.valueOf(i));
                    }
                    else {
                        // Somewhat inefficient way to pare the
                        // escaped delimiter down to a single
                        // character without breaking our stride
                        remainingExpression = remainingExpression.substring(0, i + 1) + remainingExpression.substring(i + 2);
                        length--;
                    }

                }

            }
            // increment parenthesis level
            else if (ch == '(') {
                parenLevel++;
            }
            // decrement parenthesis level
            else if (ch == ')') {
                parenLevel--;
                // If unmatched right parenthesis
                if (parenLevel < 0) {
                    throw ((ExpressionSyntaxException) new ExpressionSyntaxException("syntax error: unmatched right parenthesis: " + remainingExpression).setExpression(expression));
                }

            }
            // start or end quote
            else if (ch == '\'') {
                inQuote = !inQuote;
            }

        }
        // If unmatched left parenthesis
        if (parenLevel > 0) {
            throw ((ExpressionSyntaxException) new ExpressionSyntaxException("syntax error: unmatched left parenthesis: " + remainingExpression).setExpression(expression));
        }
        // If runaway quote
        if (inQuote) {
            throw ((ExpressionSyntaxException) new ExpressionSyntaxException("syntax error: runaway quotation: " + remainingExpression).setExpression(expression));
        }

        this.expression = remainingExpression;
        this.iterator = delimiters.iterator();
    }

    boolean hasMoreTokens () {
        return this.currIndex < this.expression.length();
    }

    String nextToken () {
        if (this.iterator.hasNext()) {
            int delim = (this.iterator.next()).intValue();
            String token = this.expression.substring(this.currIndex, delim);
            this.currIndex = (delim + 1);
            this.delimiterCount -= 1;
            return token;
        }
        String token = this.expression.substring(this.currIndex);
        this.currIndex = this.expression.length();
        return token;
    }

    int countTokens () {
        if (hasMoreTokens()) {
            return this.delimiterCount + 1;
        }
        return 0;
    }

    @Override
    public String toString () {
        return "ExpressionTokenizer{" +
            "expression='" + expression + '\'' +
            ", iterator=" + iterator +
            ", currIndex=" + currIndex +
            ", delimiterCount=" + delimiterCount +
            '}';
    }
}
