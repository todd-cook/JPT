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
import jpt.util.TalesPrefix;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
class Expression {
    private static final Pattern numericPattern = Pattern.compile("[-]?(\\d*?[.])?\\d+[fFdDlL]?");
    private static final int STATE_SCANNING = 0;
    private static final int STATE_AT_DOLLAR = 1;
    private static final int STATE_IN_EXPRESSION = 2;
    private static final int STATE_IN_BRACKETED_EXPRESSION = 3;
    private static final int SCANNING = 0;
    private static final int IN_PAREN = 1;
    private static final int IN_QUOTE = 2;
    private static final Object[] emptyArray = new Object[0];

    private static final int maxIntegerLength = String.valueOf(2147483647).length();
    private static final Long maxIntegerAsLong = new Long(String.valueOf(2147483647));
    private static final Pattern naturalIntegerPattern = Pattern.compile("\\d{1," + maxIntegerLength + "}");

    static final Object evaluate (String expression, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        try {
            Object result;
            if (expression.startsWith(TalesPrefix.string + ":")) {
                result = evaluateString(expression, expressionEvaluator);
            }
            else {
                if (expression.startsWith(TalesPrefix.exists + ":")) {
                    result = evaluateExists(expression, expressionEvaluator);
                }
                else {
                    if (expression.startsWith(TalesPrefix.not + ":")) {
                        result = evaluateNot(expression, expressionEvaluator);
                    }
                    else {
                        if (expression.startsWith(TalesPrefix.java + ":")) {
                            result = evaluateJava(expression, expressionEvaluator);
                        }
                        else {
                            result = evaluatePath(expression, expressionEvaluator);
                        }
                    }
                }
            }
            return result;
        }
        catch (ExpressionEvaluationException e) {
            e.setExpression(expression);
            throw e;
        }

    }

    static final boolean evaluateBoolean (String expression, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        Object result = evaluate(expression, expressionEvaluator);
        if (result == null) {
            return false;
        }
        if ((result instanceof Boolean)) {
            return (Boolean) result;
        }
        if ((result instanceof String)) {
            return ((String) result).length() != 0;
        }
        if ((result instanceof Long)) {
            return (Long) result != 0L;
        }
        if ((result instanceof Integer)) {
            return (Integer) result != 0;
        }
        if ((result instanceof Double)) {
            return (Double) result != 0.0D;
        }
        if ((result instanceof Float)) {
            return (Float) result != 0.0D;
        }
        if ((result instanceof Collection)) {
            return ((Collection) result).size() != 0;
        }
        if ((result instanceof Map)) {
            return ((Map) result).size() != 0;
        }
        return true;
    }

    static final String evaluateString (String stringExpression,
                                        ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        if (stringExpression.length() == (TalesPrefix.string + ":").length()) {
            return "";
        }

        StringBuffer result = new StringBuffer(stringExpression.length() * 2);
        String expression = stringExpression.substring((TalesPrefix.string + ":").length());

        StringBuffer subexpression = new StringBuffer(20);
        int state = 0;
        int length = expression.length();
        for (int i = 0; i < length; i++) {
            char ch = expression.charAt(i);

            switch (state) {
                case 0:
                    if (ch == '$') {
                        state = 1;
                    }
                    else {
                        result.append(ch);
                    }
                    break;
                case 1:
                    if (ch == '$') {
                        result.append('$');
                        state = 0;
                    }
                    else if (ch == '{') {
                        subexpression.setLength(0);
                        state = 3;
                    }
                    else {
                        subexpression.setLength(0);
                        subexpression.append(ch);
                        state = 2;
                    }
                    break;
                case 2:
                case 3:
                    if (((state == 3) && (ch == '}')) || ((state == 2) && (Character.isWhitespace(ch)))) {
                        result.append(String.valueOf(evaluate(subexpression.toString(), expressionEvaluator)));
                        if (state == 2) {
                            result.append(ch);
                        }
                        state = 0;
                    }
                    else {
                        subexpression.append(ch);
                    }
            }

        }

        if (state == 3) {
            throw ((ExpressionSyntaxException) new ExpressionSyntaxException("unclosed left curly brace").setExpression(expression));
        }

        if (state == 2) {
            result.append(evaluate(subexpression.toString(), expressionEvaluator));
        }

        return result.toString();
    }

