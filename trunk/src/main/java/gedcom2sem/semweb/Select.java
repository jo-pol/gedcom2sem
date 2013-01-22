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

import gedcom2sem.sem.Extension;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;

public class Select
{
    private final Syntax querySyntax;
    static final String DEFAULT_XSL = "http://www.w3.org/TR/rdf-sparql-XMLres/result-to-html.xsl";
    private static final Set<String> inputFormats;
    static
    {
        inputFormats = new HashSet<String>();
        for (final Extension ext : Extension.values())
            inputFormats.add(ext.name());
    }
    private final Model model = ModelFactory.createDefaultModel();
    private File outputFile = null;
    private File xsl = null;

    /**
     * Creates an object that executes SPARQL select queries with {@link Syntax.syntaxARQ).
     */
    public Select()
    {
        querySyntax = Syntax.syntaxARQ;
    }

    /**
     * Creates an object that executes SPARQL select queries with the specified syntax.
     */
    public Select(final Syntax querySyntax)
    {
        this.querySyntax = querySyntax;
    }

    /**
     * Passes the arguments on to {@link #process(String...)}. Processing continues with the next
     * argument in case an argument causes an exception. Exceptions are report on stderr without stack
     * traces.
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(final String... args) throws Exception
    {
        final Select select = new Select(Syntax.syntaxARQ);
        for (final String arg : args)
        {
            try
            {
                select.process(new File(arg));
            }
            catch (final Exception e)
            {
                System.err.println("skipping " + arg + ": "+e.toString());
                throw e;
            }
        }
    }

    /**
     * @param files
     *        input files (see {@link Extension}), output files (txt/csv/tsv/htm/html/xml) and/or query
     *        files (arq). As soon as a query file is encountered the results are written to the last
     *        encountered output file. In case of HTML also an XSL file is required, whether the output
     *        is actually HTML depends on the XSL. The default XSL is {@link #DEFAULT_XSL} though it is
     *        recommended to save a local copy.
     * @return this object for chaining. The following example reads all triple files from folder x and
     *         executes the first query, the second query fails to prevent overwriting the previous
     *         results. If folder x contains arq files, they might be processed in random order depending
     *         on the presence of output files.
     *         <code>new Select().process(new File("x").listFiles()).process("an.xsl","out.html").process("y.arq","z.arq")</code>
     *         In contrast with {@link main}: as soon as one of the arguments in a single call causes an
     *         exception, the rest will be ignored.
     * @throws IOException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @throws IllegalStateException
     *         if no input or output file was processed before a query file, or the input files so far
     *         contained no triples.
     */
    public Select process(final File... files) throws IOException, TransformerFactoryConfigurationError, TransformerException
    {
        for (final File file : files)
        {
            final String ext = file.getName().replaceAll(".*[.]", "").toLowerCase();
            if (inputFormats.contains(ext))
            {
                final String language = Extension.valueOf(file).language();
                model.read(new FileInputStream(file), (String) null, language);
            }
            else if ("xsl".equals(ext))
                xsl = file;
            else if (!"arq".equals(ext))
                outputFile = file;
            else if (outputFile == null)
                throw new IllegalStateException("no output file yet");
            else if (model.size() == 0)
                throw new IllegalStateException("no input yet");
            else
            {
                final ResultSet resultSet = executeSelect(file);
                final OutputStream outputStream = new FileOutputStream(outputFile);
                try
                {
                    final String outExt = outputFile.getName().replaceAll(".*[.]", "").toLowerCase();
                    writeResultSet(outExt, resultSet, outputStream);
                    // do not overwrite with anther query
                    outputFile = null;
                }
                finally
                {
                    outputStream.close();
                }
            }
        }
        return this;
    }

    private void writeResultSet(final String ext, final ResultSet resultSet, final OutputStream outputStream) throws IOException,
            TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError
    {
        if ("tsv".equals(ext))
            ResultSetFormatter.outputAsTSV(outputStream, resultSet);
        else if ("csv".equals(ext))
            ResultSetFormatter.outputAsCSV(outputStream, resultSet);
        else if ("xml".equals(ext))
            ResultSetFormatter.outputAsXML(outputStream, resultSet);
        else if ("txt".equals(ext))
        {
            final Prologue prologue = new Prologue(PrefixMapping.Factory.create().setNsPrefixes(model.getNsPrefixMap()));
            outputStream.write(ResultSetFormatter.asText(resultSet, prologue).getBytes());
        }
        else if (!ext.matches("html?"))
            throw new IllegalArgumentException("output extension not supported: " + ext);
        else
        {
            final ByteArrayOutputStream xml = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsXML(xml, resultSet);
            final StreamSource xmlStreamSource = new StreamSource(new ByteArrayInputStream(xml.toByteArray()));
            readXsl().transform(xmlStreamSource, new StreamResult(outputStream));
        }
    }

    private Transformer readXsl() throws TransformerConfigurationException, TransformerFactoryConfigurationError, MalformedURLException, IOException
    {
        final StreamSource ss;
        if (xsl == null)
            ss = new StreamSource(xsl);
        else
            ss = new StreamSource(new URL(DEFAULT_XSL).openStream());
        return TransformerFactory.newInstance().newTransformer(ss);
    }

    private ResultSet executeSelect(final File file) throws IOException
    {
        final byte[] bytes = new byte[(int) file.length()];
        final InputStream inputStream = new FileInputStream(file);
        try
        {
            inputStream.read(bytes);
        }
        finally
        {
            inputStream.close();
        }
        final String q = new String(bytes);
        final QuerySolutionMap qsm = new QuerySolutionMap();
        return QueryExecutionFactory.create(q, querySyntax, model, qsm).execSelect();
    }
}
