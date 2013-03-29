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
package gedcom2sem.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import com.hp.hpl.jena.util.FileUtils;

public class FileUtil
{
    /**
     * @see FileUtils#guessLang
     * @param file
     * @return null for unknown extension
     * @throws MalformedURLException
     */

    public static String guessLanguage(File file) throws MalformedURLException
    {
        String language = FileUtils.guessLang(file.toURI().toURL().toString(), null);
        if (language == null || language.length() == 0)
            throw new IllegalArgumentException("invalid extension (.ttl, .n3, .nt, .rdf) " + file);
        return language.replaceAll("^RDF/XML$", "RDF/XML-ABBREV");
    }

    /**
     * Reads the whole file.
     * 
     * @param file
     *        UTF-8 encoding assumed.
     * @return
     * @throws IOException
     */
    public static String read(final File file) throws IOException
    {
        final byte[] bytes = new byte[(int) file.length()];
        final FileInputStream inputStream = new FileInputStream(file);
        try
        {
            inputStream.read(bytes);
        }
        finally
        {
            inputStream.close();
        }
        return new String(bytes, "UTF-8");
    }

}
