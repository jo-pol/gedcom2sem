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
package gedcom2sem;

import gedcom2sem.gedsem.Convert;
import gedcom2sem.gedsem.UriFormats;
import gedcom2sem.semweb.Mashup;
import gedcom2sem.semweb.GedKml;
import gedcom2sem.semweb.Select;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.Ignore;
import org.junit.Test;

public class BatchExamples
{
    private static final String TEST = "src/test/resources/";
    private static final String MAIN = "src/main/resources/";
    private static final String QUERY_DIR = "src/main/resources/reports/mashup/";
    private static final String RULES = "src/main/resources/rules/SlowRules.txt";
    private PrintStream savedOut;

    @Ignore // Mashup takes too long to run on a regular basis
    @Test
    public void kennedy() throws Exception
    {
        redirectOut("target/kennedy.ttl");
        Convert.main("-rules", RULES, "-uri", UriFormats.DEFAULT_URI, "-gedcom", TEST + "kennedy.ged");
        restoreOut();

        Select.main("target/kennedy.ttl", "target/mashup.tsv", QUERY_DIR + "mashup.arq");

        // consider MASHUP_TSV changed manually into KENNEDY_TSV (geo name IDs added)
        Mashup.main("target/kennedy.ttl", TEST + "kennedy.tsv", "target/mashup.ttl", UriFormats.DEFAULT_URI, "de|fr");

        Select.main("target/kennedy.ttl", "target/mashup.ttl", "target/places.tsv", QUERY_DIR + "places.arq");
     
        GedKml.main(MAIN+"kml.properties","target/kennedy.ttl", "target/mashup.ttl", "target/places.kml", QUERY_DIR + "places.arq");
    }

    private void redirectOut(final String filename) throws FileNotFoundException
    {
        savedOut = System.out;
        System.setOut(new PrintStream(new FileOutputStream(filename)));
    }

    private void restoreOut()
    {
        System.setOut(savedOut);
    }
}
