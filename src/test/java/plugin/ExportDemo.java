package plugin;

import gedcom2sem.gedsem.Transform;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.gedcom4j.parser.GedcomParserException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

public class ExportDemo
{
    private static final Logger logger = LoggerFactory.getLogger(ReportDemo.class);

    /** The folder stored as conf in the download. */
    private static final String CONF = "src/main/resources/";

    /** The folder stored as test in the download. */
    private static final String TEST = "src/test/resources/";

    private final ByteArrayOutputStream outputStream;

    private String lang;
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
                "dummy.ttl");
        // NOTE: the output file dummy.ttl is not actually used
        // but stubbornly full command line validation is applied
        // TODO fix with or after issue 15 (split output per individual)
    }

    public ExportDemo()
    {
        outputStream = new ByteArrayOutputStream();
    }

    @Ignore
    @Test
    public void asJSON()
    {
        // TODO upgrade to http://jena.apache.org/documentation/io/index.html
        lang = "RDF/JSON";
        model.write(outputStream, lang);
    }

    @Ignore
    @Test
    public void asTRIG()
    {
        // TODO upgrade to http://jena.apache.org/documentation/io/index.html
        lang = "TriG";
        model.write(outputStream, lang);
    }

    @Test
    public void asTTL()
    {
        lang = "TURTLE";
        model.write(outputStream, lang);
    }

    @Test
    public void asRDF()
    {
        lang = "RDF/XML";
        model.write(outputStream, lang);
    }

    @Test
    public void asN3()
    {
        lang = "N3";
        model.write(outputStream, lang);
    }

    @Test
    public void asNT()
    {
        lang = "N-TRIPLE";
        model.write(outputStream, lang);
    }

    @Test
    public void asSlowRDF()
    {
        lang = "RDF/XML-ABBREV";
        model.write(outputStream, lang);
    }

    @After
    public void logResult() throws UnsupportedEncodingException
    {
        String result = outputStream.toString("UTF-8");
        int loggedLength = result.length() < 2000 ? result.length() : 2000;
        logger.info(lang+"=============="+System.getProperty("line.separator") + result.substring(0, loggedLength));
    }
}
