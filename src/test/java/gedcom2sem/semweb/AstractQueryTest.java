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
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class AstractQueryTest
{
    static final String GEDCOM     = "src/test/resources/kennedy.ged";
    static final String GEDCOM_TTL = "src/test/resources/kennedy.ttl";
    static final String MASHUP_TTL = "src/test/resources/mashup.ttl";
    static final String REPORT_DIR = "src/main/resources/reports";
    static final String REPORT_TXT = "target/report.txt";
    final Integer       expectedNrOfLines;
    final String        queryFileName;
    final Boolean       mashup;

    public AstractQueryTest(final Boolean mashup, final Integer expectedNrOfLines, final String queryFileName)
    {
        this.mashup = mashup;
        this.expectedNrOfLines = expectedNrOfLines;
        this.queryFileName = REPORT_DIR + "/" + queryFileName;
    }

    @Parameters
    public static Collection<Object[]> getContructorParameters()
    {
        return Arrays.asList(new Object[][] {//
                // {false, 1, "AgeDiffBetweenSpouses.arq"},//
                        {false, 89, "CountEventsPerPlace.arq"},//
                        {false, 7, "CountGivnNames.arq"},//
                        {false, 34, "SOSA-InbredStatistics.arq"},//
                        {false, 158, "SOSA-MultiMedia.arq"},//
                        {true, 12, "mashup/classmates.arq"},//
                        {true, 15, "mashup/dbpediaLanguages.arq"},//
                        {true, 117, "mashup/dbpediaProperties.arq"},//
                        {true, 225, "mashup/dbpediaRelatedEntities.arq"},//
                        {true, 31, "mashup/geonamesProperties.arq"},//
                        {true, 9, "mashup/geonamesRelatedEntities.arq"},//
                        {true, 93, "mashup/mashup.arq"},//
                // {true, 1, "mashup/MigrationLines.arq"},//
                });
    }

    @Before
    public void beNice()
    {
        if (!mashup)
            return;
        if (queryFileName.contains("dbpedia"))
            Nice.sleep("dbpedia");
        else if (queryFileName.contains("geonames"))
            Nice.sleep("geonames");
        else
            Nice.sleep("unknown");
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
