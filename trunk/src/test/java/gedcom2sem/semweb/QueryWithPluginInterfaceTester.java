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
package gedcom2sem.semweb;

import gedcom2sem.gedsem.Parser;
import gedcom2sem.gedsem.UriFormats;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.gedcom4j.parser.GedcomParserException;
import org.junit.Test;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryWithPluginInterfaceTester extends AbstractQueryTest
// name does neither start nor end with test so maven can build a jar if web-resources don't cooperate
{

    public QueryWithPluginInterfaceTester(final Boolean mashup, final Integer expectedNrOfLines, final String queryFileName)
    {
        super(mashup, expectedNrOfLines, queryFileName);
    }

    @Test
    public void run() throws Exception
    {
        final Model model = createModel();
        final String queryStr = readFile(queryFileName);
        final QueryExecution queryExecution = executeQuery(model, queryStr);
        formatQueryOutput(queryExecution);
    }

    private QueryExecution executeQuery(final Model model, final String queryStr)
    {
        return QueryExecutionFactory.create(queryStr, Syntax.syntaxARQ, model, new QuerySolutionMap());
    }

    private Model createModel() throws FileNotFoundException, IOException, GedcomParserException
    {
        final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(GEDCOM));
        final Model model = new Parser().parse(inputStream, new UriFormats().getURIs());
        if (mashup)
            model.read(new File("src/test/resources/mashup.ttl").toURI().toURL().toString(),null, "TURTLE");
        return model;
    }

    private void formatQueryOutput(final QueryExecution queryExecution) throws FileNotFoundException, IOException
    {
        final OutputStream outputStream = new FileOutputStream(REPORT_TXT);
        outputStream.write(ResultSetFormatter.asText(queryExecution.execSelect()).getBytes());
        outputStream.close();
    }

    private String readFile(final String filePath) throws Exception
    {
        final FileInputStream input = new FileInputStream(filePath);
        final byte[] fileData = new byte[input.available()];
        input.read(fileData);
        input.close();
        return new String(fileData, "UTF-8");
    }
}
