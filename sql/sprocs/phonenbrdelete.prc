SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS ON 
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[PhoneNbrDelete]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[PhoneNbrDelete]
GO


CREATE PROCEDURE PhoneNbrDelete
/*
 Deletes an Ip-access for a user. Used by the PhoneNbrDelete
*/
 @phoneId int
AS
DELETE FROM PHONES 
WHERE phone_id = @phoneId


GO
SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS ON 
GO

