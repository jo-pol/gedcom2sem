package gedcom2sem.semweb;

import gedcom2sem.sem.Extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Snippet;

public class KmlGenerator
{
    private class Row
    {
        String         sosa;
        final Float    latitude;
        final Float    longitude;
        final String[] formatArgs;

        Row(final List<String> resultVars, final QuerySolution solution)
        {
            sosa = toBinaryString(solution.get("sosa").toString().replaceAll("[^0-9].*$", ""));
            latitude = getFloat(solution.get("lat"));
            longitude = getFloat(solution.get("long"));
            formatArgs = new String[resultVars.size()];
            for (int i = 0; i < resultVars.size(); i++)
            {
                final String columnName = resultVars.get(i);
                formatArgs[i] = getString(solution.get(columnName));
            }
        }

        private String getString(final RDFNode node)
        {
            return node == null ? "" : node.toString();
        }

        private Float getFloat(final RDFNode rdfNode2)
        {
            return rdfNode2 == null ? null : rdfNode2.asLiteral().getFloat();
        }
    }

    private final Map<String, Row> all    = new HashMap<String, Row>();
    private final Map<String, Row> leaves = new TreeMap<String, Row>();
    private final ResourceBundle   properties;

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
        final ResultSet resultSet = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, qsm).execSelect();
        final List<String> resultVars = resultSet.getResultVars();
        while (resultSet.hasNext())
        {
            final Row row = new Row(resultVars, resultSet.next());
            all.put(row.sosa, row);
        }
        for (final Row row : all.values())
        {
            if (!all.containsKey(row.sosa + "0") && !all.containsKey(row.sosa + "1"))
                leaves.put(row.sosa, row);
        }
    }

    /**
     * Creates lines from the proband to places of its ancestors.
     * 
     * @param folder
     *        gets a description and placemarks for the individuals without parents
     * @throws MissingResourceException
     *         when a property is missing in the resource specified at construction time
     */
    public void buildMigrationLines(final Folder folder) throws MissingResourceException
    {
        folder.withName(format("migration.folder.text", leaves.size() + ""));
        for (final String brancheId : leaves.keySet())
        {
            final StringBuffer description = new StringBuffer();
            final Placemark placeMark = folder.createAndAddPlacemark().//
                    withName(createBrancheName(brancheId)).withOpen(false);
            for (int l = brancheId.length(); l > 1; l--)
            {
                final String sosa = brancheId.substring(0, l);
                description.append(format("migration.ancestor.html", all.get(sosa).formatArgs));
            }
            final String snippetValue = format("migration.folder.item.text", all.get(brancheId).formatArgs);
            placeMark.withDescription(format("migration.popup.html", description.toString()));
            placeMark.withSnippet(new Snippet().withValue(snippetValue));
            // type snippet is deprecated, but how else can we set the required maxlines?

            final LineString lineString = placeMark.createAndSetLineString();
            for (int l = 2; l <= brancheId.length(); l++)
            {
                final String sosa = brancheId.substring(0, l);
                final Row row = all.get(sosa);
                if (row != null && row.longitude != null && row.latitude != null)
                    lineString.addToCoordinates(row.longitude, row.latitude);
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
    public void buildProbandParentsMarker(final Folder folder) throws MissingResourceException
    {
        final Placemark placemark = folder.createAndAddPlacemark();
        final float latitude = all.get("10").latitude;
        final float longitude = all.get("10").longitude;
        final StringBuffer description = new StringBuffer();
        description.append(format("proband.father.html", all.get("10").formatArgs));
        description.append(format("proband.mother.html", all.get("11").formatArgs));
        placemark.createAndSetPoint().addToCoordinates(longitude, latitude);
        placemark.withName(format("proband.marker.name", description.toString()));
        placemark.withDescription(format("proband.popup.html", description.toString()));
        placemark.withSnippet(new Snippet().withValue(format("proband.marker.text", all.get("1").formatArgs)));
        // type snippet is deprecated, but how else can we set the required maxlines?
    }

    private String createBrancheName(final String brancheId) throws MissingResourceException
    {
        final String m = properties.getString("mother.symbol");
        final String f = properties.getString("father.symbol");
        return brancheId.substring(1).replace("0", m).replace("1", f) + " " + Integer.parseInt(brancheId, 2);
    }

    private String toBinaryString(final String brancheId)
    {
        return Integer.toBinaryString(Integer.valueOf(brancheId));
    }

    private String format(final String templateKey, final String... args) throws MissingResourceException
    {
        final String template = properties.getString(templateKey);
        return MessageFormat.format(template, (Object[]) args);
    }

    public static void main(final String... files)
    {
        final Model model = ModelFactory.createDefaultModel();
        ResourceBundle properties = null;
        String query = null;
        for (final String file : files)
        {
            final String extension = file.replaceAll(".*[.]", "").toLowerCase();
            try
            {
                if ("kml".equals(extension))
                {
                    createKml(model, properties, new File(file), query);
                    return;
                }
                else if ("properties".equals(extension))
                    properties = new PropertyResourceBundle(new FileInputStream(file));
                else if (file.toLowerCase().endsWith(".arq"))
                    query = readFile(new File(file));
                else
                    model.read(new FileInputStream(file), (String) null, Extension.valueOf(extension).language());
            }
            catch (final Exception e)
            {
                e.printStackTrace(System.err);
            }
        }
        showUsage();
    }

    private static String readFile(final File file) throws FileNotFoundException, IOException
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
        return new String(bytes, "UTF8");
    }

    private static void createKml(final Model model, final ResourceBundle properties, final File kmlFile, final String file) throws IOException,
            FileNotFoundException
    {
        final Kml kml = KmlFactory.createKml();
        final Folder rootFolder = kml.createAndSetFolder().withOpen(true);
        final KmlGenerator migrationKml = new KmlGenerator(model, properties, file);
        migrationKml.buildProbandParentsMarker(rootFolder.createAndAddFolder());
        migrationKml.buildMigrationLines(rootFolder.createAndAddFolder());
        kml.marshal(kmlFile);
    }

    private static void showUsage()
    {
        System.err.println();
        System.err.println("expected filenames in random order: .properties .kml .ttl, .nt, .n3, .rdf");
        System.err.println("the last file shoul be a sparql query with extension .arq");
        System.err.println("only the last is used for .properties and .kml");
    }
}
