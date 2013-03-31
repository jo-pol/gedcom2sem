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
package gedcom2sem.gedsem;

import gedcom2sem.io.FileNameArguments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.gedcom4j.parser.GedcomParserException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class Transform
{
    /**
     * Converts a file with extension ged into a semantic document. Files with rules are used to replace
     * the initial triplyfied gedcom tags. Additional semantic documents enrich the gedcom source.
     * 
     * @throws GedcomParserException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(final String... files) throws FileNotFoundException, IOException, GedcomParserException 
    {
        final Set<Statement> inferredStatements = Convert.execute(files);
        final Model modelOut = createModel(extractNsPrefixes(inferredStatements));
        copy(inferredStatements, modelOut);
        new FileNameArguments(files).write(modelOut);
        System.err.println("written: " + modelOut.listStatements().toList().size());
    }

    private static Model createModel(Map<String, String> extractNsPrefixes)
    {
        final Model modelOut = ModelFactory.createDefaultModel();
        modelOut.setNsPrefixes(extractNsPrefixes);
        return modelOut;
    }

    private static void copy(final Set<Statement> inferredStatements, final Model modelOut)
    {
        for (final Statement stmt : inferredStatements)
            modelOut.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
    }

    private static Map<String, String> extractNsPrefixes(final Set<Statement> inferredStatements)
    {
        if (inferredStatements==null || inferredStatements.size()==0)
            throw new IllegalStateException("no inference, kept the original statements");
        return inferredStatements.iterator().next().getModel().getNsPrefixMap();
    }
}
