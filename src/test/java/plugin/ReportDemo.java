package plugin;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringStartsWith.startsWith;
import gedcom2sem.gedsem.Convert;
import gedcom2sem.gedsem.Parser;
import gedcom2sem.io.FileUtil;
import gedcom2sem.semweb.KmlGenerator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.gedcom4j.parser.GedcomParserException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping.Factory;
import com.hp.hpl.jena.sparql.core.Prologue;

/**
 * From a testing point of view, these are just smoke tests. The methods are rather snippets of code to
 * connect to dialogs and menu commands. Methods annotated with "Test" format the result of a report
 * query. The test framework runs the test methods for each query returned by the method annotated with
 * "Parameters". Users might develop their own sets of rule files for their own queries. Some portions
 * may take long to load and/or require much memory. Choose between loading on an as needed basis to
 * avoid spilling memory, or let a save action of the gedcom trigger loading at low priority in the
 * background so things are ready when needed.
 */
@RunWith(Parameterized.class)
public class ReportDemo
// As the name does not end or start with test, maven does not execute this class as a test.
//
// Note that some reports were developed with a gedcom using special conventions
// and remain empty for the Kennedy family.
// Comment headers may be oriented to GenJ or Ancestris, the birth place of this library.
{
    private static final String OUT_DIR = "target/plugin-demo/ReportDemo/";

    /** The folder stored as conf in the download. */
    private static final String CONF = "src/main/resources/";

    /** The folder stored as test in the download. */
    private static final String TEST = "src/test/resources/";

    private static final String GEDCOM = System.getProperty("gedcom.file", TEST + "kennedy.ged");
    private static final String PROBAND = System.getProperty("proband.id",  "@R4@");
    private static final String CLOSE_RELATIVES = System.getProperty("close.relatives.id", "@R4@");
    private static final String PATCHWORK_FAMILY = System.getProperty("patchwork.family.id", "@F0@");

    private static Transformer xslTransformer;
    private static Collection<Object[]> constructorParameters = new ArrayList<Object[]>();

    private final String queryString;
    private final Model model;

    private final String queryFileName;

    /**
     * The enum allows to pick a model in the method annotated with "Parameters", and initialize the
     * models later with a method annotated with "BeforeClass". Do not store models in a top level enum
     * in your application as it allows models for only one gedcom at any given time. The methods load
     * the delivered test data with one set of the delivered rule files.
     */
    private enum GedcomModel
    {
        withoutRules, withBasicRules, withBasicGeoRules, withEveryting;

        Model model = null;

        void readGedcom(final String fileName) throws IOException, GedcomParserException
        {
            final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileName));
            try
            {
                GedcomModel.this.model = new Parser().parse(inputStream);
            }
            finally
            {
                inputStream.close();
            }
            GedcomModel.withoutRules.model.read(new FileInputStream(CONF + "prefixes.ttl"), (String) null, "TURTLE");
            Convert.preparePrefixes(model);
            System.out.println("Name space prefixes: " + GedcomModel.this.model.getNsPrefixMap());
        }

        static void loadModels() throws IOException, GedcomParserException
        {
            withoutRules.readGedcom(GEDCOM);
            withBasicRules.applyRules(CONF + "rules/Basic.rules", withoutRules.model);
            withBasicGeoRules.applyRules(TEST + "geoMashup.rules", withBasicRules.model);
            withEveryting.applyRules(CONF + "rules/Additional.rules", withBasicGeoRules.model);
        }

        void readCachedData() throws FileNotFoundException
        {
            GedcomModel.withoutRules.model.read(new FileInputStream(TEST + "geoNamesCache.ttl"), (String) null, "TURTLE");
        }

        void applyRules(final String filename, Model model) throws IOException
        {
            System.out.println("loading: " + filename);
            GedcomModel.this.model = Convert.applyRules(FileUtil.read(new File(filename)), model);
        }
    }

    @Parameters
    public static Collection<Object[]> getConstructorParameters()
    {
        // Reports will also run using a model with more rules than specified.
        add("AgeDiffBetweenSpouses.arq", GedcomModel.withoutRules);
        add("classmates.arq", GedcomModel.withEveryting);
        add("CountEventsPerPlace.arq", GedcomModel.withoutRules);
        add("CountGivnNames.arq", GedcomModel.withoutRules);
        add("dbpediaLanguages.arq", GedcomModel.withBasicGeoRules);
        add("dbpediaProperties.arq", GedcomModel.withBasicGeoRules);
        add("dbpediaRelatedEntities.arq", GedcomModel.withBasicGeoRules);
        add("FAM-PatchworkFamily.arq", GedcomModel.withBasicRules, PATCHWORK_FAMILY);
        add("INDI-RootAncestors.arq", GedcomModel.withEveryting, PROBAND);
        add("INDI-TimeLineWithCloseRelatives.arq", GedcomModel.withBasicRules, CLOSE_RELATIVES);
        add("SOSA-EventDocuments.arq", GedcomModel.withoutRules);
        add("SOSA-InbredStatistics.arq", GedcomModel.withoutRules);
        add("SOSA-MultiMedia.arq", GedcomModel.withoutRules);
        add("SOSA-Roots.arq", GedcomModel.withoutRules);
        // geoNamesCache.ttl is is added to the models when the constructor encounters mashup.arq
        add("mashup.arq", GedcomModel.withBasicGeoRules);
        add("places-by-birth.arq", GedcomModel.withBasicGeoRules); // requires INDI:_SOSAN
        add("places-by-marriage.arq", GedcomModel.withBasicGeoRules); // requires INDI:_SOSAN
        add("geonamesProperties.arq", GedcomModel.withBasicGeoRules);
        add("geonamesRelatedEntities.arq", GedcomModel.withBasicGeoRules);
        // so far the provided report queries
        // a user may customize reports and/or develop new ones.
        return constructorParameters;
    }

    /**
     * Typically at most once per session or when the loaded file changes. Maintain a pool as users might
     * create variants. See also https://github.com/jo-pol/gedcom2sem/wiki/Localizing
     * 
     * @throws MalformedURLException
     * @throws IOException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    @BeforeClass
    public static void loadXSL() throws MalformedURLException, IOException, TransformerFactoryConfigurationError, TransformerException
    {
        final StreamSource xslSS = new StreamSource(new File(CONF + "result-to-html.xsl").toURI().toURL().openStream());
        xslTransformer = TransformerFactory.newInstance().newTransformer(xslSS);
        new File(OUT_DIR).mkdirs();
    }

    /**
     * Constructs the models required for the queries. Escpecially the additional rules may take (too)
     * long. An application should apply rules in the background and allow abort. Saving the gedcom could
     * be a trigger, or an explicit user request or a request to run a query after the gedcom changed
     * since the last conversion.
     * 
     * @throws GedcomParserException
     * @throws IOException
     */
    @BeforeClass
    public static void loadModels() throws IOException, GedcomParserException
    {
        GedcomModel.loadModels();
    }

    /**
     * Adds one instance of constructor parameters to the collection. Same signature as the constructor.
     */
    private static void add(final String queryFileName, final GedcomModel gedcomModel, final String... entityID)
    {
        constructorParameters.add(new Object[] {queryFileName, gedcomModel, entityID});
    }

    /**
     * The constructor prepares the fields for the tests.
     * 
     * @throws IOException
     */
    public ReportDemo(final String queryFileName, final GedcomModel gedcomModel, final String... entityID) throws IOException
    {
        this.queryFileName = queryFileName;
        model = gedcomModel.model;
        // Read the query and replace the placeholder %s
        queryString = String.format(FileUtil.read(new File(CONF + "reports/" + queryFileName)), (Object[]) entityID);
        // all the models will see the new data (???)
        if ("mashup.arq".equals(queryFileName))
            GedcomModel.withoutRules.readCachedData();
    }

    @Test
    public void asTSV() throws Exception
    {
        String fileName = OUT_DIR + queryFileName.replace(".arq", "") + ".tsv";
        FileOutputStream outputStream = new FileOutputStream(fileName);
        try
        {
            ResultSetFormatter.outputAsTSV(outputStream, runQuery());
        }
        finally
        {
            outputStream.close();
        }
        assertMoreThanOneLine(fileName);
    }

    @Test
    public void asCSV() throws Exception
    {
        FileOutputStream outputStream = new FileOutputStream(OUT_DIR + queryFileName.replace(".arq", "") + ".csv");
        try
        {
            ResultSetFormatter.outputAsCSV(outputStream, runQuery());
        }
        finally
        {
            outputStream.close();
        }
    }

    @Test
    public void asXML() throws Exception
    {
        FileOutputStream outputStream = new FileOutputStream(OUT_DIR + queryFileName.replace(".arq", "") + ".xml");
        try
        {
            ResultSetFormatter.outputAsXML(outputStream, runQuery());
        }
        finally
        {
            outputStream.close();
        }
    }

    @Test
    public void asJSON() throws Exception
    {
        FileOutputStream outputStream = new FileOutputStream(OUT_DIR + queryFileName.replace(".arq", "") + ".json");
        try
        {
            ResultSetFormatter.outputAsJSON(outputStream, runQuery());
        }
        finally
        {
            outputStream.close();
        }
    }

    @Test
    public void asTXT() throws IOException
    {
        FileOutputStream outputStream = new FileOutputStream(OUT_DIR + queryFileName.replace(".arq", "") + ".txt");
        try
        {
            final Prologue prologue = new Prologue(Factory.create().setNsPrefixes(model.getNsPrefixMap()));
            outputStream.write(ResultSetFormatter.asText(runQuery(), prologue).getBytes());
        }
        finally
        {
            outputStream.close();
        }
    }

    @Test
    public void asKML() throws Exception
    {
        Assume.assumeThat(queryFileName, startsWith("places"));

        final String propertiesFileName = CONF + queryFileName.replace(".arq", ".properties").replace("places-", "kml-");
        final ResourceBundle properties = new PropertyResourceBundle(new FileInputStream(propertiesFileName));
        FileOutputStream outputStream = new FileOutputStream(OUT_DIR + queryFileName.replace(".arq", "") + ".kml");
        try
        {
            new KmlGenerator(model, properties, queryString).create(outputStream);
        }
        finally
        {
            outputStream.close();
        }
    }

    @Test
    public void asHTML() throws MalformedURLException, IOException, TransformerFactoryConfigurationError, TransformerException
    {
        final ByteArrayOutputStream xml = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsXML(xml, runQuery());
        final StreamSource xmlSS = new StreamSource(new ByteArrayInputStream(xml.toByteArray()));
        FileOutputStream outputStream = new FileOutputStream(OUT_DIR + queryFileName.replace(".arq", "") + ".html");
        try
        {
            xslTransformer.transform(xmlSS, new StreamResult(outputStream));
        }
        finally
        {
            outputStream.close();
        }
    }

    private ResultSet runQuery()
    {
        final QuerySolutionMap qsm = new QuerySolutionMap();
        return QueryExecutionFactory.create(queryString, Syntax.syntaxARQ, model, qsm).execSelect();
    }

    private void assertMoreThanOneLine(String fileName) throws FileNotFoundException, IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        try
        {
            reader.readLine();
            Assert.assertThat(fileName + " should have more than a header line", reader.readLine(), notNullValue());
        }
        finally
        {
            reader.close();
        }
    }
}
