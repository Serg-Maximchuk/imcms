Skripten i denna katalog k�rs av klassen DatabaseService n�r man anropar metoderna
initDatabase() respektive createTestData();

Se klassen DatabaseService f�r att se vilka databaser som st�djs.
(I skrivandets stund �r det SQLServer, Mimer och MySQL)

Vid f�r�ndring av skripen i denna katalog, se till att validera inneh�llet mot SQL 92/99 i m�jligaste m�n,
http://developer.mimer.com/validator/parser92/index.tml
http://developer.mimer.com/validator/parser92/index.tml
Se �ven till att alla tester genom testklassen TestDatabaseService �ven forts�ttningsvis g� att k�ra.

F�r att kunna skapa DatabaseService objectet beh�ver man en tom databas.
Se respektive databasleverant�rs instruktioner f�r hur man g�r detta.
Mimer: K�r ett skript med kommandot CREATE DATABANK innan create.sql skriptet k�rs. Se mimer.sql.
R�cker med att k�ra detta en g�ng n�r databasen �r nyskapat.

Nedan listat f�r�ndringar som �r gjorda j�mf�rt med den tidigare SQL Server databasen.

Kvar att unders�ka/g�ra
* Default v�rden satta till NULL �r borttagna
* Andra default v�rden �r inte satta (�nnu, g�r det, finns det en standard?)
* Indexeringen �r droppad s� l�nge men borde g� att l�gga till.
* Kvar �r help.sql tills jag vet mer hur dessa skapats.
* l�gga till en forreign key mellan owner id och user tabellen?
* Ska det finnas kvar en counter i texts?

Sproc:ar kvar att flytta in i koden:
(Ta bort den du h�ller p� med)
classification_fix.prc
deletenewdocpermissionsetex.prc
deleteuseradminpermissibleroles.prc
existingdocsgetselectedmetaids.prc
getcategoryusers.prc
getdoctypeswithnewpermissions.prc
getdoctypeswithpermissions.prc
getmenudocchilds.prc
getmetapathinfo.prc
getpermissionset.prc
getreadrunneruserdataforuser.prc
getrolesdocpermissions.prc
gettemplategroups.prc
gettemplategroupsforuser.prc
gettemplategroupswithnewpermissions.prc
gettemplategroupswithpermissions.prc
gettemplates.prc
gettextdocdata.prc
gettexts.prc
getuseradminpermissibleroles.prc
getusercreatedate.prc
getuseridfromname.prc
getusernames.prc
getuserpermissionset.prc
getuserpermissionsetex.prc
getuserrolesdocpermissions.prc
getuserswhobelongstorole.prc
getusertype.prc
getusertypes.prc
incsessioncounter.prc
inheritpermissions.prc
inserttext.prc
ipaccessadd.prc
ipaccessdelete.prc
ipaccessesgetall.prc
ipaccessupdate.prc
listconferences.prc
listdocsbydate.prc
listdocsgetinternaldoctypes.prc
listdocsgetinternaldoctypesvalue.prc
permissionsgetpermission.prc
poll_addanswer.prc
poll_addnew.prc
poll_addquestion.prc
poll_getall.prc
poll_getallanswers.prc
poll_getallquestions.prc
poll_getanswer.prc
poll_getone.prc
poll_getquestion.prc
poll_increaseansweroption.prc
poll_setanswerpoint.prc
poll_setparameter.prc
removeuserfromrole.prc
roleaddnew.prc
roleadmingetall.prc
rolecheckconferenceallowed.prc
rolecount.prc
rolecountaffectedusers.prc
roledelete.prc
roledeleteviewaffectedmetaids.prc
roledeleteviewaffectedusers.prc
rolefindname.prc
rolegetallapartfromrole.prc
rolegetconferenceallowed.prc
rolegetname.prc
rolegetpermissionsbylanguage.prc
rolegetpermissionsfromrole.prc
rolepermissionsaddnew.prc
roleupdatename.prc
roleupdatepermissions.prc
searchdocs.prc
searchdocsindex.prc
sectionadd.prc
sectionaddcrossref.prc
sectionchangeanddeletecrossref.prc
sectionchangename.prc
sectioncount.prc
sectiondelete.prc
sectiongetall.prc
sectiongetallcount.prc
sectiongetinheritid.prc
servermasterget.prc
servermasterset.prc
setdocpermissionset.prc
setdocpermissionsetex.prc
setinclude.prc
setnewdocpermissionset.prc
setnewdocpermissionsetex.prc
setreadrunneruserdataforuser.prc
setroledocpermissionsetid.prc
shop_addshoppingitemdescription.prc
shop_addshoppingitemtoorder.prc
shop_addshoppingorder.prc
shop_getdescriptionsforshoppingitem.prc
shop_getshoppingitemsfororder.prc
shop_getshoppingorderforuserbyid.prc
shop_getshoppingordersforuser.prc
sortorder_getexistingdocs.prc
startdocget.prc
startdocset.prc
systemmessageget.prc
systemmessageset.prc
unsetuserflag.prc
updatedefaulttemplates.prc
updateparentsdatemodified.prc
updatetemplatetextsandimages.prc
userprefschange.prc
webmasterget.prc
webmasterset.prc

