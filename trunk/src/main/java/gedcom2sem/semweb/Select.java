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

import gedcom2sem.io.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;

public class Select
{
    static final String DEFAULT_XSL = "http://www.w3.org/TR/rdf-sparql-XMLres/result-to-html.xsl";

    public static void main(final String... fileNames) throws IOException, TransformerFactoryConfigurationError, TransformerException
    {
        final Model model = ModelFactory.createDefaultModel();
        File outputFile = null;
        File xsl = null;
        String queryStr = null;
        for (final String fileName : fileNames)
        {
            final File file = new File(fileName);
            final String language = FileUtil.guessLanguage(file);
            if (language != null)
                model.read(new FileInputStream(file), (String) null, language);
            else if (fileName.toLowerCase().endsWith(".xsl"))
                xsl = file;
            else if (fileName.toLowerCase().endsWith(".arq"))
                queryStr = FileUtil.read(file);
            else
                outputFile = file;
        }
        if (model.size() == 0)
            throw new IllegalArgumentException("no or empty data files (.nt, .n3, .ttl, .rdf)");
        if (queryStr == null)
            throw new IllegalArgumentException("no query file (.arq)");
        if (outputFile == null)
            throw new IllegalArgumentException("no output file (.txt, .tsv, .csv, .json, .xml, .htm, .html)");

        final ResultSet resultSet = executeQuery(model, queryStr);
        final OutputStream outputStream = new FileOutputStream(outputFile);
        try
        {
            final String ext = outputFile.getName().replaceAll(".*[.]", "").toLowerCase();
            if ("tsv".equals(ext))
                ResultSetFormatter.outputAsTSV(outputStream, resultSet);
            else if ("csv".equals(ext))
                ResultSetFormatter.outputAsCSV(outputStream, resultSet);
            else if ("xml".equals(ext))
                ResultSetFormatter.outputAsXML(outputStream, resultSet);
            else if ("json".equals(ext))
                ResultSetFormatter.outputAsJSON(outputStream, resultSet);
            else if ("txt".equals(ext))
                outputAsText(resultSet, outputStream, model);
            else if (ext.matches("html?"))
                outputAsHtml(resultSet, outputStream, xsl);
            else
                throw new IllegalArgumentException("output extension not supported: " + outputFile);
        }
        finally
        {
            outputStream.close();
        }
    }

    private static ResultSet executeQuery(final Model model, final String queryStr)
    {
        final QuerySolutionMap qsm = new QuerySolutionMap();
        final QueryExecution queryExecution = QueryExecutionFactory.create(queryStr, Syntax.syntaxARQ, model, qsm);
        return queryExecution.execSelect();
    }

    private static void outputAsHtml(final ResultSet resultSet, final OutputStream outputStream, final File xsl) //
            throws TransformerException, TransformerFactoryConfigurationError, IOException
    {
        final ByteArrayOutputStream xml = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsXML(xml, resultSet);
        final StreamSource xmlStreamSource = new StreamSource(new ByteArrayInputStream(xml.toByteArray()));
        readXsl(xsl).transform(xmlStreamSource, new StreamResult(outputStream));
    }

    private static void outputAsText(final ResultSet resultSet, final OutputStream outputStream, final Model model) //
            throws IOException
    {
        final Prologue prologue = new Prologue(PrefixMapping.Factory.create().setNsPrefixes(model.getNsPrefixMap()));
        outputStream.write(ResultSetFormatter.asText(resultSet, prologue).getBytes());
    }

    private static Transformer readXsl(final File file) //
            throws TransformerConfigurationException, TransformerFactoryConfigurationError, IOException
    {
        final StreamSource ss;
        if (file == null)
            ss = new StreamSource(file);
        else
            ss = new StreamSource(new URL(DEFAULT_XSL).openStream());
        return TransformerFactory.newInstance().newTransformer(ss);
    }
}
