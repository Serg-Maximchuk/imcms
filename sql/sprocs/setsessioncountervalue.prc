CREATE PROCEDURE SetSessionCounterValue
 @value int 
AS
 update sys_data
 set ivalue = @value
 where type_id = 1