    static final Boolean evaluateNot (String expression, ExpressionEvaluator expressionEvaluator) throws ExpressionEvaluationException {
        return Boolean.valueOf(!evaluateBoolean(expression.substring((TalesPrefix.not + ":").length()), expressionEvaluator));
    }

    static final Boolean evaluateExists (String expression, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        boolean exists = false;
        try {
            exists = evaluate(expression.substring((TalesPrefix.exists + ":").length()), expressionEvaluator) != null;
        }
        catch (NoSuchPathException e) {
        }
        return Boolean.valueOf(exists);
    }

    static final Object evaluateJava (String expression, ExpressionEvaluator expressionEvaluator) throws ExpressionEvaluationException {
        try {
            return expressionEvaluator.eval(expression.substring((TalesPrefix.java + ":").length()));
        }
        catch (EvalException e) {
            throw new ExpressionEvaluationException(e);
        }
    }

    /**
     * If you want to see the test cases break in unusual ways,
     * change this to return Boolean.valueOf(expression)
     *
     * @param expression
     * @return
     */
    static final Boolean booleanLiteral (String expression) {
        if ("true".equalsIgnoreCase(expression)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(expression)) {
            return Boolean.FALSE;
        }
        return null;
    }

    static final String stringLiteral (String expression) {
        if ((expression.startsWith("'")) && (expression.endsWith("'"))) {
            return expression.substring(1, expression.length() - 1);
        }
        return null;
    }

    static final Number numericLiteral (String expression) {
        Matcher isNumberMatcher = numericPattern.matcher(expression);
        boolean isNumber = isNumberMatcher.matches();
        boolean hasDecimal = (isNumber) && (isNumberMatcher.group(1) != null);

        char lastChar = expression.charAt(expression.length() - 1);

        boolean isWholeNumber = (isNumber) && (!hasDecimal);
        boolean isLong = ((isWholeNumber) && (lastChar == 'l')) || (lastChar == 'L');

        boolean isDecimal = (isNumber) && (hasDecimal);
        boolean isDouble = ((isDecimal) && (lastChar == 'd')) || (lastChar == 'D');
        boolean isFloat = ((isDecimal) && (lastChar == 'f')) || (lastChar == 'F');
        try {
            if (isLong) {
                return new Long(expression.substring(0, expression.length() - 1));
            }
            if (isWholeNumber) {
                return new Integer(expression);
            }
            if (isDouble) {
                return new Double(expression.substring(0, expression.length() - 1));
            }
            if (isFloat) {
                return new Float(expression.substring(0, expression.length() - 1));
            }
            if (isDecimal) {
                return new Float(expression);
            }
        }
        catch (NumberFormatException e) {
        }
        return null;
    }

