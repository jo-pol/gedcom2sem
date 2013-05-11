package gedcom2sem.semweb;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;

public class ValidationTest
{
    private static final String BIO_SCHEMA = "http://vocab.org/bio/0.1/.rdf";
    private static final String FOAF_SCHEMA = "http://xmlns.com/foaf/spec/20100809.rdf";
    private static final String REL_SCHEMA = "http://vocab.org/relationship/.rdf";
    private static final String RDF_SCHEMA = "http://www.w3.org/2000/01/rdf-schema";
    private static Reasoner reasoner;

    @BeforeClass
    public static void createValidator() throws MalformedURLException, IOException
    {
        final Model schemaModel = ModelFactory.createDefaultModel();
        schemaModel.read(RDF_SCHEMA);
        schemaModel.read(REL_SCHEMA);
        schemaModel.read(FOAF_SCHEMA);
        schemaModel.read(BIO_SCHEMA);
        // URL localBIO = new File("src/test/resources/invalid/bio.rdf").toURI().toURL();
        // schemaModel.read(localBIO.openStream(), null, "RDF/XML-ABBREV");
        reasoner = ReasonerRegistry.getRDFSReasoner();
        reasoner.bindSchema(schemaModel);
    }

    // @BeforeClass
    public static void bio2rdf() throws Exception
    {
        final Model model = ModelFactory.createDefaultModel();
        final File data = new File("src/test/resources/invalid/bio.rdf");
        data.mkdirs();
        model.read(BIO_SCHEMA, null, "RDF/XML");
        model.write(new FileOutputStream("target/data/bio.rdf"), "RDF/XML-ABBREV");
    }

    // @BeforeClass
    public static void ttl2rdf() throws Exception
    {
        final Model model = ModelFactory.createDefaultModel();
        final File data = new File("src/test/resources/invalid/domain-range.ttl");
        data.mkdirs();
        model.read(new FileInputStream(data), null, "Turtle");
        model.write(new FileOutputStream("target/data/domain-range.rdf"), "RDF/XML-ABBREV");
    }

    @Test
    public void test() throws Exception
    {
        final Model model = ModelFactory.createDefaultModel();
        final File data = new File("src/test/resources/kennedy-mini.ttl");
        model.read(new FileInputStream(data), null, "Turtle");
        final ValidityReport validity = ModelFactory.createInfModel(reasoner, model).validate();
        assertTrue(validity.isValid());
    }

    @Test
    public void domainRange() throws Exception
    {
        // http://jena.apache.org/documentation/inference/#validation

        final Model model = ModelFactory.createDefaultModel();
        final File data = new File("target/domain-range.rdf");
        model.read(new FileInputStream(data), null, "RDF/XML");
        final ValidityReport validity = ModelFactory.createInfModel(reasoner, model).validate();
        assertThat(validity.isValid(), is(true)); // FIXME there a obvious errors
        assertThat(validity.getReports().hasNext(), is(false)); // FIXME there a obvious errors

        // http://www.langdale.com.au/validate/
        // FIXME the tool has trouble reading BIO and FOAF
        // String dataURL = data.toURI().toURL().toString();
        // String[] args = new String[] {dataURL, RDF_SCHEMA, BIO_SCHEMA, FOAF_SCHEMA};
        // org.iec.tc57.cimrdf.Main.main(args);
    }

    void pelletValidation()
    {
        // sheet 59 of http://clarkparsia.com/pellet/tutorial/

        // <repositories>
        // <repository>
        // <id>on.cs.unibas.ch</id>
        // <name>DBIS Maven Releases Repository</name>
        // <url>http://on.cs.unibas.ch/maven/repository</url>
        // </repository>
        // </repositories>
        //
        // <dependency>
        // <groupId>com.owldl</groupId>
        // <artifactId>pellet</artifactId>
        // <version>2.2.0</version>
        // <scope>test</scope>
        // </dependency>

        // import org.mindswap.pellet.jena.PelletReasonerFactory;
        // import com.clarkparsia.pellet.

        // Reasoner r = PelletReasonerFactory.theInstance().create();
        // // create an inferencing model using Pellet reasoner
        // InfModel dataModel = ModelFactory.createInfModel(r);
        // // load the schema and instance data to Pellet
        // dataModel.read( "file:data.rdf" );
        // dataModel.read( "file:schema.owl" );
        // // Create the IC validator and associate it with the dataset
        // JenaIOUtils ICValidator validator = new JenaICValidator(dataModel);
        // // Load the constraints into the IC validator
        // validator.getConstraints().read("file:constraints.owl");
        // // Get the constraint violations
        // Iterator<ConstraintViolation> violations = validator.getViolations();
    }
}