Nedan �r f�r�ndringar mot scriptet tables.ascii.sql
* Splittat i tv� separata skript. Ett f�r drop table och ett f�r create table.
* Satt in ; i slutet av varje kommando. (Standard SQL).
* tinyint �r bytt mot smallint i alla tabeller
* Microsofts (och MySQL) "datetime" & "smalldatetime" har bytts ut mot "timestamp" (D� detta �r Standard SQL, vid k�rning av create table commandon
 byts alla ut mot datetime innan de k�rs. D�refter g�r det att arbeta p� vanligt s�tt med jdbc �ven mot SQLServer)

N�r det g�ller str�ngar skiljer sig dom �t r�tt re�lt mellan databaserna:
- MySQL tar bort trailing spaces p� CHAR vilket g�r att jag k�r VARCHAR genomg�ende f�r att f� samma beteende fr�n samtliga.
- Maximala storleken varierar ocks�:
    MIMER VARCHAR(max 15000) d�refter CLOB, samt CHAR VARYING(max 5000) d�refter CLOB
    SQL Server VARCHAR(max 8 000) d�refter text/ntext
    MySQL VARCHAR(max 255) d�refter TEXT(65 535), MEDIUMTEXT(16 777 215), and LONGTEXT(4 294 967 295)
  CLOB fanns inget st�d f�r i MySQL eller i SQLServer s� denna typ undveks d� l�sning och skrivning hade blivit olika f�r
  de olika databasfallen. Om inte VARCHAR(15000) r�cker f�r vi ta en ny funderare.
  D� f�r man i s� fall behandla MIMER annorlunda och anv�nda CLOB f�r den och k�ra TEXT i �vrigt.
Detta har lett till f�ljande:
* meta: meta_text varchar(1000) -> TEXT i MySQL i �vrigt of�r�ndrat.
* frameset_docs: frame_set text -> VARCHAR(15000) i scriptet, som i sin tur byts ut mot TEXT i MySQL och i SQLServer
* texts: text ntext -> NCHAR VARYING(5000) i scriptet, som i sin tur byts ut mot TEXT i MySQL och NTEXT SQLServer

* I user tabellen �r namnet 'external' bytt mot external_user (extern �r ett reserverat ord i Standard SQL)
* I browsers tabellen �r namnet 'value' bytt mot 'browser_value' (value �r ett reserverat ord i Standard SQL)
* I sys_data tabellen �r namnet 'value' bytt mot 'sysdata_value' (value �r ett reserverat ord i Standard SQL)
* Bytte ut CAST( URRENT_TIME AS CHAR(80)) -> CAST( CURRENT_TIME AS CHAR) kodm�ssigt d� MySQL inte st�dde castning CHAR(siffror).
* Tog bort tabellen CREATE TABLE user_rights (
	user_id INT NOT NULL ,
	meta_id INT NOT NULL ,
	permission_id SMALLINT NOT NULL ,
	PRIMARY KEY (user_id,meta_id,permission_id) ,
    FOREIGN KEY (meta_id) REFERENCES meta (meta_id) ,
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);
d� den inte anv�nds l�ngre
* La till p� tabellen roles_rights FOREIGN KEY (set_id) REFERENCES permission_sets (set_id) och �ndrade typen p� set_id till INT fr�n SMALLINT

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

