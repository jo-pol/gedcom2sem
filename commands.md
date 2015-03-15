The library might or might not be plugged into your favorite genealogical-application. But as long as you have your family data available in a
[gedcom](http://en.wikipedia.org/wiki/GEDCOM)![http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png](http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png)
file, you can always use the library on a command line.

The ovals in the diagram on the [overview](overview.md) page represent the main classes of the library that can be executed form a command line.
Below a partial example of such a command line.
Replace the dots (...) with the  files from the same diagram.
Put the output file last; the order of rule files is significant: they may need the results of a previous one.

See also the [notes](Configuration.md) on the configuration files.
```
java -Xmx1024M -cp unzipped/gedcom2sem-XXX.jar gedcom2sem.semweb.Select ...
```
The downloads provide complete
[examples](https://code.google.com/p/gedcom2sem/source/browse/#svn%2Ftrunk%2Fsrc%2Fmain%2Fscripts)
in their roots, the contents of the zip and the tar.gz are identical.
The
[bat](http://en.wikipedia.org/wiki/Batch_file)![http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png](http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png)
files are for Windows: simply double-click them.
The
[sh](http://en.wikipedia.org/wiki/Shell_script)![http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png](http://gedcom2sem.googlecode.com/svn/trunk/wiki-images/icon_external_link_blue.png)
files are for Mac/Unix: open a terminal, go to the directory with the unpacked sh files, assign execute rigths (`chmod +x *.sh`) and run a command (for example: `./convert.sh`). On both platforms you can merge and adjust the files with a plain text editor. Thus you can configure the output you want with the input you have.

See also these [examples](https://code.google.com/p/gedcom2sem/source/browse/trunk/src/test/java/gedcom2sem/BatchExamples.java) expressed in java `JUnit` tests.