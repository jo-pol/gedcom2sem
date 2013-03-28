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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.Set;

import org.gedcom4j.parser.GedcomParserException;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

public class Convert
{
    public static void main(final String... files) throws Exception
    {
        execute(files);
    }

    static Set<Statement> execute(final String... files) throws IOException, FileNotFoundException, MalformedURLException, GedcomParserException
    {
        BufferedInputStream gedcomInputStream = null;
        String output = null;
        String language = null;
        String rules = null;

        if (files == null)
            throw new IllegalArgumentException("no files at all");
        for (final String file : files)
        {
            final String extension = file.replaceAll(".*[.]", "").toLowerCase();
            if ("txt".equals(extension))
                rules = FileUtil.read(new File(file));
            else if ("ged".equals(extension))
                gedcomInputStream = new BufferedInputStream(new FileInputStream(file));
            else
            {
                language = FileUtil.guessLanguage(new File(file));
                output = file;
            }
        }
        if (gedcomInputStream == null)
            throw new IllegalArgumentException("no .ged");
        if (output == null)
            throw new IllegalArgumentException("no output (.ttl, .n3, .nt, .rdf)");

        // execute

        final Model model = new Parser().parse(gedcomInputStream);
        model.write(new PrintStream(output), language); // flush in case rules take too long or too much heap space
        Set<Statement> originalStatements = model.listStatements().toSet();
        System.err.println("before rules: "+originalStatements.size());
        if (rules != null){
            Model infModel = applyRules(rules, model).write(new PrintStream(output), language);
            Set<Statement> statements = infModel.listStatements().toSet();
            System.err.println("after rules: "+infModel.listStatements().toList().size());
            statements.removeAll(originalStatements);
            System.err.println("inferred: "+statements.size());
            return statements;
        }
        else return null;
    }

    private static InfModel applyRules(final String rules, final Model model)
    {
        for (final String key : SemanticGedcomModel.PREFIXES.keySet())
            PrintUtil.registerPrefix(key, SemanticGedcomModel.PREFIXES.get(key));
        final GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setMode(GenericRuleReasoner.HYBRID);

        final InfModel infModel = ModelFactory.createInfModel(reasoner, model);
        infModel.prepare();
        return infModel;
    }
}
