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

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.vocabulary.*;

public enum Prefix
{
    rdf (RDF.getURI()), //
    rdfs(RDFS.getURI()), //
    xsd (XSD.getURI()), //
    owl (OWL.getURI()), //
    ;
    public final String uri;
    public static final Map<String, String> NAME_URI_MAP = new HashMap<String, String>();
    static
    {
        for (Prefix p : values())
            NAME_URI_MAP.put(p.name(), p.uri);
    }

    private Prefix(final String uri)
    {
        this.uri = uri;
    }
}
