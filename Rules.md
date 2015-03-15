
<h1>Ontologies and rules</h1>

Like HTML tells to browsers how to present your text and images, ontologies tell semantic tools like search engines how to reason with your data.

The first conversion step by this library ignores ontologies: it just turns gedcom tags into predicates and object classes.
The result is similar to the input, just an other syntax and multiple formats to choose from.
On the other hand does this approach lead to conflicts, for example the title of a person is another concept (social rank: Mrs., Dr., Sir) than the title of a multimedia object which is rather a catchy phrase.

However, with [jena's rules](http://jena.apache.org/documentation/inference/index.html#rules)![http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png](http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png)
you can derive new statements that respect an ontology of your choice. Drop the original statements for a valid semantic document. Thus you can model the data any way you want.
# Implementation #

The library currently provides two sets of rules.
One set only works with the convert command and the result is used by the report queries.
Another set works with the transform command, it uses foaf/bio ontologies, the result is intended for publication.
The latter set needs a review: [issue 16](https://code.google.com/p/gedcom2sem/issues/detail?id=16).
See the
[transform](https://code.google.com/p/gedcom2sem/source/browse/trunk/src/main/scripts/convert.bat)
example for the actual set of rule files.
For more tricks see jena's built in
[primitives](http://jena.apache.org/documentation/inference/index.html#RULEbuiltins)![http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png](http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png)
and java's
[regular expressions](http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html)![http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png](http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png)


# Customization #

The rules provided for the transform command are not complete
because some details depend on what you do with and in your gedcom.
For example a note may be about an event but also a reminder for further research.
The examples below should help you to construct additional rule files.
You might also want to make other changes as explained by [Modeling](Modeling.md).


## Published gedcom ##

When you upload your gedcom to some community or publish it yourself, you might want to link to that publication with an additional rule. In the following examples you should replace at least `SOMENAME` with a value related to your account or publication.

**ancestry.com**
```
[
  (?indi rdf:type  t:INDI)
  (?indi p:id ?i)
  regex(?i, '@(.*)@.*', ?id)
  uriConcat("http://wc.rootsweb.ancestry.com/cgi-bin/igm.cgi?op=GET&db=SOMENAME&id=", ?id, ?url)
->
  (?indi http://xmlns.com/foaf/0.1/isPrimaryTopicOf ?url  )
]
```

**genealogieonline.nl**
```
[
  (?indi rdf:type  t:INDI)
  (?indi p:id ?i)
  regex(?i, '@(.*)@.*', ?id)
  uriConcat("http://www.genealogieonline.nl/SOMENAME/", ?id, ".php", ?url)
->
  (?indi http://xmlns.com/foaf/0.1/isPrimaryTopicOf ?url  )
]
```

## Identification ##

See "identifying marks" in this [Introduction to FOAF](http://www.xml.com/pub/a/2004/02/04/foaf.html). It explains how the above primary-topic statements are identifications for the persons.

## Sources ##

The meaning of the gedcom properties/entities notes, sources and multimedia objects may vary with individual or project conventions. So no default rules can be specified to model these properties .

Let us assume a convention to put URLs in a source property of the gedcom whenever possible. At least in some cases and not start with an URL if it comes with ordinary text. For example https://familysearch.org/pal:/MM9.3.1/TH-1-11324-36479-63
has birth certificates. Early adopters of this service might also have something starting with das.familysearch.org
These URLs usually come with a question mark and subsequent parameters, it appears these can be stripped from the URL and replaced with something like "#112". The replacement has no function than to indicate the certificate number on the digitized page to human readers and still have a working URL.

The following rule links your gedcom data with the digitized page.
```
[
  (?entity p:BIRT ?event)
  (?event p:SOUR ?s)
  (?s rdfs:label ?uri)
  regex(?uri, '(https?://[das.]*familysearch.org/[^#]*)(#(.*))?', ?url, ?fragment, ?certificateNr)
  print(?url, ?certificateNr)
  bound(?url)
->
  (?event under:construction ?url)  
]
```
The regular expression splits the mandatory part of the URL and the added certificate number. The question mark after https makes the s optional, both forms of the URLs work. The statement with the predicate `under:construction` needs to be figured out, `foaf:isPrimaryTopicOf` is not applicable as there are more certificates on the digitized page. Suggestions are welcome to elaborate this attempt.