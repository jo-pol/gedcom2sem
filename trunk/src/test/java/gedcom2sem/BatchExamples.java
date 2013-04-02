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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.gedcom4j.parser.GedcomParserException;
import org.junit.BeforeClass;
import org.junit.Test;

public class BatchExamples
{
    private static final String KENNEDY_TTL = "target/kennedy.ttl";

    /** built in resources */
    private static final String MAIN = "src/main/resources/";

    /** Examples of input / configuration files */
    private static final String TEST = "src/test/resources/";

    private static final String CACHE = TEST + "geoNamesCache.ttl";

    /**
     * Converts a file with extension ged into semantic statements. The gedcom tags are simply turned
     * into predicates and type values.
     * 
     * @throws GedcomParserException
     * @throws IOException
     * @throws FileNotFoundException
     */
    @BeforeClass
    public static void convert() throws Exception
    {
        Convert.main(//
                MAIN + "prefixes.ttl", //
                MAIN + "rules/basic.rules", //
                MAIN + "rules/additional.rules", //
                TEST + "geoMashup.rules", //
                TEST + "kennedy.ged", //
                KENNEDY_TTL);
    }

    /**
     * Converts a file with extension ged into a semantic document. Files with rules are used to replace
     * the initial triplyfied gedcom tags. Additional semantic documents enrich the gedcom source.
     * 
     * @throws GedcomParserException
     * @throws IOException
     * @throws FileNotFoundException
     */
    @Test
    public void transform() throws FileNotFoundException, IOException, GedcomParserException
    {
        // TODO fix blind nodes caused by INDI records not in a FAM record
        // these are typically authors of the gedcom or SOUR entities
        // TODO multiply birth/marriage for other event types (after review)
        
        Transform.main(//
                // input
                TEST + "kennedy.ged", //
                MAIN + "prefixes.ttl", //
                // do not merge rule files that depend on one another
                TEST + "geoMashup.rules", // causes blind nodes for not handled types of events
                MAIN + "rules/foaf.rules", //
                MAIN + "rules/bio/child.rules",//
                MAIN + "rules/bio/birth.rules", //
                MAIN + "rules/bio/marriage.rules", //
                // Provenance need the results of primaryTopicOf):
                TEST + "primaryTopicOf.rules", //
                MAIN + "rules/provenance/publisher.rules", //
                MAIN + "rules/provenance/modified.rules", //
                // integration after provenance to avoid false credits
                // it would be great to have pages with RDFa
                TEST + "integration.rules", //
                // output
                "target/kennedy2.ttl");
    }

    @Test
    public void withoutRules() throws Exception
    {
        Convert.main(TEST + "kennedy-mini.ged", "target/mini.ttl");
    }

    /**
     * Intended for validation but http://www.w3.org/RDF/Validator/ and http://inspector.sindice.com seem
     * to accept something containing "_:x bio:birth [rdf:type bio:marriage]."
     */
    @Test
    public void foafBioRdf() throws Exception
    {
        Transform.main(//
                MAIN + "prefixes.ttl", //
                TEST + "geoMashup.rules", // causes blind nodes for not handled types of events
                MAIN + "rules/foaf.rules", //
                MAIN + "rules/bio/child.rules",//
                MAIN + "rules/bio/birth.rules", //
                MAIN + "rules/bio/marriage.rules", //
                // Provenance need the results of primaryTopicOf):
                TEST + "primaryTopicOf.rules", //
                MAIN + "rules/provenance/publisher.rules", //
                MAIN + "rules/provenance/modified.rules", //
                // I/O
                TEST + "kennedy-mini.ged", //
                "target/mini.rdf");
    }

    @Test
    public void prepareMashup() throws Exception
    {
        Select.main(KENNEDY_TTL, //
                CACHE, //
                MAIN + "reports/mashup.arq", //
                "target/mashup.txt");
    }

    @Test
    public void prepareMashupWithFolder() throws Exception
    {
        Select.main(TEST, KENNEDY_TTL, MAIN + "reports/mashup.arq", //
                "target/mashup.tsv");
    }

    @Test
    public void toHtml() throws Exception
    {
        Select.main(KENNEDY_TTL, //
                MAIN + "result-to-html.xsl", //
                MAIN + "reports/CountEventsPerPlace.arq", //
                "target/report.html");
    }

    @Test
    public void migrations() throws Exception
    {
        KmlGenerator.main(CACHE, KENNEDY_TTL, //
                MAIN + "kml-by-birth.properties", //
                MAIN + "reports/places-by-birth.arq", //
                "target/places1.kml");
    }

    @Test
    public void migrationsWithFolder() throws Exception
    {
        KmlGenerator.main(TEST, KENNEDY_TTL, //
                MAIN + "kml-by-birth.properties", MAIN + "reports/places-by-birth.arq", //
                "target/birthPlaces.kml");
    }

    @Test
    public void birthPlacesFirstStep() throws Exception
    {
        Select.main(CACHE, KENNEDY_TTL, MAIN + "reports/places-by-birth.arq", "target/birthPlaces.txt");
    }

    @Test
    public void mariagePlacesFirstStep() throws Exception
    {
        Select.main(CACHE, KENNEDY_TTL, MAIN + "reports/places-by-birth.arq", "target/marrigaePlaces.txt");
    }
}
