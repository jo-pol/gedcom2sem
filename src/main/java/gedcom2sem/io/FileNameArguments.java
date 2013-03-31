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
package gedcom2sem.io;

import gedcom2sem.gedsem.Parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.gedcom4j.parser.GedcomParserException;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.util.FileUtils;

/** Responsible for i/o in formats dictated by extensions. */
public class FileNameArguments
{
    private final String[] input;
    private final File output;

    /**
     * The last file is the output file.
     * 
     * @throws MalformedURLException
     */
    public FileNameArguments(final String... fileNames)
    {
        input = Arrays.copyOfRange(fileNames, 0, fileNames.length - 1);
        output = new File(fileNames[fileNames.length - 1]);
    }

    /**
     * Reads the input files with semantical content recognized by extensions.
     * 
     * @param model2
     * @throws IOException
     */
    public Model readInto(Model model) throws IOException
    {
        for (final String fileName : input)
        {
            if (new File(fileName).isDirectory())
                for (File file : new File(fileName).listFiles())
                    loadIntoModel(model, file);
            else
                loadIntoModel(model, new File(fileName));
        }
        return model;
    }

    private void loadIntoModel(Model model, final File file) throws IOException
    {
        String language = FileUtils.guessLang(file.toURI().toURL().toString(), null);
        if (language != null)
        {
            final InputStream inputStream = new FileInputStream(file);
            try
            {
                model.read(new FileInputStream(file), (String) null, language);
            }
            finally
            {
                inputStream.close();
            }
        }
    }

    public void write(final Model model) throws MalformedURLException, FileNotFoundException
    {
        final String language = FileUtils.guessLang(getOutput().toURI().toURL().toString(), null);
        if (language == null)
            throw new IllegalArgumentException("output extension not supported: " + getOutput());
        final PrintStream outputStream = new PrintStream(getOutput());
        try
        {
            model.write(outputStream, language.replaceAll("^RDF/XML$", "RDF/XML-ABBREV"));
        }
        finally
        {
            outputStream.close();
        }
    }

    public File getMandatoryFile(String extension)
    {
        return getMandatory(extension);
    }

    public Model readGedcom() throws IOException, GedcomParserException
    {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(getOptional("ged")));
        try
        {
            return new Parser().parse(inputStream);
        }
        finally
        {
            inputStream.close();
        }
    }

    public List<File> getRuleFiles()
    {
        final List<File> result = new ArrayList<File>();
        for (final String fileName : input)
        {
            if (new File(fileName).isDirectory())
            {
                for (File file : new File(fileName).listFiles())
                    if (isRuleFile(file.getName()))
                        result.add(file);
            }
            else
            {
                if (isRuleFile(fileName))
                    result.add(new File(fileName));
            }
        }
        return result;
    }

    private boolean isRuleFile(final String fileName)
    {
        return "rules".equals(fileName.replaceAll(".*[.]", "").toLowerCase());
    }

    public void write(final ResultSet resultSet) throws IOException, TransformerException, TransformerFactoryConfigurationError
    {
        Model model = resultSet.getResourceModel();
        final OutputStream outputStream = new FileOutputStream(getOutput());
        try
        {
            final String ext = getOutput().getName().replaceAll(".*[.]", "").toLowerCase();
            if ("tsv".equals(ext))
                ResultSetFormatter.outputAsTSV(outputStream, resultSet);
            else if ("csv".equals(ext))
                ResultSetFormatter.outputAsCSV(outputStream, resultSet);
            else if ("xml".equals(ext))
                ResultSetFormatter.outputAsXML(outputStream, resultSet);
            else if ("json".equals(ext))
                ResultSetFormatter.outputAsJSON(outputStream, resultSet);
            else if ("txt".equals(ext))
                outputAsText(resultSet, outputStream, model.getNsPrefixMap());
            else if (ext.matches("html?"))
                outputAsHtml(resultSet, outputStream);
            else
                throw new IllegalArgumentException("output extension not supported: " + getOutput());
        }
        finally
        {
            outputStream.close();
        }
    }

    private void outputAsHtml(final ResultSet resultSet, final OutputStream outputStream) //
            throws TransformerException, TransformerFactoryConfigurationError, IOException
    {
        final ByteArrayOutputStream xml = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsXML(xml, resultSet);
        final StreamSource xmlSS = new StreamSource(new ByteArrayInputStream(xml.toByteArray()));
        final StreamSource xslSS = new StreamSource(getMandatory("xsl").toURI().toURL().openStream());
        final Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSS);
        transformer.transform(xmlSS, new StreamResult(outputStream));
    }

    private static void outputAsText(final ResultSet resultSet, final OutputStream outputStream, final Map<String, String> map) //
            throws IOException
    {
        final Prologue prologue = new Prologue(PrefixMapping.Factory.create().setNsPrefixes(map));
        outputStream.write(ResultSetFormatter.asText(resultSet, prologue).getBytes());
    }

    private File getOptional(final String extension)
    {
        for (final String fileName : input)
            if (extension.equals(fileName.replaceAll(".*[.]", "").toLowerCase()))
                return new File(fileName);
        return null;
    }

    private File getMandatory(final String extension)
    {
        final File file = getOptional(extension);
        if (file == null)
            throw new IllegalArgumentException("missing argument: " + extension);
        return file;
    }

    public File getOutput()
    {
        return output;
    }
}
