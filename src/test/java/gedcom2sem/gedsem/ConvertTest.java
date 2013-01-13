package gedcom2sem.gedsem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class ConvertTest
{

    @Test(expected = IllegalArgumentException.class)
    public void noArgs() throws Exception
    {
        Convert.main(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyArgs() throws Exception
    {
        Convert.main(new String[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongOption() throws Exception
    {
        Convert.main(new String[] {"-"});
    }
}
