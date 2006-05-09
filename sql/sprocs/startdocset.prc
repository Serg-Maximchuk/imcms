CREATE PROCEDURE StartDocSet @meta_id INT AS
/**
	Changes the start document
**/

UPDATE sys_data SET ivalue = @meta_id WHERE sys_id = 0
