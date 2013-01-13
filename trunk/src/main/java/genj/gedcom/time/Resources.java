package genj.gedcom.time;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Resources
{
    public static ResourceBundle get()
    {
        try
        {
            // TODO add properties file
            return new PropertyResourceBundle(new FileInputStream(""));
        }
        catch (final IOException e)
        {
            return new ResourceBundle()
            {

                @Override
                public Enumeration<String> getKeys()
                {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                protected Object handleGetObject(final String key)
                {
                    return key;
                }
            };
        }
    }
}
