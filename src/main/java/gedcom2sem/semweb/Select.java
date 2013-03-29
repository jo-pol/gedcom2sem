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
package gedcom2sem.semweb;

import gedcom2sem.io.FileNameArguments;
import gedcom2sem.io.FileUtil;

import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Select
{
    public static void main(final String... fileNames) throws IOException, TransformerFactoryConfigurationError, TransformerException
    {
        final FileNameArguments arguments = new FileNameArguments(fileNames);
        final Model model = arguments.readInto(ModelFactory.createDefaultModel());
        final String queryStr = FileUtil.read(arguments.getMandatoryFile("arq"));
        arguments.write(executeQuery(model, queryStr));
    }

    private static ResultSet executeQuery(final Model model, final String queryStr)
    {
        final QuerySolutionMap qsm = new QuerySolutionMap();
        final QueryExecution queryExecution = QueryExecutionFactory.create(queryStr, Syntax.syntaxARQ, model, qsm);
        return queryExecution.execSelect();
    }
}
