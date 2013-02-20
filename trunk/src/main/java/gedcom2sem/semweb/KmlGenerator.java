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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Snippet;

@SuppressWarnings("deprecation")
// Snippet is deprecated, but how else can we set the required maxlines?
public class KmlGenerator
{
    private static final Logger logger = Logger.getLogger(KmlGenerator.class.getName());

    private final Map<String, KmlQueryRow> all = new HashMap<String, KmlQueryRow>();
    private final Map<String, KmlQueryRow> leaves = new TreeMap<String, KmlQueryRow>();
    private final ResourceBundle properties;

    /**
     * Creates an internal representation of the query results.
     * 
     * @param model
     *        the triple store
     * @param properties
     *        templates for labels and descriptions in the KML file. The place holders in the templates
     *        should match the columns of the query.
     * @param query
     *        required column names: sosa (mandatory values starting with a number) lat + long (optional
     *        float values)
     */
    public KmlGenerator(final Model model, final ResourceBundle properties, final String query)
    {
        this.properties = properties;
        if (model == null || properties == null || query == null || model.size() == 0 || properties.keySet().size() == 0)
            throw new IllegalArgumentException("no null or empty arguments");

        final QuerySolutionMap qsm = new QuerySolutionMap();
        logger.info("starting query");
        final ResultSet resultSet = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, qsm).execSelect();
        logger.info("processing query results");
        final List<String> resultVars = resultSet.getResultVars();
        while (resultSet.hasNext())
        {
            final KmlQueryRow row = new KmlQueryRow(resultVars, resultSet.next());
            all.put(row.sosa, row);
        }
        findLeaves();
        logger.info("constructor ready");
    }

    /**
     * Creates an internal representation of the query results.
     * 
     * @param report
     *        Tab separated lines. First column: mandatory content, starting with a sosa number. Second
     *        an third column: latitude respective longitude (optional, float values).
     * @param properties
     *        templates for labels and descriptions in the KML file. The place holders in the templates
     *        should match the columns of the query.
     * @throws IOException
     */
    public KmlGenerator(final BufferedReader report, final ResourceBundle properties) throws IOException
    {
        this.properties = properties;
        if (report == null || properties == null || properties.keySet().size() == 0)
            throw new IllegalArgumentException("no null or empty arguments");

        String line;
        while (null != (line = report.readLine()))
        {
            if (!line.startsWith("?"))
            {
                final KmlQueryRow row = new KmlQueryRow(line.split("\t"));
                all.put(row.sosa, row);
            }
        }
        findLeaves();
        logger.info("constructor ready");
    }

    private void findLeaves()
    {
        if (all.size() == 0)
            throw new IllegalArgumentException("empty input");
        for (final KmlQueryRow row : all.values())
        {
            if (!all.containsKey(row.sosa + "0") && !all.containsKey(row.sosa + "1"))
                leaves.put(row.sosa, row);
        }
    }

    public void create(final File kmlFile) throws IOException, FileNotFoundException
    {
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument();
        addLineStyles(document);
        buildProbandParentsMarker(document.createAndAddFolder());
        buildMigrationLines(document.createAndAddFolder());
        kml.marshal(kmlFile);
    }

    /**
     * Creates lines from the proband to places of its ancestors.
     * 
     * @param folder
     *        gets a description and placemarks for the individuals without parents
     * @throws MissingResourceException
     *         when a property is missing in the resource specified at construction time
     */
    private void buildMigrationLines(final Folder folder) throws MissingResourceException
    {
        folder.withName(format("migration.folder.text", leaves.size() + ""));
        for (final String brancheId : leaves.keySet())
        {
            final String name = createBrancheName(brancheId);
            logger.info(name);
            final StringBuffer description = new StringBuffer();
            final Placemark placeMark = folder.createAndAddPlacemark().withName(name).withOpen(false);
            for (int l = brancheId.length(); l > 1; l--)
            {
                final String sosa = brancheId.substring(0, l);
                description.append(format("migration.ancestor.html", all.get(sosa).formatArgs));
                logger.info("description " + Arrays.toString(all.get(sosa).formatArgs));
            }
            final String snippetValue = format("migration.folder.item.text", all.get(brancheId).formatArgs);
            placeMark.withDescription(format("migration.popup.html", description.toString()));
            placeMark.withSnippet(new Snippet().withValue(snippetValue));
            placeMark.withStyleUrl("#" + (brancheId+"0000").substring(1, 5));

            final LineString lineString = placeMark.createAndSetLineString();
            for (int l = 2; l <= brancheId.length(); l++)
            {
                final String sosa = brancheId.substring(0, l);
                final KmlQueryRow row = all.get(sosa);
                if (row != null && row.longitude != null && row.latitude != null)
                {
                    lineString.addToCoordinates(row.longitude, row.latitude);
                    logger.info("line " + Arrays.toString(all.get(sosa).formatArgs));
                }
            }
        }
    }

    /**
     * Creates a marker for the proband's parents.
     * 
     * @param folder
     *        gets a description by the proband and one placmerker defined by its parents
     * @throws MissingResourceException
     *         when a property is missing in the resource specified at construction time
     */
    private void buildProbandParentsMarker(final Folder folder) throws MissingResourceException
    {
        final Placemark placemark = folder.createAndAddPlacemark();
        KmlQueryRow father = all.get("10");
        KmlQueryRow mother = all.get("11");
        KmlQueryRow proband = all.get("1");
        if (father == null || mother == null || proband == null||father.longitude==null|| father.latitude==null)
            return;
        final StringBuffer description = new StringBuffer();
        description.append(format("proband.father.html", father.formatArgs));
        description.append(format("proband.mother.html", mother.formatArgs));
        placemark.createAndSetPoint().addToCoordinates(father.longitude, father.latitude);
        placemark.withName(format("proband.marker.name", proband.formatArgs));
        placemark.withDescription(format("proband.popup.html", description.toString()));
        placemark.withSnippet(new Snippet().withValue(format("proband.marker.text", proband.formatArgs)));
        folder.withName(format("proband.folder.name", proband.formatArgs));
    }

    private void addLineStyles(final Document document)
    {
        final double width = Double.parseDouble(properties.getString("line.style.width"));
        for (final String key : properties.keySet())
        {
            if (key.startsWith("line.style.color."))
            {
                final String id = key.split("\\.", 4)[3];
                final String value = properties.getString(key);
                document.createAndAddStyle().withId(id).createAndSetLineStyle().withColor(value).withWidth(width);
            }
        }
    }

    private String createBrancheName(final String brancheId) throws MissingResourceException
    {
        final String m = properties.getString("mother.symbol");
        final String f = properties.getString("father.symbol");
        return brancheId.substring(1).replace("0", m).replace("1", f) + " " + Integer.parseInt(brancheId, 2);
    }

    private String format(final String templateKey, final String... args) throws MissingResourceException
    {
        final String template = properties.getString(templateKey);
        return MessageFormat.format(template, (Object[]) args);
    }

    public static void main(final String... files) throws UnsupportedEncodingException, IOException
    {
        final Model model = ModelFactory.createDefaultModel();
        ResourceBundle properties = null;
        String query = null;
        File kmlFile = null;
        BufferedReader report = null;

        if (files == null)
            throw new IllegalArgumentException("no files at all");
        for (final String file : files)
        {
            final String extension = file.replaceAll(".*[.]", "").toLowerCase();
            if ("kml".equals(extension))
                kmlFile = new File(file);
            else if ("tsv".equals(extension))
                report = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            else if ("properties".equals(extension))
                properties = new PropertyResourceBundle(new FileInputStream(file));
            else if ("arq".equals(extension))
                query = FileUtil.read(new File(file));
            else
                model.read(new FileInputStream(file), (String) null, FileUtil.guessLanguage(new File(file)));
        }
        if (kmlFile == null)
            throw new IllegalArgumentException("missing output (.kml)");
        if (properties == null)
            throw new IllegalArgumentException("missing formatting (.properties)");
        final KmlGenerator kmlGenerator;
        if (report != null)
            kmlGenerator = new KmlGenerator(report, properties);
        else
        {
            if (model.size() == 0)
                throw new IllegalArgumentException("no data (no or all empty .ttl, nt, .n3, .rdf)");
            if (query == null || query.trim().length() == 0)
                throw new IllegalArgumentException("no or empty query (.arq)");
            kmlGenerator = new KmlGenerator(model, properties, query);
        }
        kmlGenerator.create(kmlFile);
    }
}
