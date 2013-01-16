package gedcom2sem.semweb;

import gedcom2sem.sem.Extension;

import java.io.File;
import java.io.FileInputStream;
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

public class GedKml
{
    private class Row
    {
        String sosa;
        final boolean isTop;
        final Float latitude;
        final Float longitude;
        final String[] formatArgs;

        Row(final List<String> resultVars, final QuerySolution solution)
        {
            sosa = toBinaryString(solution.get("sosa").toString().replaceAll("[^0-9].*$", ""));
            isTop = solution.get("isTop") == null;
            latitude = (solution.get("lat") == null ? null : solution.get("lat").asLiteral().getFloat());
            longitude = (solution.get("long") == null ? null : solution.get("long").asLiteral().getFloat());
            formatArgs = new String[resultVars.size()];
            for (int i = 0; i < resultVars.size(); i++)
            {
                final RDFNode node = solution.get(resultVars.get(i));
                formatArgs[i] = (node == null ? null : node.toString());
            }
        }
    }

    private final Map<String, Row> all = new HashMap<String, Row>();
    private final Map<String, Row> leaves = new TreeMap<String, Row>();
    private final ResourceBundle properties;

    public GedKml(final Model model, final ResourceBundle properties, final File query) throws IOException
    {
        this.properties = properties;
        if (model == null || properties == null || query == null || model.size() == 0 || properties.keySet().size() == 0)
            throw new IllegalArgumentException("no null or empty arguments");

        final ResultSet resultSet = executeSelect(model, query);
        final List<String> resultVars = resultSet.getResultVars();
        while (resultSet.hasNext())
        {
            final Row row = new Row(resultVars, resultSet.next());
            if (row.isTop)
                leaves.put(row.sosa, row);
            all.put(row.sosa, row);
        }
    }

    public void buildLines(final Folder folder) throws MissingResourceException
    {
        folder.withName(leaves.size() + " takken");
        for (final String brancheId : leaves.keySet())
        {

            final StringBuffer description = new StringBuffer();
            final Placemark placeMark = folder.createAndAddPlacemark().withName(createBrancheName(brancheId)).withOpen(false);
            final LineString lineString = placeMark.createAndSetLineString();
            for (String sosa = brancheId; sosa.length() > 2; sosa = sosa.substring(0, sosa.length() - 2))
            {
                final Row row = all.get(sosa);
                if (row.longitude != null && row.latitude != null)
                    lineString.addToCoordinates(row.longitude, row.latitude);
                description.append(format("indi.html.element", row.formatArgs));
            }
            placeMark.withDescription(format("branche.html.container", description.toString()));
            placeMark.withSnippetd(format("branche.text", all.get(brancheId).formatArgs));
        }
    }

    public void buildProbandMarker(final Placemark placeMark) throws MissingResourceException
    {
        final float latitude = all.get("10").latitude;
        final float longitude = all.get("10").longitude;
        final StringBuffer description = new StringBuffer();
        description.append(format("spouse.html", all.get("10").formatArgs));
        description.append(format("spouse.html", all.get("11").formatArgs));
        placeMark.createAndSetPoint().addToCoordinates(longitude, latitude);
        placeMark.withName(format("target.text", all.get("1").formatArgs));
        placeMark.withDescription(description.toString());
    }

    private String createBrancheName(final String brancheId) throws MissingResourceException
    {
        final String m = properties.getString("mother.symbol");
        final String f = properties.getString("father.symbol");
        return toBinaryString(brancheId).substring(1).replace("0", m).replace("1", f) + " " + Integer.valueOf(brancheId);
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

    private ResultSet executeSelect(final Model model, final File file) throws IOException
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
        return QueryExecutionFactory.create(q, Syntax.syntaxARQ, model, qsm).execSelect();
    }

    public static void main(final String... files) throws IOException
    {
        final Model model = ModelFactory.createDefaultModel();
        ResourceBundle properties = null;
        File kmlFile = null;
        for (final String file : files)
        {
            if (file.toLowerCase().endsWith(".properties"))
                properties = new PropertyResourceBundle(new FileInputStream(file));
            else if (file.toLowerCase().endsWith(".kml"))
                kmlFile = new File(file);
            else if (!file.toLowerCase().endsWith(".arq"))
                model.read(new FileInputStream(file), (String) null, Extension.valueOf(file.replaceAll(".*[.]", "")).language());
            else
            {
                try
                {
                    final Kml kml = KmlFactory.createKml();
                    final Folder rootFolder = kml.createAndSetFolder().withOpen(true);
                    final GedKml migrationKml = new GedKml(model, properties, new File(file));
                    migrationKml.buildLines(rootFolder.createAndAddFolder());
                    //FIXME migrationKml.buildProbandMarker(rootFolder.createAndAddFolder().withName("*").createAndAddPlacemark());
                    kml.marshal(kmlFile);
                    return;
                }
                catch (final IllegalArgumentException e)
                {
                    e.printStackTrace(System.err);
                    showUsage();
                }
            }
        }
        showUsage();
    }

    private static void showUsage()
    {
        System.err.println();
        System.err.println("expected filenames in random order: .properties .kml .ttl, .nt, .n3, .rdf");
        System.err.println("the last file shoul be a sparql query with extension .arq");
        System.err.println("only the last is used for .properties and .kml");
    }
}
