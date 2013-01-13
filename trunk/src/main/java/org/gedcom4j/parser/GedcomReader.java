package org.gedcom4j.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

import org.gedcom4j.model.StringTree;

public class GedcomReader
{
    public static List<StringTree> read(BufferedInputStream stream) throws IOException{
        return GedcomParserHelper.readStream(stream).children;
    }
}
