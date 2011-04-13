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

import java.io.IOException;
import java.io.Reader;

/**
 * Expression script data structure class
 *
 * @author Chris Rossi
 * @author Todd Cook
 * @version Revision: 1.7
 */
public class ExpressionScript {

    private String script;
    private static final int BUFFER_SIZE = 16384;

    public ExpressionScript (Reader reader)
        throws IOException {
        StringBuffer result = new StringBuffer();
        char[] buf = new char[BUFFER_SIZE];
        int count;
        while ((count = reader.read(buf)) != -1) {
            result.append(buf, 0, count);
        }
        this.script = result.toString();
    }

    public String getScript () {
        return this.script;
    }

    @Override
    public String toString () {
        return "ExpressionScript{" +
            "script='" + script + '\'' +
            '}';
    }
}
