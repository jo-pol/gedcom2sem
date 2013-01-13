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

public class UriFormats
{
    public static final String DEFAULT_URI = "http://my.domain.com/gedcom/{0}.html";

    public String fam = DEFAULT_URI;
    public String indi = DEFAULT_URI;
    public String obje = DEFAULT_URI;
    public String note = DEFAULT_URI;
    public String repo = DEFAULT_URI;
    public String sour = DEFAULT_URI;
    public String subm = DEFAULT_URI;

    public Map<String, String> getURIs()
    {
        Map<String, String> uris;
        uris = new HashMap<String, String>();
        uris.put("FAM", fam);
        uris.put("INDI", indi);
        uris.put("OBJE", obje);
        uris.put("NOTE", note);
        uris.put("REPO", repo);
        uris.put("SOUR", sour);
        uris.put("SUBM", subm);
        uris.put("HEAD", DEFAULT_URI);
        uris.put("TRLR", DEFAULT_URI);
        uris.put("", DEFAULT_URI);
        uris.put(null, DEFAULT_URI);
        return uris;
    }
}
