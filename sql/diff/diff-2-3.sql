DROP PROCEDURE GetCurrentSessionCounter
DROP PROCEDURE GetCurrentSessionCounterDate
DROP PROCEDURE GetTemplateGroupsWithNewPermissions
DROP PROCEDURE GetTemplateGroupsWithPermissions
DROP PROCEDURE GetUserPermissionSet
DROP PROCEDURE GetUserPermissionSetEx
DROP PROCEDURE IncSessionCounter
DROP PROCEDURE SectionGetInheritId
DROP PROCEDURE ServerMasterGet
DROP PROCEDURE StartDocGet
DROP PROCEDURE SystemMessageGet
DROP PROCEDURE WebMasterGet

-- 2005-02-28 Kreiger

DROP PROCEDURE GetDocTypesForUser
DROP PROCEDURE SetRoleDocPermissionSetId

-- 2005-03-01 Kreiger

DROP PROCEDURE DocumentDelete
DROP PROCEDURE GetAllUsersInList

-- 2005-03-03 Kreiger

DROP PROCEDURE FindUserName

-- 2005-03-21 Kreiger

DROP PROCEDURE SectionDelete
DROP PROCEDURE SectionChangeAndDeleteCrossref

-- 2005-04-06 Kreiger

DROP PROCEDURE RoleFindName
DROP PROCEDURE RoleGetName
DROP PROCEDURE RoleUpdateName
DROP PROCEDURE RoleDelete
DROP PROCEDURE RoleDeleteViewAffectedMetaIds
DROP PROCEDURE RoleDeleteViewAffectedUsers

-- 2005-04-13 Kreiger

ALTER TABLE category_types ADD inherited BIT
GO
UPDATE category_types SET inherited = 1 WHERE inherited IS NULL
GO
ALTER TABLE category_types ALTER COLUMN inherited BIT NOT NULL
GO
ALTER TABLE category_types ADD CONSTRAINT UQ__category_types__name UNIQUE ( name )
GO

-- 2005-04-14 Kreiger

DROP PROCEDURE SortOrder_GetExistingDocs
DROP TABLE display_name
DROP TABLE sort_by

-- 2005-04-19 Kreiger

UPDATE meta SET meta_image = REPLACE(meta_image, '../', '')

-- 2005-04-26 Lennart �

DROP PROCEDURE PhoneNbrAdd

BEGIN TRANSACTION
SET QUOTED_IDENTIFIER ON
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE
SET ARITHABORT ON
SET NUMERIC_ROUNDABORT OFF
SET CONCAT_NULL_YIELDS_NULL ON
SET ANSI_NULLS ON
SET ANSI_PADDING ON
SET ANSI_WARNINGS ON
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.phones
    DROP CONSTRAINT FK_phones_users
GO
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.phones
    DROP CONSTRAINT DF_phones_phonetype_id
GO
CREATE TABLE dbo.Tmp_phones
(
    phone_id int NOT NULL IDENTITY (1, 1),
    number varchar(25) NOT NULL,
    user_id int NOT NULL,
    phonetype_id int NOT NULL
)  ON [PRIMARY]
GO
ALTER TABLE dbo.Tmp_phones ADD CONSTRAINT
    DF_phones_phonetype_id DEFAULT (0) FOR phonetype_id
