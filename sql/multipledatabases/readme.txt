Skripten i denna katalog k�rs av DatabaseService n�r man instantierar denna klass.
Se den klassen f�r att se vilka databaser som st�djs.
(I skrivandets stund �r det SQLServer, Mimer och MySQL)

Vid f�r�ndring av skripen i denna katalog, se till att validera inneh�llet mot SQL 92,
http://developer.mimer.com/validator/parser92/index.tml

Innan man k�r skripten m�ste man skapa en tom databas. Se respektive databasleverant�rs instruktioner f�r hur man g�r detta.
Mimer: K�r ett skript med kommandot CREATE DATABANK innan create.sql skriptet k�rs. Se mimer.sql.

Kvar att unders�ka/g�ra
* Default v�rden satta till NULL �r borttagna
* Andra default v�rden �r inte satta (�nnu, g�r det, finns det en standard?)
* Microsofts text & ntext (unicode variant av text) utbytt mot VARCHAR(255), MySQL st�djer inte st�rre. Alternativ? CLOB?
* CREATE TABLE meta, meta_text varchar (1000) -> varchar (255)
* CREATE TABLE user_flags, description varchar (256) - varchar (255)
* CREATE TABLE frameset_docs, varchar(8000) -> varchar (255)
* CREATE TABLE texts, text varchar(8000) -> varchar (255)
* Microsofts Indexeringen �r droppad, skapa annan? Verkar ing� i standardsql
* Sparar help.sql tills jag vet mer hur detta skapats.

Nedan �r f�r�ndringar mot den scriptet tables.ascii.sql
* Splittat i tv� separata skript. Ett f�r drop table och ett f�r create table.
* Satt in ; i slutet av varje kommando. (Standard SQL).
* tinyint �r bytt mot smallint i alla tabeller
* Microsofts (och MySQL) "datetime" & "smalldatetime" har bytts ut mot "timestamp" (D� detta �r Standard SQL, vid k�rning av create table commandon
 byts alla ut mot datetime innan de k�rs. D�refter g�r det att arbeta p� vanligt s�tt med jdbc �ven mot SQLServer)
* I user tabellen �r external bytt mot external_user (extern �r ett reserverat ord i Standard SQL)
* I browsers tabellen �r 'value' bytt mot 'browser_value' (value �r ett reserverat ord i Standard SQL)
* I sys_data tabellen �r 'value' bytt mot 'sysdata_value' (value �r ett reserverat ord i Standard SQL)
* Bytte ut CAST( URRENT_TIME AS CHAR(80)) -> CAST( CURRENT_TIME AS CHAR) kodm�ssigt d� MySQL inte st�dde castning CHAR(siffror).
F�r att slippa g�ra trim p� str�ngar i koden �ndrade jag de (f�) st�llena med char till varchar. Detta f�r att MySQL trimmar alla
CHAR default, och det g�r inte att st�nga av, s� f�r att garanterat f� samma beteende gjorde jag detta.
* CREATE TABLE lang_prefixes, lang_prefix char (3),
* CREATE TABLE roles, role_name char (25) NOT NULL
* CREATE TABLE user_types, type_name char (30) och lang_prefix char (3) NOT NULL ,

F�r�ndringar gentemot filen types.sql
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

