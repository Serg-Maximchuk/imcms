-- Diff from 1_7_4-RELEASE to 1_7_5-RELEASE

if exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[AddPhoneNr]') and OBJECTPROPERTY(id, N'IsProcedure') = 1)
drop procedure [dbo].[AddPhoneNr]
GO

-- 2003-11-28 Lennart
-- 1_7_5-RELEASE

print ' OBS !!!!! '
print 'F�ljande �tg�rder beh�ver genomf�ras efter detta script '
print ''
print '1. Du M�STE k�ra hela "sprocs.sql" som finns i "dist" katalogen'
print ''

GO