-- mssql SET IDENTITY_INSERT sys_data ON
-- oracle ALTER TABLE sys_data DISABLE ALL TRIGGERS;
INSERT INTO sys_data (sys_id, type_id, ivalue) VALUES(0, 0, 1001);
INSERT INTO sys_data (sys_id, type_id, ivalue) VALUES(1, 1, 0);
INSERT INTO sys_data (sys_id, type_id, ivalue) VALUES(2, 2,
-- mysql NOW()
-- mssql CONVERT(CHAR(10),GETDATE(),120)
-- oracle TO_CHAR(SYSDATE, 'YYYY-MM-DD')
);
INSERT INTO sys_data (sys_id, type_id, ivalue) VALUES(3, 3, NULL);
INSERT INTO sys_data (sys_id, type_id, ivalue) VALUES(4, 4, '@servermaster-name@');
INSERT INTO sys_data (sys_id, type_id, ivalue) VALUES(5, 5, '@servermaster-email@');
INSERT INTO sys_data (sys_id, type_id, ivalue) VALUES(6, 6, '@webmaster-name@');
INSERT INTO sys_data (sys_id, type_id, ivalue) VALUES(7, 7, '@webmaster-email@');
-- mssql SET IDENTITY_INSERT sys_data OFF
-- oracle ALTER TABLE sys_data ENABLE ALL TRIGGERS;

-- mssql SET IDENTITY_INSERT users ON
-- oracle ALTER TABLE users DISABLE ALL TRIGGERS;
INSERT INTO users (user_id, login_name, login_password, first_name, last_name,
                   title, company, address, city, zip, country, county_council, email,
                   iexternal, active, create_date, ilanguage)
VALUES (1,'admin', 'admin', 'Admin', 'Super',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'@servermaster-email@',0,1,
-- mysql NOW()
-- mssql GETDATE()
-- oracle SYSDATE
,'<? sql/default_lang ?>');
INSERT INTO users (user_id, login_name, login_password, first_name, last_name,
                   title, company, address, city, zip, country, county_council, email,
                   iexternal, active, create_date, ilanguage)
VALUES (2,'user', 'user', 'User', 'Extern',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,
-- mysql NOW()
-- mssql GETDATE()
-- oracle SYSDATE
,'<? sql/default_lang ?>');
-- mssql SET IDENTITY_INSERT users OFF
-- oracle ALTER TABLE users ENABLE ALL TRIGGERS;

-- mssql SET IDENTITY_INSERT roles ON
-- oracle ALTER TABLE roles DISABLE ALL TRIGGERS;
INSERT INTO roles (role_id, role_name, permissions, admin_role) VALUES(0, 'Superadmin', 0, 1);
INSERT INTO roles (role_id, role_name, permissions, admin_role) VALUES(1, 'Useradmin', 0, 2);
INSERT INTO roles (role_id, role_name, permissions, admin_role) VALUES(2, 'Users', 1, 0);
-- mssql SET IDENTITY_INSERT roles OFF
-- oracle ALTER TABLE roles ENABLE ALL TRIGGERS;

INSERT INTO user_roles_crossref VALUES(1,0);
INSERT INTO user_roles_crossref VALUES(2,2);

-- mssql SET IDENTITY_INSERT meta ON
-- oracle ALTER TABLE meta DISABLE ALL TRIGGERS;
-- oracle DROP SEQUENCE SEQ_META_META_ID;
-- oracle CREATE SEQUENCE SEQ_META_META_ID START WITH 1002;
INSERT INTO meta (meta_id, doc_type, meta_headline,                meta_text, meta_image, owner_id, permissions, shared, show_meta, lang_prefix,         date_created,                    date_modified,                   disable_search, archived_datetime, target,  activate, status, publication_start_datetime,      publication_end_datetime)
 VALUES (1001,   2,        '<? sql/sql/newdb.sql/headline_1001 ?>',  NULL,        NULL,         1,        0,           0,      0,         '@language@',
-- mysql NOW()
-- mssql GETDATE()
-- oracle SYSDATE
,
-- mysql NOW()
-- mssql GETDATE()
-- oracle SYSDATE
, 0,              null,              '_self', 1,        2,
-- mysql NOW()
-- mssql GETDATE()
-- oracle SYSDATE
, null);
-- mssql SET IDENTITY_INSERT meta OFF
-- oracle ALTER TABLE meta ENABLE ALL TRIGGERS;

INSERT INTO templates VALUES (1,'demo.html', 'demo', '<? sql/default_lang ?>', 1,1,1);

-- mssql SET IDENTITY_INSERT templategroups ON
-- oracle ALTER TABLE templategroups DISABLE ALL TRIGGERS;
INSERT INTO templategroups (group_id, group_name) VALUES (0, 'normal');
-- mssql SET IDENTITY_INSERT templategroups OFF
-- oracle ALTER TABLE templategroups ENABLE ALL TRIGGERS;

INSERT INTO templates_cref VALUES(0,1);

INSERT INTO text_docs VALUES (1001, 1, 0, -1, -1, NULL);

INSERT INTO roles_rights VALUES (2,1001,3);

INSERT INTO texts (meta_id, iname, text, itype) VALUES( 1001, 1, '<? sql/sql/newdb.sql/text_1001_1 ?>',1);
INSERT INTO texts (meta_id, iname, text, itype) VALUES( 1001, 2, '<? sql/sql/newdb.sql/text_1001_2 ?>',1);

