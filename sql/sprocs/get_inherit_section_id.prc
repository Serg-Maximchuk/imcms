SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS OFF 
GO

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[get_inherit_section_id]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[get_inherit_section_id]
GO

CREATE PROCEDURE get_inherit_section_id
  @parent_meta_id int

AS

SELECT s.section_id, s.section_name
 FROM sections s, meta_section ms, meta m
where m.meta_id=ms.meta_id
and m.meta_id=@parent_meta_id
and ms.section_id=s.section_id
GO
SET QUOTED_IDENTIFIER OFF 
GO
SET ANSI_NULLS ON 
GO

