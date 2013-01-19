// @formatter:off
/*
 * Copyright 2012, J. Pol
 *
 * This file is part of free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 *
 * This package is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details. A copy of the GNU General Public License is
 * available at <http://www.gnu.org/licenses/>.
 */
// @formatter:on
package gedcom2sem.semweb;

import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class KmlQueryRow
{
    String sosa;
    Float latitude;
    Float longitude;
    final String[] formatArgs;

    KmlQueryRow(final List<String> resultVars, final QuerySolution solution)
    {
        formatArgs = new String[resultVars.size()];
        for (int i = 0; i < resultVars.size(); i++)
        {
            final String columnName = resultVars.get(i);
            final RDFNode columnValue = solution.get(columnName);
            formatArgs[i] = columnValue == null ? "" : columnValue.toString();
        }
        setFields();
    }

    KmlQueryRow(final String[] columns)
    {
        // additional columns are allocated in case the last columns don't have values
        // otherwise the place holders in the templates may appear
        formatArgs = new String[columns.length+10];
        for (int i = 0; i < columns.length; i++)
        {
            formatArgs[i] = columns[i].trim().replaceFirst("^\"", "").replaceAll("\"$", "").trim();
        }
        setFields();
    }

    private void setFields()
    {
        for (int i = 0; i < formatArgs.length; i++)
        {
            if (formatArgs[i]==null)
                formatArgs[i] = "";
        }
        final String leadingDigits = formatArgs[0].replaceAll("[^0-9].*$", "");
        sosa = Integer.toBinaryString(Integer.valueOf(leadingDigits));
        if (formatArgs[1].length() == 0 || formatArgs[2].length() == 0)
            latitude = longitude = null;
        else
        {
            latitude = Float.parseFloat(formatArgs[1]);
            longitude = Float.parseFloat(formatArgs[2]);
        }
    }
}
