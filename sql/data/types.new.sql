
INSERT INTO languages VALUES('se','se','Svenska');
INSERT INTO languages VALUES('se','en','Swedish');
INSERT INTO languages VALUES('en','se','Engelska');
INSERT INTO languages VALUES('en','en','English');

INSERT INTO lang_prefixes VALUES(1,'se');
INSERT INTO lang_prefixes VALUES(2,'en');

INSERT INTO doc_types VALUES(2, 'se', 'Text-dokument');
INSERT INTO doc_types VALUES(5, 'se', 'URL-dokument');
INSERT INTO doc_types VALUES(6, 'se', 'Browserkontroll');
INSERT INTO doc_types VALUES(7, 'se', 'HTML-dokument');
INSERT INTO doc_types VALUES(8, 'se', 'Fil');
INSERT INTO doc_types VALUES(102, 'se', 'Konferens');
INSERT INTO doc_types VALUES(103, 'se', 'Chatt');
INSERT INTO doc_types VALUES(104, 'se', 'Anslagstavla');
INSERT INTO doc_types VALUES(107, 'se', 'Kalender');

INSERT INTO doc_types VALUES(2, 'en', 'Text-document');
INSERT INTO doc_types VALUES(5, 'en', 'URL-document');
INSERT INTO doc_types VALUES(6, 'en', 'Browsercontrol');
INSERT INTO doc_types VALUES(7, 'en', 'HTML-document');
INSERT INTO doc_types VALUES(8, 'en', 'File');
INSERT INTO doc_types VALUES(102, 'en', 'Conference');
INSERT INTO doc_types VALUES(103, 'en', 'Chat');
INSERT INTO doc_types VALUES(104, 'en', 'Billboard');
INSERT INTO doc_types VALUES(107, 'en', 'Calendar');

INSERT INTO user_types VALUES(0, 'Anonyma anv�ndare', 'se');
INSERT INTO user_types VALUES(1, 'Autentiserade anv�ndare', 'se');
INSERT INTO user_types VALUES(2, 'Konferensanv�ndare', 'se');
INSERT INTO user_types VALUES(0, 'Anonymous users', 'en');
INSERT INTO user_types VALUES(1, 'Authenticated users', 'en');
INSERT INTO user_types VALUES(2, 'Conference users', 'en');

INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,2,'se','�ndra text');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,5,'se','Redigera');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,6,'se','Redigera');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,7,'se','Redigera');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,8,'se','Redigera');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,101,'se','Redigera');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,102,'se','Redigera');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(131072,2,'se','�ndra bild');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(262144,2,'se','�ndra meny');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(524288,2,'se','�ndra utseende');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(1048576,2,'se','�ndra include');

INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,2,'en','Edit texts');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,5,'en','Edit');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,6,'en','Edit');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,7,'en','Edit');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,8,'en','Edit');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,101,'en','Edit');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(65536,102,'en','Edit');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(131072,2,'en','Edit pictures');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(262144,2,'en','Edit menus');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(524288,2,'en','Change template');
INSERT INTO doc_permissions (permission_id, doc_type, lang_prefix, description) VALUES(1048576,2,'en','Change include');

INSERT INTO permissions (permission_id, lang_prefix, description) VALUES(1,'se','�ndra rubrik');
INSERT INTO permissions (permission_id, lang_prefix, description) VALUES(2,'se','�ndra dokinfo');
INSERT INTO permissions (permission_id, lang_prefix, description) VALUES(4,'se','�ndra r�ttigheter f�r roller');
INSERT INTO permissions (permission_id, lang_prefix, description) VALUES(8,'se','Skapa dokument');

INSERT INTO permissions (permission_id, lang_prefix, description) VALUES(1,'en','Edit headline');
INSERT INTO permissions (permission_id, lang_prefix, description) VALUES(2,'en','Edit docinfo');
INSERT INTO permissions (permission_id, lang_prefix, description) VALUES(4,'en','Edit permissions');
INSERT INTO permissions (permission_id, lang_prefix, description) VALUES(8,'en','Create document');

