ALTER TABLE category_types ADD inherited BIT
GO
UPDATE category_types SET inherited = 1 WHERE inherited IS NULL
GO
ALTER TABLE category_types ALTER COLUMN inherited BIT NOT NULL
GO
ALTER TABLE category_types ADD CONSTRAINT UQ__category_types__name UNIQUE ( name )
GO

-- 2005-04-14 Kreiger
