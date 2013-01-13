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
import java.util.*;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * Manager of downloads from the semantic web.
 * 
 * @author Jo Pol
 */
public class QueryUtil
{
    private static final Logger logger = Logger.getLogger(QueryUtil.class.getName());

    private final Model model;
    private final String dbpediaFilter;

    public QueryUtil(final Model model, final String languages) throws IOException
    {
        if (model == null)
            throw new IllegalArgumentException("model should not be null");
        this.model = model;

        if (languages == null || languages.length() == 0)
            dbpediaFilter = "/dbpedia.org";
        else
            dbpediaFilter = "/((" + languages + ")[.])?dbpedia.org";
    }

    /**
     * Get specific objects for a subject.
     * 
     * @param subject
     *        the resource URI for which properties are searched
     * @param predicate
     *        the type of property searched for
     * @param objectRegEx
     *        filter for the searched objects
     * @return
     */
    public Set<String> getProperties(final String subject, final Property predicate, final String objectRegEx)
    {
        final String format = "select distinct ?n {<%s> <%s> ?n. FILTER regex(str(?n),'%s')}";
        final String q = String.format(format, subject, predicate.getURI(), objectRegEx);
        return queryModel(q);
    }

    /**
     * Gets DbPedia resources related with owl:sameAs. The desired language variants are specified at
     * construction time of this object instance.
     * 
     * @param uri
     *        typically a DbPedia resource
     * @return URI's of DbPedia resources
     */
    public Set<String> getSameDbpediaResources(final String uri)
    {
        // TODO ??? http://fr.dbpedia.org/ontology/wikiPageInterLanguageLink
        final String format = "select distinct ?n {<%s> <%s> ?n. FILTER regex(str(?n),'%s')}";
        final String q = String.format(format, uri, OWL.sameAs.getURI(), dbpediaFilter);
        return queryDbpedia(q);
    }

    /**
     * Gets DbPedia person related to a place and living in a period. The desired language variants are
     * specified at construction time of this object instance.
     * 
     * @param placeURI
     *        DbPedia resource of a place
     * @param from
     *        start of the period (inclusive)
     * @param to
     *        end of the period (exclusive)
     * @param limit TODO
     * @return URI's of DbPedia resources of persons
     */
    public static Set<String> getPersons(final String placeURI, final String from, final String to, final int limit)
    {
        final String dateRegex = "\\d{4}[-./]\\d{2}[-./]\\d{2}";
        if (!from.matches(dateRegex)||!to.matches(dateRegex))
        {
            logger.warn("illegal date: "+placeURI+" - "+from+" - "+to);
            return new HashSet<String>();
        }
        final StringBuffer sb = new StringBuffer();
        sb.append(" PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>");
        sb.append(" PREFIX foaf: <http://xmlns.com/foaf/0.1/>");
        sb.append(" PREFIX dbo: <http://dbpedia.org/ontology/>");
        sb.append(" SELECT DISTINCT ?person");
        sb.append(" {");
        sb.append(" ?person ?predicate <%s> .");
        sb.append(" FILTER (!regex(str(?predicate),'(monument|building|states)','i')).");
        sb.append(" ?person dbo:birthDate ?birth .");
        sb.append(" ?person dbo:deathDate ?death .");
        sb.append(" FILTER");
        sb.append(" (  (?birth >= '%s'^^xsd:date && ?birth < '%s'^^xsd:date)");
        sb.append(" || (?death >= '%s'^^xsd:date && ?death < '%s'^^xsd:date)");
        sb.append(" ).");
        sb.append(" } ORDER BY ?birth LIMIT %s");
        return queryDbpedia(String.format(sb.toString(), placeURI, from, to, from, to, limit));
    }

    /**
     * Runs a SPARQL query with {@link Syntax.syntaxARQ}
     * 
     * @param query
     * @return the first column of the query result
     */
    public Set<String> runQuery(final String query)
    {
        return queryModel(query);
    }

    private Set<String> queryModel(final String query)
    {
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
        return run(query, queryExecution);
    }

    private static Set<String> queryDbpedia(final String query)
    {
        Nice.sleep("http://dbpedia.org/sparql");
        final QueryExecution queryExecution = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
        return run(query, queryExecution);
    }

    private static Set<String> run(final String query, final QueryExecution queryExecution)
    {
        logger.debug("query: " + query);
        final Set<String> result = new HashSet<String>();
        try
        {
            final ResultSet resultSet = queryExecution.execSelect();
            final List<String> columnnNames = resultSet.getResultVars();
            while (resultSet.hasNext())
            {
                final QuerySolution row = resultSet.next();
                result.add(row.get(columnnNames.get(0)).toString());
            }
        }
        finally
        {
            queryExecution.close();
        }
        if (!result.isEmpty())
            logger.info(Arrays.deepToString(result.toArray()));
        return result;
    }
}
