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