GO
SET IDENTITY_INSERT dbo.Tmp_phones ON
GO
IF EXISTS(SELECT * FROM dbo.phones)
    EXEC('INSERT INTO dbo.Tmp_phones (phone_id, number, user_id, phonetype_id)
SELECT phone_id, number, user_id, phonetype_id FROM dbo.phones TABLOCKX')
GO
SET IDENTITY_INSERT dbo.Tmp_phones OFF
GO
DROP TABLE dbo.phones
GO
EXECUTE sp_rename N'dbo.Tmp_phones', N'phones', 'OBJECT'
GO
ALTER TABLE dbo.phones ADD CONSTRAINT
PK_phones PRIMARY KEY NONCLUSTERED
(
    phone_id,
    user_id
) ON [PRIMARY]
GO
ALTER TABLE dbo.phones WITH NOCHECK ADD CONSTRAINT
FK_phones_users FOREIGN KEY
(
    user_id
) REFERENCES dbo.users
(
    user_id
)
GO
COMMIT

BEGIN TRANSACTION
SET QUOTED_IDENTIFIER ON
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE
SET ARITHABORT ON
SET NUMERIC_ROUNDABORT OFF
SET CONCAT_NULL_YIELDS_NULL ON
SET ANSI_NULLS ON
SET ANSI_PADDING ON
SET ANSI_WARNINGS ON
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.users
    DROP CONSTRAINT DF_users_title
GO
ALTER TABLE dbo.users
    DROP CONSTRAINT DF_users_company
GO
ALTER TABLE dbo.users
    DROP CONSTRAINT DF_users_active
GO
CREATE TABLE dbo.Tmp_users
    (
    user_id int NOT NULL IDENTITY (1, 1),
    login_name varchar(50) NOT NULL,
    login_password varchar(15) NOT NULL,
    first_name varchar(25) NOT NULL,
    last_name varchar(30) NOT NULL,
    title varchar(30) NOT NULL,
    company varchar(30) NOT NULL,
    address varchar(40) NOT NULL,
    city varchar(30) NOT NULL,
    zip varchar(15) NOT NULL,
    country varchar(30) NOT NULL,
    county_council varchar(30) NOT NULL,
    email varchar(50) NOT NULL,
    [external] int NOT NULL,
    active int NOT NULL,
    create_date smalldatetime NOT NULL,
    [language] varchar(3) NOT NULL
    )  ON [PRIMARY]
GO
ALTER TABLE dbo.Tmp_users ADD CONSTRAINT
    DF_users_title DEFAULT ('') FOR title
GO
ALTER TABLE dbo.Tmp_users ADD CONSTRAINT
    DF_users_company DEFAULT ('') FOR company
GO
ALTER TABLE dbo.Tmp_users ADD CONSTRAINT
    DF_users_active DEFAULT (1) FOR active
GO
SET IDENTITY_INSERT dbo.Tmp_users ON
GO
IF EXISTS(SELECT * FROM dbo.users)
     EXEC('INSERT INTO dbo.Tmp_users (user_id, login_name, login_password, first_name, last_name, title, company, address, city, zip, country, county_council, email, [external], active, create_date, [language])
        SELECT user_id, login_name, login_password, first_name, last_name, title, company, address, city, zip, country, county_council, email, [external], active, create_date, [language] FROM dbo.users TABLOCKX')
GO
SET IDENTITY_INSERT dbo.Tmp_users OFF
GO
ALTER TABLE dbo.meta
    DROP CONSTRAINT FK_meta_users
GO
ALTER TABLE dbo.phones
    DROP CONSTRAINT FK_phones_users
GO
ALTER TABLE dbo.user_flags_crossref
    DROP CONSTRAINT FK_user_flags_crossref_users
GO
ALTER TABLE dbo.user_rights
    DROP CONSTRAINT FK_user_rights_users
GO
ALTER TABLE dbo.user_roles_crossref
    DROP CONSTRAINT FK_user_roles_crossref_users
GO
ALTER TABLE dbo.useradmin_role_crossref
    DROP CONSTRAINT FK_useradmin_role_crossref_users
GO
DROP TABLE dbo.users
GO
EXECUTE sp_rename N'dbo.Tmp_users', N'users', 'OBJECT'
GO
ALTER TABLE dbo.users ADD CONSTRAINT
    users_login_name UNIQUE NONCLUSTERED
    (
    login_name
    ) ON [PRIMARY]

GO
ALTER TABLE dbo.users ADD CONSTRAINT
    PK_users PRIMARY KEY NONCLUSTERED
    (
    user_id
    ) ON [PRIMARY]

GO
CREATE UNIQUE NONCLUSTERED INDEX IX_users_login_name ON dbo.users
    (
    login_name
    ) ON [PRIMARY]
GO
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.useradmin_role_crossref WITH NOCHECK ADD CONSTRAINT
    FK_useradmin_role_crossref_users FOREIGN KEY
    (
    user_id
    ) REFERENCES dbo.users
    (
    user_id
    )
GO
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.user_roles_crossref WITH NOCHECK ADD CONSTRAINT
    FK_user_roles_crossref_users FOREIGN KEY
    (
    user_id
    ) REFERENCES dbo.users
    (
    user_id
    )
GO
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.user_rights WITH NOCHECK ADD CONSTRAINT
    FK_user_rights_users FOREIGN KEY
    (
    user_id
    ) REFERENCES dbo.users
    (
    user_id
    )
GO
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.user_flags_crossref WITH NOCHECK ADD CONSTRAINT
    FK_user_flags_crossref_users FOREIGN KEY
    (
    user_id
    ) REFERENCES dbo.users
    (
    user_id
    )
GO
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.phones WITH NOCHECK ADD CONSTRAINT
    FK_phones_users FOREIGN KEY
    (
    user_id
    ) REFERENCES dbo.users
    (
    user_id
    )
GO
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.meta WITH NOCHECK ADD CONSTRAINT
    FK_meta_users FOREIGN KEY
    (
    publisher_id
    ) REFERENCES dbo.users
    (
    user_id
    )
GO
COMMIT

-- 2005-06-14 Kreiger - Add identity to "phones" and "users" tables primary keys.

DROP TABLE A_conf_forum
DROP TABLE A_conf_selfreg_crossref
DROP TABLE A_conf_templates
DROP TABLE A_conf_users_crossref
DROP TABLE A_conf_users
DROP TABLE A_conference
DROP TABLE A_discussion
DROP TABLE A_forum
DROP TABLE A_replies
DROP TABLE A_selfreg_roles
DROP TABLE A_templates
DROP TABLE B_bill
DROP TABLE B_billboard
DROP TABLE B_billboard_section
DROP TABLE B_billboard_templates
DROP TABLE B_replies
DROP TABLE B_section
DROP TABLE B_templates
DROP TABLE C_authorization_types
DROP TABLE C_chat
DROP TABLE C_chatParameters
DROP TABLE C_chat_authorization
DROP TABLE C_chat_msg_type
DROP TABLE C_chat_selfreg_crossref
DROP TABLE C_chat_templates
DROP TABLE C_msg_type
DROP TABLE C_selfreg_roles
DROP TABLE C_templates

DELETE FROM doc_types WHERE doc_type > 100

DROP TABLE poll_answers
DROP TABLE poll_questions
DROP TABLE polls

DROP PROCEDURE poll_addanswer
DROP PROCEDURE poll_addnew
DROP PROCEDURE poll_addquestion
DROP PROCEDURE poll_getall
DROP PROCEDURE poll_getallanswers
DROP PROCEDURE poll_getallquestions
DROP PROCEDURE poll_getanswer
DROP PROCEDURE poll_getone
DROP PROCEDURE poll_getquestion
DROP PROCEDURE poll_increaseansweroption
DROP PROCEDURE poll_setanswerpoint
DROP PROCEDURE poll_setparameter

DROP PROCEDURE GetUserIdFromName

DROP TABLE user_flags_crossref
DROP TABLE user_flags

DROP PROCEDURE getuserflags
DROP PROCEDURE getuserflagsforuser
DROP PROCEDURE getuserflagsforuseroftype
DROP PROCEDURE getuserflagsoftype
DROP PROCEDURE setuserflag
DROP PROCEDURE unsetuserflag

DROP PROCEDURE DeleteInclude
DROP PROCEDURE GetHighestUserId

-- 2005-06-17 Kreiger - Issue 3347


BEGIN TRANSACTION
SET QUOTED_IDENTIFIER ON
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE
SET ARITHABORT ON
SET NUMERIC_ROUNDABORT OFF
SET CONCAT_NULL_YIELDS_NULL ON
SET ANSI_NULLS ON
SET ANSI_PADDING ON
SET ANSI_WARNINGS ON
COMMIT
BEGIN TRANSACTION
ALTER TABLE dbo.sys_data
	DROP CONSTRAINT FK_sys_data_sys_types
GO
COMMIT
BEGIN TRANSACTION
CREATE TABLE dbo.Tmp_sys_data
	(
	sys_id tinyint NOT NULL IDENTITY (1, 1),
	type_id tinyint NOT NULL,
	[value] varchar(1000) NULL
	)  ON [PRIMARY]
GO
SET IDENTITY_INSERT dbo.Tmp_sys_data ON
GO
IF EXISTS(SELECT * FROM dbo.sys_data)
	 EXEC('INSERT INTO dbo.Tmp_sys_data (sys_id, type_id, [value])
		SELECT sys_id, type_id, [value] FROM dbo.sys_data TABLOCKX')
GO
SET IDENTITY_INSERT dbo.Tmp_sys_data OFF
GO
DROP TABLE dbo.sys_data
GO
EXECUTE sp_rename N'dbo.Tmp_sys_data', N'sys_data', 'OBJECT'
GO
ALTER TABLE dbo.sys_data ADD CONSTRAINT
	PK_sys_data PRIMARY KEY NONCLUSTERED
	(
	sys_id,
	type_id
	) ON [PRIMARY]

GO
ALTER TABLE dbo.sys_data WITH NOCHECK ADD CONSTRAINT
	FK_sys_data_sys_types FOREIGN KEY
	(
	type_id
	) REFERENCES dbo.sys_types
	(
	type_id
	)
GO
COMMIT

-- 2005-07-01 Lennart � - Issue 3072, Increasing the size for system data value to varchar 1000.
