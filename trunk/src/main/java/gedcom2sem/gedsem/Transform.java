// @formatter:off
/*
 * Copyright 2012, J. Pol
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

import gedcom2sem.io.FileUtil;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class Transform
{
    public static void main(final String... files) throws Exception
    {
        // TODO read geoMashup, but then file types don't tell input from output
        // TODO connect each FAM/INDI with provenance in HEAD
        final Set<Statement> inferredStatements = Convert.execute(files);
        final Model modelOut = ModelFactory.createDefaultModel();
        for (final Statement stmt : inferredStatements)
            modelOut.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
        // TODO add name space prefixes, must be user specified along with the rules
        for (final String file : files)
        {
            try
            {
                final String language = FileUtil.guessLanguage(new File(file));
                modelOut.write(new PrintStream(file), language);
                System.err.println("written: " + modelOut.listStatements().toList().size());
                return;
            }
            catch (final IllegalArgumentException e)
            {
                continue;
            }
        }
    }
}