INSERT INTO roles_permissions (permission_id, lang_prefix, description) VALUES(1,'se','R�tt att f� l�senord per mail');
INSERT INTO roles_permissions (permission_id, lang_prefix, description) VALUES(2,'se','Sj�lvregistreringsr�tt i konferens');

INSERT INTO roles_permissions (permission_id, lang_prefix, description) VALUES(1,'en','Permission to get password by email');
INSERT INTO roles_permissions (permission_id, lang_prefix, description) VALUES(2,'en','Selfregister rights in conference');

INSERT INTO permission_sets (set_id, description) VALUES(0,'Full');
INSERT INTO permission_sets (set_id, description) VALUES(1,'Begr�nsad 1');
INSERT INTO permission_sets (set_id, description) VALUES(2,'Begr�nsad 2');
INSERT INTO permission_sets (set_id, description) VALUES(3,'L�s');

INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(1,'Internet Explorer','%MSIE%',2);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(2,'Netscape','Mozilla%(%;%[UIN][);]%',2);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(3,'Internet Explorer 3','%MSIE 3%',4);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(4,'Internet Explorer 4','%MSIE 4%',4);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(5,'Internet Explorer 5','%MSIE 5%',4);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(6,'Internet Explorer 6','%MSIE 6%',4);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(7,'Netscape 3','Mozilla/3%(%;%[UIN][ );]%',4);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(8,'Netscape 4','Mozilla/4%(%;%[UIN][ );]%',4);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(9,'Netscape 6','Mozilla/5%(%;%[UIN][ );]%',4);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(10,'Netscape 7','Mozilla%/5;%netscape/7%',4);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(11,'Windows','%win%',1);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(12,'Macintosh','%mac%',1);

INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(13,'Windows Internet Explorer','%MSIE%win%',3);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(14,'Windows Internet Explorer 3','%MSIE 3%win%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(15,'Windows Internet Explorer 4','%MSIE 4%win%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(16,'Windows Internet Explorer 5.0','%MSIE 5.0%win%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(17,'Windows Internet Explorer 5.5','%MSIE 5.5%win%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(18,'Windows Internet Explorer 6','%MSIE 6%win%',5);

INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(19,'Windows Netscape','Mozilla%(%win%;%[UIN][ );]%',3);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(20,'Windows Netscape 3','Mozilla/3%(%win%;%[UIN][ );]%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(21,'Windows Netscape 4','Mozilla/4%(%win%;%[UIN][ );]%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(22,'Windows Netscape 6','Mozilla/5%(%win%;%[UIN][ );]%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(23,'Windows Netscape 7','Mozilla/5%(%win%netscape/7%',5);

INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(24,'Macintosh Internet Explorer','%MSIE%mac%',3);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(25,'Macintosh Internet Explorer 3','%MSIE 3%mac%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(26,'Macintosh Internet Explorer 4','%MSIE 4%mac%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(27,'Macintosh Internet Explorer 5','%MSIE 5%mac%',5);

INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(28,'Macintosh Netscape','Mozilla%(%mac%;%[UIN][ );]%',3);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(29,'Macintosh Netscape 3','Mozilla/3%(%mac%;%[UIN][ );]%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(30,'Macintosh Netscape 4','Mozilla/4%(%mac%;%[UIN][ );]%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(31,'Macintosh Netscape 6','Mozilla/5%(%mac%;%[UIN][ );]%',5);
INSERT INTO browsers (browser_id, name, user_agent, browser_value) VALUES(0,'�vriga','%',0);

INSERT INTO sys_types (type_id,name) VALUES(0,'StartDocument');
INSERT INTO sys_types (type_id,name) VALUES(1,'SessionCounter');
INSERT INTO sys_types (type_id,name) VALUES(2,'SessionCounterDate');
INSERT INTO sys_types (type_id,name) VALUES(3,'SystemMessage');
INSERT INTO sys_types (type_id,name) VALUES(4,'ServerMaster');
INSERT INTO sys_types (type_id,name) VALUES(5,'ServerMasterAddress');
INSERT INTO sys_types (type_id,name) VALUES(6,'WebMaster');
INSERT INTO sys_types (type_id,name) VALUES(7,'WebMasterAddress');

INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(0,'Annan...','other','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(1,'Vanlig text','text/plain','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(2,'HTML-dokument','text/html','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(3,'Bin�rfil','application/octet-stream','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(4,'Shockwave Flash','application/x-shockwave-flash','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(5,'Shockwave Director','application/x-director','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(6,'PNG-bild','image/png','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(7,'GIF-bild','image/gif','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(8,'JPEG-bild','image/jpeg','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(9,'Adobe Acrobat-dokument','application/pdf','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(10,'Wav-ljud','audio/x-wav','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(11,'Zip-fil','application/zip','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(12,'AVI-film','video/x-msvideo','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(13,'Quicktime-film','video/quicktime','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(14,'MPEG-film','video/mpeg','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(15,'MS Word-dokument','application/msword','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(16,'MS Excel-dokument','application/vnd.ms-excel','se');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(17,'MS Powerpoint-dokument','application/vnd.ms-powerpoint','se');

INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(0,'Other...','other','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(1,'Plain text','text/plain','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(2,'HTML-document','text/html','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(3,'Binary file','application/octet-stream','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(4,'Shockwave Flash','application/x-shockwave-flash','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(5,'Shockwave Director','application/x-director','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(6,'PNG-image','image/png','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(7,'GIF-image','image/gif','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(8,'JPEG-image','image/jpeg','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(9,'Adobe Acrobat-document','application/pdf','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(10,'Wav-sound','audio/x-wav','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(11,'Zip-file','application/zip','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(12,'AVI-movie','video/x-msvideo','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(13,'Quicktime-movie','video/quicktime','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(14,'MPEG-movie','video/mpeg','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(15,'MS Word-document','application/msword','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(16,'MS Excel-document','application/vnd.ms-excel','en');
INSERT INTO mime_types (mime_id, mime_name, mime, lang_prefix) VALUES(17,'MS Powerpoint-document','application/vnd.ms-powerpoint','en');

INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (1, 'meta_headline');
INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (2, 'meta_id');
INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (3, 'doc_type');
INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (4, 'date_modified');
INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (5, 'date_created');
INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (6, 'date_archived');
INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (7, 'date_activated');
INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (8, 'meta_text');
INSERT INTO SORT_BY ( sort_by_id, sort_by_type ) VALUES (9, 'archive');

INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(1,1,'Rubrik');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(1,2,'Meta headline');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(2,1,'Meta ID');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(2,2,'Meta ID');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(3,1,'Dokument typ');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(3,2,'Document type');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(4,1,'�ndrat datum');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(4,2,'Date modified');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(5,1,'Skapat datum');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(5,2,'Date created');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(6,1,'Arkiverat datum');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(6,2,'Archived date');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(7,1,'Aktiverat datum');
INSERT INTO display_name ( sort_by_id, lang_id, display_name ) VALUES(7,2,'Activated date');


INSERT INTO phonetypes VALUES(0, 'Annat', 1 );
INSERT INTO phonetypes VALUES(1, 'Home', 1 );
INSERT INTO phonetypes VALUES(2, 'Arbete', 1 );
INSERT INTO phonetypes VALUES(3, 'Mobil', 1 );
INSERT INTO phonetypes VALUES(4, 'Fax', 1 );
INSERT INTO phonetypes VALUES(0, 'Other', 2 );
INSERT INTO phonetypes VALUES(1, 'Home', 2 );
INSERT INTO phonetypes VALUES(2, 'Work', 2 );
INSERT INTO phonetypes VALUES(3, 'Mobile', 2 );
INSERT INTO phonetypes VALUES(4, 'Fax', 2 );
