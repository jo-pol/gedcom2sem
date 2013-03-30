// @formatter:off
/*
 * Copyright 2013, J. Pol This file is part of free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free Software Foundation. This
 * package is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details. A copy of the GNU General Public License is available at
 * <http://www.gnu.org/licenses/>.
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
    public void convertTTL() throws Exception
    {
        Convert.main(RULES + "basic.rules", RULES + "additional.rules", //
                MAIN + "prefixes.ttl", TEST + "geoMashup.rules", //
                TEST + "kennedy.ged", "target/kennedy.ttl");
    }

    @Test
    public void transform() throws Exception
    {
        Transform.main(MAIN + "prefixes.ttl", TEST + "primaryTopicOf.rules", //
                MAIN + "rules/foaf.rules", MAIN + "rules/child.rules",//
                MAIN + "rules/birth.rules", MAIN + "rules/marriage.rules", //
                MAIN + "rules/publisher.rules", MAIN + "rules/modified.rules", //
                TEST + "geoMashup.rules", TEST + "kennedy.ged", "target/kennedy2.ttl");
    }

    @Test
    public void prepareMashup() throws Exception
    {
        Select.main(TEST + "kennedy.ttl", TEST + "geoNamesCache.ttl", //
                QUERY_DIR + "mashup/mashup.arq", "target/mashup.txt");
    }

    @Test
    public void prepareMashupWithFolder() throws Exception
    {
        Select.main(TEST, QUERY_DIR + "mashup/mashup.arq", "target/mashup.tsv");
    }

    @Test
    public void toHtml() throws Exception
    {
        Select.main(TEST + "kennedy.ttl", MAIN + "result-to-html.xsl", QUERY_DIR + "CountEventsPerPlace.arq", "target/report.html");
    }

    @Test
    public void migrations() throws Exception
    {
        KmlGenerator.main(TEST + "kennedy.ttl", TEST + "geoNamesCache.ttl",//
                MAIN + "kml-by-birth.properties", QUERY_DIR + "mashup/places-by-birth.arq", "target/places1.kml");
    }

    @Test
    public void migrationsWithFolder() throws Exception
    {
        KmlGenerator.main(MAIN + "kml-by-birth.properties", TEST, QUERY_DIR + "mashup/places-by-birth.arq", "target/birthPlaces.kml");
    }

    @Test
    public void birthPlacesFirstStep() throws Exception
    {
        Select.main(TEST, QUERY_DIR + "mashup/places-by-birth.arq", "target/birthPlaces.txt");
    }

    @Test
    public void mariagePlacesFirstStep() throws Exception
    {
        Select.main(TEST, QUERY_DIR + "mashup/places-by-birth.arq", "target/marrigaePlaces.txt");
    }
}
