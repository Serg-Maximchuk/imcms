CREATE PROCEDURE SystemMessageSet
 @newMsg varchar(1000)
AS
UPDATE sys_data
SET ivalue = @newMsg
WHERE type_id = 3
