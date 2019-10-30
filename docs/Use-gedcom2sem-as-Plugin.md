### From plugin to plugin?

The project evolved from a plugin 
in [GenJ]('http://genj.sourceforge.net/)
via [Ancestris](http://www.ancestris.org/)
into this library with a command line interface.
The original plug-in just provides customizable reports, without KML capabilities.

To simplify usage of the current library,
it could be plugged back into both applications and into other gedcom supporting applications and web services.
Plugin developers of genealogical applications and/or services can get started with the
[plugin tests](https://github.com/jo-pol/gedcom2sem/tree/master/src/test/java/plugin).
The methods of these JUnit tests provide code snippets for dialogs and other GUI controls.
See also the [overview](overview).

An export function for reports and semantic web files would be low hanging fruit,
adding RDFa to generated web pages a next step.

### RDFa versus Microdata
[Rumors](http://stackoverflow.com/questions/2986918/microformats-rdf-or-microdata)
have it that microdata replace RDFa. 
But [RDFa Lite](http://www.w3.org/TR/2012/REC-rdfa-lite-20120607/)
combines simplicity with improved support for using multiple schemas. 
We happen to use multiple schemas and search engine optimization is only a byproduct of the whole RDF excersise. The main targets are services and [tools](http://wiki.dbpedia.org/Applications)
like <code>LodLive</code>, <code>Sgvizler</code>, [foaf-search.net](http://www.foaf-search.net/)
and mashups and aggregations that might emerge.