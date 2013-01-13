package gedcom2sem.web;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.PropertyResourceBundle;

import org.junit.Test;

public class KmlConverterTest
{
    @Test
    public void run() throws Exception
    {
        final InputStream inputStream = new FileInputStream("src/main/resources/KmlConverter.properties");
        final FileInputStream in = new FileInputStream("src/test/resources/migratielijnen.txt");
        final FileOutputStream out = new FileOutputStream("target/migratielijnen.kml");
        new KmlConverter(new PropertyResourceBundle(inputStream)).tsvToKml(in,out);
    }

}
