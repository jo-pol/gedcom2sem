package gedcom2sem.gedsem;

import gedcom2sem.io.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class Publish
{
    public static void main(final String... files) throws Exception
    {
        // TODO read geoMashup, but then file types don't tell input from output
        Convert.main(files);
        // TODO connect each FAM/INDI with provenance in HEAD
        // TODO redesign cleanup strategy:
        // deleting original statements deleted inferred statements
        // rather copy inferred statements to new model
        for (final String file : files)
        {
            final String language;
            try
            {
                language = FileUtil.guessLanguage(new File(file));
            }
            catch (final IllegalArgumentException e)
            {
                continue;
            }
            final Model model = ModelFactory.createDefaultModel();
            model.read(new FileInputStream(file), (String) null, language);
            System.err.println("" + model.size());
            for (final Statement stmt : model.listStatements().toList())
                if (stmt.getPredicate().toString().startsWith("http://genj"))
                    model.remove(stmt);
            System.err.println("" + model.size());
            for (final Statement stmt : model.listStatements().toList())
            {
                final Resource subject = stmt.getSubject();
                if (subject.toString().startsWith("-"))
                    // if(! model.listObjectsOfProperty(new Property(subject)).hasNext())
                    model.remove(stmt);
            }
            System.err.println("" + model.size());
            final PrintStream output = new PrintStream(file);
            model.write(output, language);
        }
    }
}
