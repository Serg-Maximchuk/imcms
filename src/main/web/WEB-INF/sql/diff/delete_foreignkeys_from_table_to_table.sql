DECLARE @table_name VARCHAR(256)
DECLARE @referenced_table_name VARCHAR(256)

SET @table_name = ?
SET @referenced_table_name = ?

DECLARE constraints_cursor CURSOR FOR
SELECT constraints.name
FROM   sysobjects constraints,
       sysobjects tables,
       sysobjects referenced_tables,
       sysforeignkeys foreignkeys
WHERE  foreignkeys.constid    = constraints.id
AND    foreignkeys.fkeyid     = tables.id 
AND    foreignkeys.rkeyid     = referenced_tables.id 
AND    tables.name            = @table_name
AND    referenced_tables.name = @referenced_table_name

OPEN constraints_cursor

DECLARE @constraint_name VARCHAR(256)
FETCH NEXT FROM constraints_cursor
INTO @constraint_name

WHILE @@FETCH_STATUS = 0
BEGIN

    EXEC('ALTER TABLE '+@table_name+' DROP CONSTRAINT '+@constraint_name)

    FETCH NEXT FROM constraints_cursor
    INTO @constraint_name

END

CLOSE constraints_cursor
DEALLOCATE constraints_cursor