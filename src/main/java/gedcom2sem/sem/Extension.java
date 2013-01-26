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
package gedcom2sem.sem;

import java.io.File;

 enum Extension
{
    ttl("TURTLE"), n3("N3"), nt("N-TRIPPLE"), rdf("RDF/XML-ABBREV");
    private String language;

    private Extension(final String language)
    {
        this.language = language;
    }

    public String language()
    {
        // http://jena.apache.org/documentation/io/iohowto.html

        // The built-in languages are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "N3" and "TURTLE". In
        // addition, for Turtle output the language can be specified as: "N3-PP", "N3-PLAIN" or
        // "N3-TRIPLE", which controls the style of N3 produced.

        // For output, "RDF/XML", produces regular output reasonably efficiently, but it is not readable.
        // In contrast, "RDF/XML-ABBREV", produces readable output without much regard to efficiency.

        return language;
    }

    public static Extension valueOf(final File file)
    {
        try
        {
            return valueOf(file.getName().replaceAll(".*\\.", "").toLowerCase());
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
