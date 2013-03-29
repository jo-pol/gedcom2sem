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
package gedcom2sem.gedsem;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

class SemanticGedcomModel
{

    public static final String GEDCOM_TAGS = "http://purl.org/vocab/vnd/gedcom2sem.googlecode.com/2013-01-13/GedcomTags/";
    private static final String PREDICATE = GEDCOM_TAGS + "predicate#";
    public final static Map<String, String> PREFIXES = new HashMap<String, String>();
    static
    {
        PREFIXES.put("p", PREDICATE);
        PREFIXES.put("t", GEDCOM_TAGS + "type#");
        PREFIXES.put("r", GEDCOM_TAGS + "rule#");
        PREFIXES.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        PREFIXES.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        PREFIXES.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    }

    private final Model model = ModelFactory.createDefaultModel();
    private final Property valueProperty = model.createProperty(PREFIXES.get("rdfs") + "label");
    private final Property typeProperty = model.createProperty(PREFIXES.get("rdf") + "type");
    private final Property idProperty = model.createProperty(PREDICATE + "id");

    private final Map<String, Property> properties = new HashMap<String, Property>();
    private final Map<String, Resource> types = new HashMap<String, Resource>();

    public SemanticGedcomModel()
    {
        getModel().setNsPrefixes(PREFIXES);
    }

    private Resource toType(final String tag)
    {
        if (!types.containsKey(tag))
            types.put(tag, model.createResource(PREFIXES.get("t") + tag));
        return types.get(tag);
    }

    private Property toProperty(final String tag)
    {
        if (!properties.containsKey(tag))
            properties.put(tag, model.createProperty(PREFIXES.get("p") + tag));
        return properties.get(tag);
    }

    public Resource addProperty(final Resource resource, final String tag, final String value)
    {
        final Resource property = model.createResource(toType(tag));
        resource.addProperty(toProperty(tag), property);
        if (value != null && value.trim().length() > 0)
            property.addProperty(valueProperty, value);
        return property;
    }

    public void addLiteral(final Resource resource, final Object value)
    {
        resource.addLiteral(valueProperty, value);
    }

    public Model getModel()
    {
        return model;
    }

    public void connect(final Resource referrer, final String tag, Resource referred)
    {
        referrer.addProperty(toProperty(tag), referred);
    }

    public Resource getResource(final String id, final String tag)
    {
        final ResIterator subjects = (id == null ? null : model.listSubjectsWithProperty(toProperty("id"), id));
        if (subjects != null && subjects.hasNext())
            return subjects.next();

        // final String uri = ("http://localhost/" + tag + "#" + id).replaceAll("@", "");
        // final Resource referred = model.createResource(uri,toType(tag));
        // TODO check queries with anonymous IDs
        final Resource referred = model.createResource(new AnonId());
        referred.addProperty(typeProperty, toType(tag));

        if (id != null)
            referred.addLiteral(idProperty, id);
        return referred;
    }
}