    static final Object evaluatePath (String pathExpression, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        if (pathExpression.trim().length() == 0) {
            return "";
        }
        String expression;
        if (pathExpression.startsWith(TalesPrefix.path + ":")) {
            expression = pathExpression.substring((TalesPrefix.path + ":").length());
        }
        else {
            expression = pathExpression;

        }

        ExpressionTokenizer segments = new ExpressionTokenizer(expression, '|');
        if (segments.countTokens() == 1) {
            return evaluatePathSegment(expression, expressionEvaluator);
        }

        NoSuchPathException exception = null;
        Object result = null;
        while (segments.hasMoreTokens()) {
            try {
                String segment = segments.nextToken().trim();
                exception = null;
                result = evaluate(segment, expressionEvaluator);
                if (result != null) {
                    return result;
                }
            }
            catch (NoSuchPathException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
        return null;
    }

    static final Object evaluatePathSegment (String expression, ExpressionEvaluator expressionEvaluator)
        throws NoSuchPathException, ExpressionSyntaxException, ExpressionEvaluationException {
        if (expression.length() == 0) {
            return "";
        }

        ExpressionTokenizer path = new ExpressionTokenizer(expression, '/');
        String token = path.nextToken().trim();
        Object result = evaluateFirstPathToken(token, expressionEvaluator);

        while (path.hasMoreTokens()) {
            if (result == null) {
                throw ((NoSuchPathException) new NoSuchPathException(token + " in '" + expression + "' is null").setExpression(expression));
            }

            token = path.nextToken().trim();
            result = evaluateNextPathToken(result, token, expressionEvaluator);
        }

        return result;
    }

    private static final Object evaluateFirstPathToken (String token, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        String arrayAccessor = null;
        int bracket = findArrayAccessor(token);
        if (bracket != -1) {
            arrayAccessor = token.substring(bracket).trim();
            token = token.substring(0, bracket).trim();
        }

        Object result = stringLiteral(token);

        if (result == null) {
            result = numericLiteral(token);

            if (result == null) {
                result = booleanLiteral(token);

                if (result == null) {
                    try {
                        if (token.endsWith(".class")) {
                            token = token.substring(0, token.length() - ".class".length());
                            result = Class.forName(token);
                        }
                    }
                    catch (ClassNotFoundException ee) {
                    }
                    if (result == null) {
                        try {
                            result = expressionEvaluator.get(token);
                        }
                        catch (EvalException eee) {
                            throw new ExpressionEvaluationException(eee);
                        }
                    }
                }
            }

        }

        if (arrayAccessor != null) {
            result = evaluateArrayAccess(token, result, arrayAccessor, expressionEvaluator);
        }

        result = handleScript(result, expressionEvaluator);

        return result;
    }

    private static Object handleScript (Object object, ExpressionEvaluator expressionEvaluator) throws ExpressionEvaluationException {
        if ((object instanceof ExpressionScript)) {
            String script = ((ExpressionScript) object).getScript();
            try {
                object = expressionEvaluator.eval(script);
            }
            catch (EvalException e) {
                throw new ExpressionEvaluationException("Problem evaluating expression in script " + script, e);
            }
        }

        return object;
    }

    private static final Object evaluateNextPathToken (Object parent, String token, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        String arrayAccessor = null;
        int bracket = findArrayAccessor(token);
        if (bracket != -1) {
            arrayAccessor = token.substring(bracket).trim();
            token = token.substring(0, bracket).trim();
        }

        Object result = null;

        if (token.startsWith("?")) {
            String indirectToken = String.valueOf(evaluateFirstPathToken(token.substring(1), expressionEvaluator));

            result = evaluateNextPathToken(parent, indirectToken, expressionEvaluator);
        }
        else {
            int leftParen = token.indexOf("(");
            if (leftParen != -1) {
                if (!token.endsWith(")")) {
                    throw new ExpressionEvaluationException("syntax error: bad method call: " + token);
                }
                String methodName = token.substring(0, leftParen).trim();
                String arguments = token.substring(leftParen + 1, token.length() - 1);
                result = evaluateMethodCall(parent, methodName, arguments, expressionEvaluator);
            }
            else {
                result = getProperty(parent, token);
            }
        }

        if (arrayAccessor != null) {
            result = evaluateArrayAccess(token, result, arrayAccessor, expressionEvaluator);
        }

        result = handleScript(result, expressionEvaluator);

        return result;
    }

    private static final int findArrayAccessor (String token) {
        int length = token.length();
        int state = 0;
        int parenDepth = 0;
        for (int i = 0; i < length; i++) {
            char ch = token.charAt(i);
            switch (state) {
                case 1:
                    if (ch == ')') {
                        parenDepth--;
                        if (parenDepth != 0) {
                            continue;
                        }
                        state = 0;
                    }
                    else {
                        if (ch != '(') {
                            continue;
                        }
                        parenDepth++;
                    }
                    break;
                case 2:
                    if (ch != '\'') {
                        continue;
                    }
                    state = 0;
                    break;
                case 0:
                    if (ch == '\'') {
                        state = 2;
                    }
                    else if (ch == '(') {
                        parenDepth++;
                        state = 1;
                    }
                    else {
                        if (ch != '[') {
                            continue;
                        }
                        return i;
                    }
            }
        }
        return -1;
    }

    private static final Object evaluateArrayAccess (String token, Object result, String accessor, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        try {
            int close = accessor.indexOf(93);
            if ((accessor.charAt(0) != '[') || (close == -1)) {
                throw new ExpressionEvaluationException("bad array accessor for " + token + ": " + accessor);
            }

            if (!result.getClass().isArray()) {
                throw new ExpressionEvaluationException(token + " is not an array: " + result.getClass());
            }

            if (result.getClass().getComponentType().isPrimitive()) {
                result = convertPrimitiveArray(result);
            }
            Object[] array = (Object[]) (Object[]) result;
            Object index = evaluate(accessor.substring(1, close), expressionEvaluator);
            if (!(index instanceof Integer)) {
                throw new ExpressionEvaluationException("array index must be an integer");
            }
            result = array[((Integer) index).intValue()];

            close++;
            if (accessor.length() > close) {
                token = token + accessor.substring(0, close);
                String afterClose = accessor.substring(close);
                result = evaluateArrayAccess(token, result, afterClose, expressionEvaluator);
            }
            return result;
        }
        catch (ArrayIndexOutOfBoundsException e) {

            throw new ExpressionEvaluationException(e);
        }
    }

    static final Object[] convertPrimitiveArray (Object o) {
        Object[] newArray = null;
        if ((o instanceof int[])) {
            int[] oldArray = (int[]) (int[]) o;
            newArray = new Integer[oldArray.length];
            for (int i = 0; i < oldArray.length; i++) {
                newArray[i] = Integer.valueOf(oldArray[i]);
            }
        }
        else if ((o instanceof long[])) {
            long[] oldArray = (long[]) (long[]) o;
            newArray = new Long[oldArray.length];
            for (int i = 0; i < oldArray.length; i++) {
                newArray[i] = Long.valueOf(oldArray[i]);
            }
        }
        else if ((o instanceof boolean[])) {
            boolean[] oldArray = (boolean[]) (boolean[]) o;
            newArray = new Boolean[oldArray.length];
            for (int i = 0; i < oldArray.length; i++) {
                newArray[i] = Boolean.valueOf(oldArray[i]);
            }
        }
        else if ((o instanceof char[])) {
            char[] oldArray = (char[]) (char[]) o;
            newArray = new Character[oldArray.length];
            for (int i = 0; i < oldArray.length; i++) {
                newArray[i] = Character.valueOf(oldArray[i]);
            }
        }
        else if ((o instanceof byte[])) {
            byte[] oldArray = (byte[]) (byte[]) o;
            newArray = new Byte[oldArray.length];
            for (int i = 0; i < oldArray.length; i++) {
                newArray[i] = Byte.valueOf(oldArray[i]);
            }
        }
        else if ((o instanceof float[])) {
            float[] oldArray = (float[]) (float[]) o;
            newArray = new Float[oldArray.length];
            for (int i = 0; i < oldArray.length; i++) {
                newArray[i] = Float.valueOf(oldArray[i]);
            }
        }
        else if ((o instanceof double[])) {
            double[] oldArray = (double[]) (double[]) o;
            newArray = new Double[oldArray.length];
            for (int i = 0; i < oldArray.length; i++) {
                newArray[i] = Double.valueOf(oldArray[i]);
            }
        }
        else if ((o instanceof short[])) {
            short[] oldArray = (short[]) (short[]) o;
            newArray = new Short[oldArray.length];
            for (int i = 0; i < oldArray.length; i++) {
                newArray[i] = Short.valueOf(oldArray[i]);
            }
        }
        return newArray;
    }

    private static final Object evaluateMethodCall (Object parent, String methodName, String argumentString, ExpressionEvaluator expressionEvaluator)
        throws ExpressionEvaluationException {
        StringBuffer errorMessage = null;
        try {
            Object object;
            Class clazz;
            if ((parent instanceof StaticCall)) {
                object = null;
                clazz = ((StaticCall) parent).clazz;
            }
            else {
                object = parent;
                clazz = parent.getClass();
            }

            ExpressionTokenizer argumentTokens = new ExpressionTokenizer(argumentString, ',');
            Object[] arguments = new Object[argumentTokens.countTokens()];
            for (int i = 0; i < arguments.length; i++) {
                String argumentExpression = argumentTokens.nextToken().trim();
                arguments[i] = evaluate(argumentExpression, expressionEvaluator);
            }

            Method[] methods = clazz.getMethods();
            Method method = null;
            for (int i = 0; i < methods.length; i++) {
                if (!methods[i].getName().equals(methodName)) {
                    continue;
                }
                Class[] parms = methods[i].getParameterTypes();
                if (parms.length == arguments.length) {
                    boolean match = true;
                    for (int j = 0; (j < parms.length) && (match); j++) {
                        if ((arguments[j] == null) || (primitiveToClass(parms[j]).isAssignableFrom(arguments[j].getClass()))) {
                            continue;
                        }
                        match = false;
                    }

                    if (match) {
                        method = methods[i];
                        break;
                    }
                }
            }

            if (method != null) {
                if (!method.isAccessible()) {
                    try {
                        method.setAccessible(true);
                    }
                    catch (SecurityException e) {
                    }
                }
                return method.invoke(object, arguments);
            }

            errorMessage = new StringBuffer(100);
            errorMessage.append("no such method: ");
            errorMessage.append(clazz.getName());
            errorMessage.append(".");
            errorMessage.append(methodName);
            errorMessage.append("(");
            for (int i = 0; i < arguments.length; i++) {
                errorMessage.append(arguments[i] == null ? "<null>" : arguments[i].getClass().getName());

                if (i < arguments.length - 1) {
                    errorMessage.append(",");
                }
            }
            errorMessage.append(")");
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExpressionEvaluationException(e);
        }

        throw new ExpressionEvaluationException(errorMessage.toString());
    }

    static final Class<?> primitiveToClass (Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (Boolean.TYPE == clazz) {
                return Boolean.class;
            }
            if (Character.TYPE == clazz) {
                return Character.class;
            }
            if (Byte.TYPE == clazz) {
                return Byte.class;
            }
            if (Short.TYPE == clazz) {
                return Short.class;
            }
            if (Integer.TYPE == clazz) {
                return Integer.class;
            }
            if (Long.TYPE == clazz) {
                return Long.class;
            }
            if (Float.TYPE == clazz) {
                return Float.class;
            }
            if (Double.TYPE == clazz) {
                return Double.class;
            }
        }
        return clazz;
    }

    private static final boolean isNaturalInteger (String expression) {
        if (!naturalIntegerPattern.matcher(expression).matches()) {
            return false;
        }

        if (expression.length() != maxIntegerLength) {
            return true;
        }

        Long exprLong = new Long(expression);
        return exprLong.compareTo(maxIntegerAsLong) <= 0;
    }

    static final Object getProperty (Object object, String name)
        throws ExpressionEvaluationException {
        boolean map = false;

        if (object == null) {
            return null;
        }
        try {
            if ((object instanceof Map)) {
                map = true;
                Object result = ((Map) object).get(name);
                if (result != null) {
                    return result;
                }

            }

            if (((object instanceof List)) &&
                (isNaturalInteger(name))) {
                Integer index = new Integer(name);
                List listObject = (List) object;
                if (listObject.size() > index.intValue()) {
                    Object result = listObject.get(index.intValue());
                    if (object != null) {
                        return result;
                    }

                }

            }

            if ((object.getClass().isArray()) &&
                (isNaturalInteger(name))) {
                Integer index = new Integer(name);
                int size = Array.getLength(object);
                if (size > index.intValue()) {
                    Object result = Array.get(object, index.intValue());
                    if (object != null) {
                        return result;
                    }

                }

            }

            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < properties.length; i++) {
                if (properties[i].getName().equals(name)) {
                    Method reader = properties[i].getReadMethod();
                    if (reader == null) {
                        throw new ExpressionEvaluationException("property '" + name + "' of " + object.getClass().getName() + " can't be read");
                    }

                    return reader.invoke(object, emptyArray);
                }
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new ExpressionEvaluationException(e);
        }

        if (!map) {
            throw new ExpressionEvaluationException("no such property '" + name + "' of " + object.getClass().getName());
        }

        return null;
    }

}

class StaticCall {
    Class clazz;

    StaticCall (Class clazz) {
        this.clazz = clazz;
    }
}
