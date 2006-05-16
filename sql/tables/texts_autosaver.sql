if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[texts_autosaver]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
drop table [dbo].[texts_autosaver]
GO

CREATE TABLE [dbo].[texts_autosaver] (
	[id] [int] IDENTITY (1, 1) NOT NULL ,
	[unique_text_id] [varchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[date_time] [datetime] NOT NULL ,
	[type] [int] NULL ,
	[text] [ntext] COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[user_id] [int] NULL 
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

