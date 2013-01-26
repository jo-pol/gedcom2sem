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

import gedcom2sem.io.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

public class Convert
{
    public static void main(final String... files) throws Exception
    {
        final Properties uriFormats = new Properties();
        BufferedInputStream gedcomInputStream = null;
        PrintStream output = null;
        String language = null;
        String rules = null;

        if (files == null)
            throw new IllegalArgumentException("no files at all");
        for (final String file : files)
        {
            final String extension = file.replaceAll(".*[.]", "").toLowerCase();
            if ("properties".equals(extension))
                uriFormats.load(new FileInputStream(file));
            else if ("txt".equals(extension))
                rules = FileUtil.read(new File(file));
            else if ("ged".equals(extension))
                gedcomInputStream = new BufferedInputStream(new FileInputStream(file));
            else
            {
                language = FileUtil.guessLanguage(new File(file));
                output = new PrintStream(file);
            }
        }
        if (gedcomInputStream == null)
            throw new IllegalArgumentException("no .ged");
        if (uriFormats.size() == 0)
            throw new IllegalArgumentException("no or empty .properties");
        if (output == null)
            throw new IllegalArgumentException("no output (.ttl, .n3, .nt, .rdf)");

        // execute
        
        final Model model = new Parser().parse(gedcomInputStream, uriFormats);
        model.write(output, language); // flush in case rules take too long or too much heap space
        if (rules != null)
            applyRules(rules, model).write(output, language);
    }

    private static InfModel applyRules(final String rules, final Model model)
    {
        System.err.println("applying rules");
        for (final String key : SemanticGedcomModel.PREFIXES.keySet())
            PrintUtil.registerPrefix(key, SemanticGedcomModel.PREFIXES.get(key));
        final GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setMode(GenericRuleReasoner.HYBRID);

        final InfModel infModel = ModelFactory.createInfModel(reasoner, model);
        infModel.prepare();
        System.err.println("rules done");
        return infModel;
    }
}
