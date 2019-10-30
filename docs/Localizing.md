# The code #
The code is not yet designed to support multiple languages for error messages nor for log messages.

# The Reports #

## .arq files ##

Dates and names are stored in various formats. Queries would become too complex and slow to cater for all these and other variations. Once a query is forked for a translation, each variant would evolve along its own route like wikipedia articles do.


## KML ##

The KML output is localized with [kml-XXX.properties](https://github.com/jo-pol/gedcom2sem/tree/master/src/main/resources). The file may contain further customizations and forks suffer the same fate as described for queries. Note that customizations should stay synchronized with [places-XXX.arq](https://github.com/jo-pol/gedcom2sem/tree/master/src/main/resources/reports/mashup)

## Headers ##

Column headers of query results can be customized in a clone of the query or with a style sheet.

### With a stye sheet ###
XML output of a query can be formatted as HTML (or whatever you want) with a style sheet. The style sheet should be published in the same domain as the xml. For a simple table layout in HTML format you can use a copy of [result-to-html.xsl](https://github.com/jo-pol/gedcom2sem/tree/master/src/main/resources/result-to-html.xsl)

For localization we need a style sheet per query per language. Submit the style sheets with the base name of the query extended with the two letter ISO-639 code for the language.
For example `CountEventsPerPlace.arq` would get a `CountEventsPerPlace.fr.xsl`

The general style sheet contains something like: `<xsl:for-each ...><th>...</th></xsl:for-each>` replace the complete code snippet with localized column headings, for example: `<th>Name</th><th>ID</th><th>Birth date</th>` You might also want to change the `<title>` and `<h1>`.

### In a clone of a query ###
When you are customizing a query anyhow, you can correct the column names in your local copy of the .arq file.

Between SELECT and the first "{", you will see constructs like `<expression>` or like (`<expression> as ?columnName`). By convention one per line. You can change the former into the latter, and you can choose your own column names. Choose the names with care, they should not occur anywhere else in the query. When you save a query as UTF-8 to cater for proper column names, the file should not be saved with a BOM (Byte order mark) such as Windows' Notepad does.
