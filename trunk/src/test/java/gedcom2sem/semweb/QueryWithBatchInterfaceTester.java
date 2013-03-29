// @formatter:off
/*
 * Copyright 2013, J. Pol
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

import org.junit.Test;

public class QueryWithBatchInterfaceTester extends AbstractQueryTest
// name does neither start nor end with test so maven can build a jar if web-resources don't cooperate
{

    public QueryWithBatchInterfaceTester(final Boolean mashup, final Integer expectedNrOfLines, final String endPointID, final String queryFileName)
    {
        super(mashup, expectedNrOfLines, endPointID, queryFileName);
    }

    @Test
    public void run() throws Exception
    {
        String qualifiedQueryFileName = queryFileName;
        if (mashup)
            Select.main(GEDCOM_TTL, MASHUP_TTL, CACHE_TTL, qualifiedQueryFileName, REPORT_TXT);
        else
            Select.main(GEDCOM_TTL, qualifiedQueryFileName, REPORT_TXT);
    }
}
