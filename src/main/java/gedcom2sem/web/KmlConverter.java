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
package gedcom2sem.web;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public class KmlConverter
{
    // revision 3669 leads to AncestrisExtensions/libs.jena/ and AncestrisExtensions/libs.apache.log4j
    // need something similar for
    // http://repo1.maven.org/maven2/de/micromata/jak/JavaAPIforKml/2.2.0/JavaAPIforKml-2.2.0.jar
    // which in turn depend on jaxb-impl 2.2; jaxb-xjc 2.2; jaxb-api 2.2; stax-api 2.2 activation 1.1;

    private final ResourceBundle properties;
    private final Map<String, String[]> indis = new TreeMap<String, String[]>(getComparator());
    private final Map<String, Map<String, String[]>> branches = new TreeMap<String, Map<String, String[]>>(getComparator());

    private Comparator<String> getComparator()
    {
        return new Comparator<String>()
        {
            public int compare(final String o1, final String o2)
            {
                return createBrancheName(o1).compareTo(createBrancheName(o2));
            }
        };
    }

    private void read(final BufferedReader reader) throws IOException
    {
        for (String line = reader.readLine(); line != null; line = reader.readLine())
        {
            if (line.startsWith("?"))
                continue; // skip header
            final String[] f = split(line);
            final String brancheId = f[0];
            final String sosa = f[1];

            if (!toBinaryString(brancheId).startsWith(toBinaryString(sosa)))
                continue;// one path in case of inbreed
            if (!branches.containsKey(brancheId))
                branches.put(brancheId, new TreeMap<String, String[]>());

            branches.get(brancheId).put(sosa, f);
            indis.put(sosa, f);
        }
        reader.close();
    }

    private void buildLines(final Folder folder) throws MissingResourceException
    {
        int nrOfBranches = 0;
        Set<String> topIndis = new HashSet<String>();
        for (final String brancheId : branches.keySet())
        {
            final String[] top = branches.get(brancheId).get(brancheId);
            if (top==null || top[6] == null || top[6].equals(""))
                continue;
            
            nrOfBranches++;
            topIndis.add(top[top.length-1]);
            
            final StringBuffer description = new StringBuffer();
            final Placemark placeMark = folder.createAndAddPlacemark().withName(createBrancheName(brancheId)).withOpen(false);
            final LineString lineString = placeMark.createAndSetLineString();
            for (final String sosa : branches.get(brancheId).keySet())
            {
                final String[] f = branches.get(brancheId).get(sosa);
                if (f[2].trim().length() > 0 && f[3].trim().length() > 0)
                {
                    final float latitude = Float.valueOf(f[2]);
                    final float longitude = Float.valueOf(f[3]);
                    lineString.addToCoordinates(longitude, latitude);
                }
                description.append(format("indi.html.element", f));
            }
            placeMark.withDescription(format("branche.html.container", description.toString()));
            placeMark.withSnippetd(format("branche.text", top));
        }
        folder.withName(nrOfBranches+" takken, "+topIndis.size()+" unieke bladeren");
    }

    private void buildTarget(final Placemark placeMark) throws MissingResourceException
    {
        final String[] spouseA = indis.get("00000002");
        final String[] spouseB = indis.get("00000003");
        final float latitude = Float.valueOf(spouseA[2]);
        final float longitude = Float.valueOf(spouseA[3]);
        final StringBuffer description = new StringBuffer();
        description.append(format("spouse.html", spouseA));
        description.append(format("spouse.html", spouseB));
        placeMark.createAndSetPoint().addToCoordinates(longitude, latitude);
        placeMark.withName(format("target.text", spouseA));
        placeMark.withDescription(description.toString());
    }

    private String[] split(final String line)
    {
        final String[] f = line.split("\t");
        for (int i = 0; i < f.length; i++)
            f[i] = f[i].trim().replaceAll("^\"", "").replaceAll("\"$", "").trim();
        return f;
    }

    @SuppressWarnings("unused")
    private class Item
    {
        int branche;
        int sosa;
        float latitude;
        float longitude;
        final String[] fields;

        Item(final String line)
        {
            fields = line.split("\t");
            for (int i = 0; i < fields.length; i++)
                fields[i] = fields[i].trim().replaceAll("^\"", "").replaceAll("\"$", "").trim();
            branche = Integer.valueOf(fields[0]);
            sosa = Integer.valueOf(fields[1]);
            latitude = Float.valueOf(fields[2]);
            longitude = Float.valueOf(fields[3]);
        }
    }

    private String format(final String templateKey, final String... args) throws MissingResourceException
    {
        final String template = properties.getString(templateKey);
        return MessageFormat.format(template, (Object[]) args);
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

    public KmlConverter(final ResourceBundle properties)
    {
        this.properties = properties;
    }

    public void tsvToKml(final InputStream in, final OutputStream out) throws IOException
    {
        final Kml kml = KmlFactory.createKml();
        read(new BufferedReader(new InputStreamReader(in)));
        Folder rootFolder = kml.createAndSetFolder().withOpen(true);
        buildTarget(rootFolder.createAndAddFolder().withName("*").createAndAddPlacemark());
        buildLines(rootFolder.createAndAddFolder());
        kml.marshal(out);
    }

    public static void main(final String[] args) throws IOException
    {
        final ResourceBundle properties = new PropertyResourceBundle(new FileInputStream(args[0]));
        new KmlConverter(properties).tsvToKml(System.in, System.out);
    }
}
