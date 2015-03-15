# Notes on the configuration files #
The configuration files are depicted with
![](https://cdn.rawgit.com/wiki/jo-pol/gedcom2sem/wiki-images/overview/config.jpg)
on the [overview](overview).
Examples are available in the conf folder of the download.
To get the latest versions download raw files from the
[resources](https://github.com/jo-pol/gedcom2sem/tree/master/src/test/resources).

# .arq #
The .arq files contain
[SPARQL](http://en.wikipedia.org/wiki/SPARQL) queries to generate reports.
Currently the queries need output of the convert command. The comment headers specify the details.

Some of these files contain placeholders: `'%s'`.
The placeholders should be replaced with the ID of an entity, for example `'@I123@'`. The original plugins for [GenJ](http://genj.sourceforge.net/)
and
[Ancestris](http://www.ancestris.org/)
perform the replacements on the flight when a report is launched from the context of an entity.
The library needs clones of these files with hard coded IDs.

# .rules #
The library provides two sets of rule files.
The rules enhance the initial conversion of
[gedcom](http://en.wikipedia.org/wiki/GEDCOM)
tags that are turned into predicates and classes.
The .rules depicted for `convert` don't produce sensible output with  `transform`. The result of `convert` is not intended for publication on the web. When [customizing](OntologiesAndRules.md) rules for `transform`, it can be convenient to test them with `convert`.

Some rules need the result of others what makes the order of these files significant.

Some rules files are not depicted as configuration files, they are bound by the data in the gedcom file.

# .html .xsl #
.html as output makes .xsl mandatory input, the provided
[example](https://github.com/jo-pol/gedcom2sem/tree/master/src/main/resources/result-to-html.xsl)
creates a straightforward table.

# .kml .properties #
KML output contains HTML snippets. These snippets are configured in properties files. Placeholders in the snippets are replaced with query results.

The kml-xxx.arq files can also be used with the `select` command, while customizing the output it can be convenient to first test the results of the query.

Note that you may also host a KML map on you own website, search the web for how-to's.