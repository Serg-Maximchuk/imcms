SET QUOTED_IDENTIFIER ON 
GO
SET ANSI_NULLS ON 
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Poll_AddAnswer]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[Poll_AddAnswer]
GO



/*
=============================================
Procedure Poll_AddAnswer
Add a new poll answer
=============================================
*/


CREATE  PROCEDURE dbo.Poll_AddAnswer
	@question_id int,
	@text_id int,
	@option_no int
	
	
AS
	INSERT INTO poll_answers ( question_id, text_id, option_number )
	VALUES ( @question_id, @text_id, @option_no )

GO
SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS ON 
GO

