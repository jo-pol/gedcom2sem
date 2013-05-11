import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;

public class FixMe
{
    public static void main(String... args)
    {
        final Model schemaModel = ModelFactory.createDefaultModel();
        schemaModel.read("http://vocab.org/bio/0.1/.rdf");
        ReasonerRegistry.getOWLReasoner().bindSchema(schemaModel);
    }
}
