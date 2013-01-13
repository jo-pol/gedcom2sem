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

import gedcom2sem.sem.Extension;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

// import org.junit.Test;

public class Convert
{

    private static final String LS = System.getProperty("line.separator");

    private static final String HELP = "=== OPTIONS === " + Arrays.deepToString(Option.values()) + //
            LS + "rules  : filename" + //
            LS + "format : default ttl; possible values: " + Arrays.deepToString(Extension.values()) + //
            LS + "uri    : default for the options FAM, INDI, OBJE, NOTE, REPO, SOUR, SUBM" + //
            LS + "         default for uri is " + UriFormats.DEFAULT_URI + //
            LS + "gedcom : filename, only preceding options are applied to the conversion";

    private static enum Option
    {
        rules, format, uri, FAM, INDI, OBJE, NOTE, REPO, SOUR, SUBM, gedcom
    };

    public static void main(final String... args) throws Exception
    {

        if (args == null || args.length == 0)
            throw createException("missing arguments");

        String qRules = null;
        final UriFormats uriFormats = new UriFormats();
        String language = Extension.ttl.language();
        Logger.getLogger("").setLevel(Level.OFF);

        for (int i = 0; i < args.length; i++)
        {
            final Option option = toOption(args[i]);
            final String value = args[++i];
            switch (option)
            {
            case format:
                language = toLanguage(value);
                break;
            case gedcom:
                final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(value));
                final Model model = new Parser().parse(inputStream, uriFormats.getURIs());
                model.write(System.out, language);
                System.err.println("parsing done");
                if (qRules != null)
                {
                    System.err.println("applying rules");
                    applyRules(qRules, model).write(System.out, language);
                    System.err.println("rules done");
                }
                break;
            case rules:
                qRules = read(value);
                break;
            case FAM:
                uriFormats.fam = chekURI(value);
                break;
            case INDI:
                uriFormats.indi = chekURI(value);
                break;
            case OBJE:
                uriFormats.obje = chekURI(value);
                break;
            case NOTE:
                uriFormats.note = chekURI(value);
                break;
            case REPO:
                uriFormats.repo = chekURI(value);
                break;
            case SOUR:
                uriFormats.sour = chekURI(value);
                break;
            case SUBM:
                uriFormats.subm = chekURI(value);
                break;
            case uri:
                uriFormats.subm = chekURI(value);
                uriFormats.fam = chekURI(value);
                uriFormats.indi = chekURI(value);
                uriFormats.obje = chekURI(value);
                uriFormats.note = chekURI(value);
                uriFormats.repo = chekURI(value);
                uriFormats.sour = chekURI(value);
                break;
            }
        }
    }

    private static InfModel applyRules(final String rules, final Model model)
    {

        for (final String key : SemanticGedcomModel.PREFIXES.keySet())
        {
            PrintUtil.registerPrefix(key, SemanticGedcomModel.PREFIXES.get(key));
        }
        final GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setMode(GenericRuleReasoner.HYBRID);

        final InfModel infModel = ModelFactory.createInfModel(reasoner, model);
        infModel.prepare();
        return infModel;
    }

    private static String chekURI(final String value) throws URISyntaxException
    {
        try
        {
            new URI(MessageFormat.format(value, "123"));
            return value;
        }
        catch (final URISyntaxException e)
        {
            throw createException(value + " " + e.getMessage());
        }
    }

    private static String read(final String fileName) throws IOException
    {
        final File file = new File(fileName);
        if (!file.exists())
            throw createException(fileName + " does not exist");
        if (file.isDirectory())
            throw createException(fileName + " should be plain text file but is a directory");
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
        return new String(bytes);
    }

    private static IllegalArgumentException createException(final String string)
    {

        System.err.println(HELP);
        return new IllegalArgumentException(string);
    }

    private static String toLanguage(final String value)
    {
        try
        {
            return Extension.valueOf(value).language();
        }
        catch (final IllegalArgumentException e)
        {
            throw createException("invalid value for " + Option.format + ": " + value);
        }
    }

    private static Option toOption(final String string)
    {

        if (!string.startsWith("-"))
        {
            throw createException("unkown option: " + string);
        }
        try
        {
            return Option.valueOf(string.substring(1));
        }
        catch (final IllegalArgumentException e)
        {
            throw createException("unkown option: " + string);
        }
    }
}
