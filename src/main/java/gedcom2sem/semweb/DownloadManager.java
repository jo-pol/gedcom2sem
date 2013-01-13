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

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.*;

/**
 * Manager of downloads from the semantic web.
 * 
 * @author Jo Pol
 */
public class DownloadManager
{
    private static final Logger logger = Logger.getLogger(DownloadManager.class.getName());

    private final Set<String> read = new HashSet<String>();
    private final Set<String> warnings = new HashSet<String>();
    private final Set<String> tried = new HashSet<String>();
    private final Set<String> found = new HashSet<String>();
    private final QueryUtil queryUtil;
    private final Model model;

    public DownloadManager(final Model model, final QueryUtil queryUtil) throws IOException
    {
        if (model == null)
            throw new IllegalArgumentException("model should not be null");

        final String pfx = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ";
        final String p1 = "?l rdfs:label ?p;rdfs:isDefinedBy ?gn.";
        final String p2 = p1 + "?gn rdfs:seeAlso ?dbp.";
        final String f = "FILTER(!regex(str(?pr),'sameAs'))";
        found.addAll(queryUtil.runQuery(pfx + "?gn  {" + p1 + "?gn ?pr ?x.}"));
        found.addAll(queryUtil.runQuery(pfx + "?dbp {" + p2 + "?dbp ?pr ?x." + f + "}"));
        found.addAll(queryUtil.runQuery(pfx + "?dbp {" + p2 + "?x ?pr ?dbp." + f + "}"));

        this.queryUtil = queryUtil;
        this.model = model;
    }

    public Set<String> downloadGeoNames(final String uri) throws URISyntaxException, IOException
    {
        if (tried.contains(uri))
            return new HashSet<String>();
        download(uri, uri + "about.rdf");
        tried.add(uri);
        final Set<String> same = queryUtil.getProperties(uri, OWL.sameAs, "geonames.org");
        for (final String uri2 : same)
            downloadGeoNames(uri2);
        same.add(uri);
        return same;
    }

    public Set<String> downloadDbPedia(final String uri) throws URISyntaxException, UnsupportedEncodingException
    {
        if (tried.contains(uri))
            return new HashSet<String>();
        download(uri, toDbpediaUrl(uri));
        tried.add(uri);
        final Set<String> same = queryUtil.getSameDbpediaResources(uri);
        for (final String uri2 : same)
            downloadDbPedia(uri2);
        same.add(uri);
        return same;
    }

    private void download(final String uri, final String url) throws URISyntaxException
    {
        if (tried.contains(uri) || found.contains(uri))
            return;
        Nice.sleep(new URI(url).getHost());
        logger.info("reading: " + url);
        try
        {
            read.add(url);
            model.read(url);
        }
        catch (final JenaException e)
        {
            logger.warn(url + " " + e.getMessage());
            warnings.add(uri + " " + e.getMessage());
        }
    }

    private String toDbpediaUrl(final String uri) throws UnsupportedEncodingException
    {
        final String decoded = URLDecoder.decode(uri, "UTF-8");
        return decoded.replace("/resource/", "/data/") + ".rdf";
    }

    public void logOverwiew()
    {
        if (!read.isEmpty())
            logger.info("DOWNLOADED: " + Arrays.deepToString(read.toArray()));
        if (!warnings.isEmpty())
            logger.warn(Arrays.deepToString(warnings.toArray()));
    }
}
