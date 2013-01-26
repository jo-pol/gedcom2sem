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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class AbstractQueryTest
{
    static final String GEDCOM = "src/test/resources/kennedy.ged";
    static final String GEDCOM_TTL = "src/test/resources/kennedy.ttl";
    static final String MASHUP_TTL = "src/test/resources/mashup.ttl";
    static final String REPORT_DIR = "src/main/resources/reports";
    static final String REPORT_TXT = "target/report.txt";

    private static final Collection<Object[]> constructorArgs = new ArrayList<Object[]>();
    final Integer expectedNrOfLines;
    final String queryFileName;
    final Boolean mashup;
    private String endPointID;

    private static void addTest(final Boolean mashup, final Integer expectedNrOfLines, final String endPointID, final String queryFileName)
    {
        constructorArgs.add(new Object[] {mashup, expectedNrOfLines, endPointID, queryFileName});
    }

    public AbstractQueryTest(final Boolean mashup, final Integer expectedNrOfLines, final String endPointID, final String queryFileName)
    {
        this.mashup = mashup;
        this.expectedNrOfLines = expectedNrOfLines;
        this.endPointID = endPointID;
        this.queryFileName = REPORT_DIR + "/" + queryFileName;
    }

    @Parameters
    public static Collection<Object[]> getContructorParameters()
    {
        // addTest(false, 1, null,"AgeDiffBetweenSpouses.arq");
        addTest(false, 89,null, "CountEventsPerPlace.arq");
        addTest(false, 7, null,"CountGivnNames.arq");
        addTest(false, 34, null,"SOSA-InbredStatistics.arq");
        addTest(false, 158, null,"SOSA-MultiMedia.arq");
        addTest(true, 12, null,"mashup/classmates.arq");
        addTest(true, 15, "dbp","mashup/dbpediaLanguages.arq");
        addTest(true, 117, "dbp","mashup/dbpediaProperties.arq");
        addTest(true, 225, "dbp","mashup/dbpediaRelatedEntities.arq");
        addTest(true, 31, "gn","mashup/geonamesProperties.arq");
        addTest(true, 9, "gn","mashup/geonamesRelatedEntities.arq");
        addTest(true, 93, null,"mashup/mashup.arq");
        // addTest(true, 1, null,"mashup/MigrationLines.arq");
        return constructorArgs;
    }

    @Before
    public void beNice()
    {
        // too frequent access to a SPARQL end point causes "service not available"
        if (endPointID!=null)
            Nice.sleep(endPointID);
    }

    @After
    public void countLines() throws FileNotFoundException, IOException
    {
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(REPORT_TXT)));
        int nrOfLines = 0;
        while (bufferedReader.readLine() != null)
            nrOfLines++;
        bufferedReader.close();
        assertThat(queryFileName, nrOfLines, is(expectedNrOfLines));
    }
}
