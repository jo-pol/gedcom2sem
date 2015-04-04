package plugin;

import gedcom2sem.gedsem.Transform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.gedcom4j.parser.GedcomParserException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class ExportDemo
{
    private static final String OUT_DIR = "target/plugin-demo/ExportDemo/";

    /** The folder stored as conf in the download. */
    private static final String CONF = "src/main/resources/";

    /** The folder stored as test in the download. */
    private static final String TEST = "src/test/resources/";

    private FileOutputStream outputStream;

    private static Model model;

    /**
     * See also {@link ReportDemo}.GedcomModel<br>
     * In this case however, it is no use to have only part of the rules applied.
     * 
     * @throws FileNotFoundException
     * @throws MalformedURLException
     * @throws IOException
     * @throws GedcomParserException
     */
    @BeforeClass
    public static void transform() throws FileNotFoundException, MalformedURLException, IOException, GedcomParserException
    {
        new File(OUT_DIR).mkdirs();
        model = Transform.transform(//
                TEST + "kennedy.ged", //
                CONF + "prefixes.ttl", //
                TEST + "geoMashup.rules", //
                CONF + "rules/foaf.rules", //
                CONF + "rules/bio", //
                TEST + "primaryTopicOf.rules", //
                CONF + "rules/provenance", //
                TEST + "/geoMashup.rules", //
                TEST + "/integration.rules",//
                OUT_DIR + "out.ttl");
        // NOTE: the out.ttl is not actually used
        // but stubbornly full command line validation is applied
        // TODO fix with or after issue 15 (split output per individual)
    }

    @Ignore
    @Test
    public void asJSON() throws Exception
    {
        // TODO upgrade to http://jena.apache.org/documentation/io/index.html
        outputStream = new FileOutputStream(OUT_DIR + "json.txt");
        model.write(outputStream, "RDF/JSON");
    }

    @Ignore
    @Test
    public void asTRIG() throws Exception
    {
        // TODO upgrade to http://jena.apache.org/documentation/io/index.html
        outputStream = new FileOutputStream(OUT_DIR + "trig.txt");
        model.write(outputStream, "TriG");
    }

    @Test
    public void asTTL() throws Exception
    {
        outputStream = new FileOutputStream(OUT_DIR + "turtle.txt");
        model.write(outputStream, "TURTLE");
    }

    @Test
    public void asRDF() throws Exception
    {
        outputStream = new FileOutputStream(OUT_DIR + "xml.rdf");
        model.write(outputStream, "RDF/XML");
    }

    @Test
    public void asN3() throws Exception
    {
        outputStream = new FileOutputStream(OUT_DIR + "n3.txt");
        model.write(outputStream, "N3");
    }

    @Test
    public void asNT() throws Exception
    {
        outputStream = new FileOutputStream(OUT_DIR + "n-tripple.txt");
        model.write(outputStream, "N-TRIPLE");
    }

    @Test
    public void asSlowRDF() throws Exception
    {
        outputStream = new FileOutputStream(OUT_DIR + "xml-abbrev.rdf");
        model.write(outputStream, "RDF/XML-ABBREV");
    }

    @After
    public void logResult() throws Exception
    {
        outputStream.close();
    }
}
