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

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */

public class ExpressionEvaluationException extends PageTemplateException {
    private static final long serialVersionUID = 656681917355463617L;
    String expression = null;

    public ExpressionEvaluationException (String message) {
        super(message);
    }

    public ExpressionEvaluationException (String message, Throwable cause) {
        super(message, cause);
    }

    public ExpressionEvaluationException (Throwable cause) {
        super(cause);
    }

    ExpressionEvaluationException setExpression (String expression) {
        if (this.expression == null) {
            this.expression = expression;
        }
        return this;
    }

    public String getMessage () {
        // Do not clobber first expression
        if (this.expression != null) {
            return this.expression + ": " + super.getMessage();
        }
        return super.getMessage();
    }

    @Override
    public String toString () {
        return "ExpressionEvaluationException{ message: " + super.getMessage()
            + " expression='" + expression + '\'' + '}';
    }
}