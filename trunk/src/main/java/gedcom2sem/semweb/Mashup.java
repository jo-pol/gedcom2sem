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

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import gedcom2sem.io.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Manager of data from the semantic web related to place name literals.
 * 
 * @author Jo Pol
 */
public class Mashup
{

    private static final Logger logger = Logger.getLogger(Mashup.class.getName());

    private final DownloadManager downloadManager;
    private final QueryUtil queryUtil;

    private final String rdfLanguage;
    private final File file;
    private final Model model;

    /** links the literal value with the downloaded resources */
    private final String uriTemplate;

    private long flushCount;

    /**
     * Creates an object to download and link data from the semantic web for place name literals.
     * 
     * @param uriTemplate
     *        used to construct new URIs.
     * @param file
     *        a file with one of the extension rdf/n3/ttl. External changes to the file between and
     *        during method invocations of this instance will be overwritten. Downloads are not refreshed
     *        for an existing file.
     * @param languages
     *        domain name prefixes for dbpedia separated with "|". For example "fr" to get the resources
     *        from fr.dbpedia.org or "fr|de" to get the French and German resources. Null or empty to
     *        just fetch the English resources.
     * @throws IOException
     */
    public Mashup(final String uriTemplate, final File file, final String languages) throws IOException
    {
        this.file = file;
        this.uriTemplate = uriTemplate;

        rdfLanguage = FileUtil.guessLanguage(file);
        model = ModelFactory.createDefaultModel();
        if (file.exists())
            model.read(new FileInputStream(file), (String) null, rdfLanguage);
        else
            model.setNsPrefixes(Prefix.NAME_URI_MAP);
        queryUtil = new QueryUtil(model, languages);
        downloadManager = new DownloadManager(model, queryUtil);
    }

    /**
     * Downloads resources and links them to a literal. The following statements are created:
     * 
     * <pre>
     * &lt;ID&gt; rdf:label &lt;literal&gt;.
     * &lt;ID&gt; rdfs:isDefinedBy &lt;http://sws.geonames.org/VALUE&gt;.
     * &lt;http://sws.geonames.org/VALUE&gt; rdfs:seeAlso &lt;http://XX.dbpedia.org/YYY&gt;
     * </pre>
     * 
     * The &lt;ID&gt; is generated by extending the {@link URI} passed to the constructor. Unless already
     * present, the GeoNames resource is fetched together with same resources. DBPedia resources are
     * fetched when the GeoNames resource refers to it with seeAlso. The third type of statement is added
     * if the dbpedia resource refers to it with a sameAs and XX is one of the desired languages.
     * 
     * @param place
     *        place name literal. For gedcom files typically<br>
     *        the unique values of INDI:*:PLAC and/or FAM:*:PLAC<br>
     *        for example ", Washington, , , , DC, USA"<br>
     * @param geoNameId
     *        the numeric component of a URI (for example http://sws.geonames.org/4140963) or URL (for
     *        example http://sws.geonames.org/4140963/about.rdf)
     * @return downloaded DbPedia resources
     * @throws URISyntaxException
     * @throws IOException
     * @throws UnsupportedEncodingException
     *         if the operating system does not support UTF-8 to decode dbpedia URI's
     */
    public void addPlaceResources(final String place, final String geoNameId) throws URISyntaxException, IOException, UnsupportedEncodingException
    {
        flush(false);
        logger.info(place);
        final String geoNameUri = "http://sws.geonames.org/" + geoNameId + "/";
        final Resource subject = createResource(MessageFormat.format(uriTemplate, geoNameId));
        model.add(subject, RDFS.label, place);
        for (final String gnUri : downloadManager.downloadGeoNames(geoNameUri))
        {
            model.add(subject, RDFS.isDefinedBy, createResource(gnUri));
            for (final String dbpUri : queryUtil.getProperties(gnUri, RDFS.seeAlso, "dbpedia.org"))
            {
                model.add(subject, RDFS.seeAlso, createResource(dbpUri));
                for (final String uri : queryUtil.getSameDbpediaResources(dbpUri))
                    model.add(subject, RDFS.seeAlso, createResource(uri));
            }
        }
    }

    public void flush(final boolean force) throws FileNotFoundException
    {
        if (force || flushCount < model.size())
        {
            model.write(new FileOutputStream(file), rdfLanguage);
            flushCount = model.size();
        }
    }

    public static void main(final String... fileNames) throws IOException, URISyntaxException
    {
        File inputFile = null;
        String uri = null;
        File outputFile = null;
        String dbpediaLanguages = null;

        for (final String fileName : fileNames)
        {
            if (fileName.toLowerCase().endsWith(".tsv"))
            {
                inputFile = new File(fileName);
            }
            else if (fileName.toLowerCase().endsWith(".properties"))
            {
                final PropertyResourceBundle properties = new PropertyResourceBundle(new FileInputStream(fileName));
                uri = properties.getString("PLACE");
                new URI(MessageFormat.format(uri, "xyz"));// verification
                dbpediaLanguages = properties.getString("dbPedia.languages");
            }
            else
            {
                outputFile = new File(fileName);
                FileUtil.guessLanguage(outputFile);
            }
        }
        if (outputFile == null)
            throw new IllegalArgumentException("no output file (.ttl, .n3, .nt, .rdf)");
        if (inputFile == null)
            throw new IllegalArgumentException("no input file (.tsv)");
        if (uri == null)
            throw new IllegalArgumentException("no configuration file (.properties)");

        final Mashup mashup = new Mashup(uri, outputFile, dbpediaLanguages);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        try
        {
            logger.info("started");
            parse(mashup, reader);
        }
        catch (final Exception e)
        {
            reader.close();
        }
        mashup.flush(true);
        mashup.downloadManager.logOverwiew();
        logger.info(mashup.model.size() + " statements in "+outputFile);
    }

    private static void parse(final Mashup mashup, final BufferedReader reader) throws IOException, URISyntaxException, UnsupportedEncodingException
    {
        for (String line; (line = reader.readLine()) != null;)
        {
            final String[] fields = line.split("\t");
            if (fields.length > 1)
            {
                final String geoNameId = fields[0].replaceAll("[^0-9]*", "");
                final String placeLiteral = fields[1].replaceFirst("^\"", "").replaceAll("\"$", "").trim();

                if (geoNameId.length() > 0 && placeLiteral.length() > 0)
                    mashup.addPlaceResources(placeLiteral, geoNameId);
            }
        }
    }
}
