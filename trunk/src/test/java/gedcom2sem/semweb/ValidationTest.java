package gedcom2sem.semweb;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private static Reasoner reasoner;

    @BeforeClass
    public static void createValidator() throws MalformedURLException, IOException
    {
        final Model schemaModel = ModelFactory.createDefaultModel();
        schemaModel.read("http://www.w3.org/2000/01/rdf-schema");
        schemaModel.read("http://www.w3.org/2002/07/owl");
        schemaModel.read("http://vocab.org/relationship/.rdf");
        schemaModel.read("http://xmlns.com/foaf/spec/20100809.rdf");
        schemaModel.read("http://vocab.org/bio/0.1/.rdf");
        reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner.bindSchema(schemaModel);
    }

    @Test
    public void motherDifferentFrom() throws Exception
    {
        // http://jena.apache.org/documentation/inference/#validation

        final Model model = ModelFactory.createDefaultModel();
        final byte[] bytes = "_:p <http://purl.org/vocab/bio/0.1/mother> _:p".getBytes();
        model.read(new ByteArrayInputStream(bytes), null, "Turtle");
        final ValidityReport validity = ModelFactory.createInfModel(reasoner, model).validate();
        // FIXME violates http://vocab.org/bio/0.1/.html#mother being a sub property of
        // http://www.w3.org/2002/07/owl#differentFrom
        assertThat(validity.isValid(), is(true));
        assertThat(validity.getReports().hasNext(), is(false));
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
