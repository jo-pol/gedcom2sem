

# Does dbPedia have resources in my language? #

Try the mashup query dbpediaLanguages.arq Note that not all can be queried directly.

# What properties are commonly available in third party resources? #
Try the mashup [queries](https://github.com/jo-pol/gedcom2sem/tree/master/src/main/resources/reports/mashup):
  * geonamesProperties.arq
  * geonamesRelatedEntities.arq
  * dbpediaRelatedEntities.arq
  * dbpediaProperties.arq

# Skipping xxx.arq: Lexical error at line 1, column 2. Encountered: "\u00bb" (187), after : "\u00ef" #

You probably saved the arq file as an UTF-8 with a byte-order-mark. Typically Window's `NotePad` adds that BOM when you choose to save as UTF-8. Normaly these bytes do not show, but with the command "type" in a command window, they show up as funny characters. Search the web how to strip the BOM.