F�r�ndringar mot SQL Server orginalscriptet tables.ascii.sql
 * tinyint �r bytt mot smallint i alla tabeller
 * Microsofts "datetime" & "smalldatetime" har bytts ut mot "timestamp"
 * I user tabellen �r external bytt mot external_user.
 * I browsers tabellen �r 'value' bytt mot 'browser_value'
 * I sys_data tabellen �r 'value' bytt mot 'sysdata_value'
 * Microsofts text & ntext (unicode variant av text) utbytt mot VARCHAR(8000), vad betyder det f�r text tabellen?
   8000 �r den maximala storleken SQLServer verkar st�dja. Vad SQL92 s�ger vet jag (�nnu) inte.

* MySQL kr�vde ytterligare f�r�ndringar:
* CREATE TABLE meta, meta_text varchar (1000) -> varchar (255)
* CREATE TABLE user_flags, description varchar (256) - varchar (255)
* CREATE TABLE frameset_docs, varchar(8000) -> varchar (255)
* CREATE TABLE texts, text varchar(8000) -> varchar (255)
* MySQL anv�nder samma datum/tid typer som SQL Server
* Bytte ut CAST( CURRENT_TIME AS CHAR(80)) -> CAST( CURRENT_TIME AS CHAR) kodm�ssigt.

 * Default v�rden satta till NULL �r borttagna
 * Andra default v�rden �r inte satta (�nnu, g�r det, finns det en standard?)
 * Microsofts Indexeringen �r droppad, skapa annan? Verkar ing� i standardsql

 * The following feature outside Core SQL-99 is used: F391, "Long identifiers"

F�r�ndringar mot types.sql
* Satt in ; i slutet p� varje commando.
* 'value' bytt mot 'browser_value' p� alla st�llen som h�ller p� med 'browsers' tabellen
* tog bort alla "SET IDENTITY_INSERT sys_types ON/OFF" d� de inte beh�vs n�r det finns en prim�rnyckel.
* tog bort de bortkommenterade raderna:
    --INSERT INTO doc_types VALUES(101, 'se', 'Diagram');
    --INSERT INTO doc_types VALUES(101, 'en', 'Diagram');

F�r�ndringar mot newdb.sql
* Satt in ; i slutet p� varje commando.
* tog bort alla "SET IDENTITY_INSERT sys_types ON/OFF" d� de inte beh�vs n�r det finns en prim�rnyckel.
* I sys_data tabellen �r 'value' bytt mot 'sysdata_value'
* La till en siffra i f�r primary key i text kollumnen
* �ndrade getDate() till CURRENT_TIMESTAMP i users
* �ndrade getDate() till CURRENT_TIMESTAMP och droppade formateringen i sys_data! Kolla upp vart denna anv�nds, id = 2
* �ndrade getDate() till CURRENT_TIMESTAMP i meta

Sparar help.sql tills jag vet mer hur detta skapats.

N�sta steg
- Validera create och drop scripten mot MySQL n�r jag �nd� h�ller p�.
- B�rja med fyra sproc:ar som g�r select, delete, update och insert respektive.
- G�r f�r�ndringar p� befintlig databas f�rst
    - Flytta samtliga sproc till ett st�lle och byt ut, innan migrationen
    - Flytta all SQL till ett och samma st�lle

Efter varje f�r�ndring av create.sql och drop.sql se till att validera inneh�llet mot SQL 99:
http://developer.mimer.com/validator/parser99/index.tml

Mimer
K�r ett skript med kommandot CREATE DATABANK innan create.sql skriptet k�rs.