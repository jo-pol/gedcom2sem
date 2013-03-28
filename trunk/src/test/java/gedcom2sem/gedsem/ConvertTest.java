// @formatter:off
/*
 * Copyright 2013, J. Pol
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

import org.junit.Test;

public class ConvertTest
{

    @Test(expected = IllegalArgumentException.class)
    public void noArgs() throws Exception
    {
        Convert.main((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyArgs() throws Exception
    {
        Convert.main(new String[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongOption() throws Exception
    {
        Convert.main(new String[] {"/"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void noGedcom() throws Exception
    {
        Convert.main(new String[] {"target/out.ttl"});
    }
}
