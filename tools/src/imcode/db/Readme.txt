K�ra avumling:
DeUmlaut.java avumlar tabellen text.

St�ll in r�tt parametrar f�r databasen i DeUmlaut.java
Kompilera med:  ant clean tools
St�ll dig i imCMS/1.3/tools/build mappen och skriv:

java -classpath .;..\..\install\lib\Opta2000.jar;..\..\install\lib\jdbc2_0-stdext.jar imcode.db.DeUmlaut login password database server

ex.
java -classpath .;..\..\install\lib\Opta2000.jar;..\..\install\lib\jdbc2_0-stdext.jar imcode.db.DeUmlaut sa nonac imcms lennart


ex. n�r filen ligger i webapp mappen client �ndras s�kv�gen i classpathen enligt nedan.

java -classpath .;..\..\lib\Opta2000.jar;..\..\lib\jdbc2_0-stdext.jar imcode.db.DeUmlaut sa hwv62v3h db010602 localhost

