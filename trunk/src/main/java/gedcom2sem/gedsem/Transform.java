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

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class Transform
{
    public static void main(final String... files) throws Exception
    {
        final Set<Statement> inferredStatements = Convert.execute(files);
        if (inferredStatements==null || inferredStatements.size()==0)
            throw new IllegalStateException("no inference, kept the original statements");
        final Model modelOut = ModelFactory.createDefaultModel();
        for (final Statement stmt : inferredStatements)
            modelOut.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
        new FileNameArguments(files).write(modelOut);
        System.err.println("written: " + modelOut.listStatements().toList().size());
    }
}
