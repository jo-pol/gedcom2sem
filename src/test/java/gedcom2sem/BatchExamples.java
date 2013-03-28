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
import gedcom2sem.gedsem.Transform;
import gedcom2sem.semweb.KmlGenerator;
import gedcom2sem.semweb.Select;

import org.junit.Test;

public class BatchExamples
{
    private static final String TEST = "src/test/resources/";
    private static final String MAIN = "src/main/resources/";
    private static final String QUERY_DIR = "src/main/resources/reports/";
    private static final String RULES = "src/main/resources/rules/";
    
    @Test
    public void convert() throws Exception
    {
        Convert.main(RULES+"basicRules.txt", RULES+"additionalRules.txt", TEST + "kennedy.ged", "target/kennedy.ttl");
    }

    @Test
    public void preparePublication() throws Exception
    {
        Transform.main(RULES+"foafRules.txt", TEST + "kennedy.ged", "target/kennedyFoaf.ttl");
    }

    @Test
    public void convertFoaf() throws Exception
    {
        Convert.main(RULES+"foafRules.txt", TEST + "kennedy.ged", "target/kennedyFoaf2.ttl");
    }

    @Test
    public void prepareMashup() throws Exception
    {
        Select.main(TEST+"kennedy.ttl", TEST+"geoMashup.ttl", TEST+"geoNamesCache.ttl", "target/mashup.tsv", QUERY_DIR + "mashup/mashup.arq");
    }

    @Test
    public void prepareMashupWithFolder() throws Exception
    {
        Select.main(TEST, "target/mashup2.tsv", QUERY_DIR + "mashup/mashup.arq");
    }

    @Test
    public void toHtml() throws Exception
    {
        Select.main(TEST + "kennedy.ttl", MAIN + "result-to-html.xsl", "target/report.html", QUERY_DIR + "CountEventsPerPlace.arq");
    }

    @Test
    public void migrations() throws Exception
    {
        KmlGenerator.main(MAIN + "kml-by-birth.properties", TEST + "kennedy.ttl", TEST + "geoMashup.ttl", TEST + "geoNamesCache.ttl", QUERY_DIR + "mashup/places-by-birth.arq", "target/places1.kml");
    }

    @Test
    public void migrationsWithFolder() throws Exception
    {
        KmlGenerator.main(MAIN + "kml-by-birth.properties", TEST , QUERY_DIR + "mashup/places-by-birth.arq", "target/places2.kml");
    }
    
    @Test
    public void migrationsInTwoSteps() throws Exception
    {
        Select.main(TEST, "target/places.tsv", QUERY_DIR + "mashup/places-by-birth.arq");
        KmlGenerator.main(MAIN + "kml-by-birth.properties", "target/places.tsv", "target/places3.kml");
    }
}
