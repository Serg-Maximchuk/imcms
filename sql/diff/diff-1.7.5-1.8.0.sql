-- Diff from 1_7_5-RELEASE up to 1_8_0-RELEASE

update doc_types
set type = 'New Page' where doc_type = 2 and lang_prefix = 'en'

update doc_types
set type = 'External Link' where doc_type = 5 and lang_prefix = 'en'

update doc_types
set type = 'Upload file' where doc_type = 8 and lang_prefix = 'en'

update doc_types
set type = 'Browser controlled link' where doc_type = 6 and lang_prefix = 'en'

update doc_types
set type = 'Chat page' where doc_type = 103 and lang_prefix = 'en'

update doc_types
set type = 'Bulletin board' where doc_type = 104 and lang_prefix = 'en'

update doc_types
set type = 'Conference page' where doc_type = 102 and lang_prefix = 'en'

-- 2004-01-12 Lennart �

update display_name
set display_name = 'Page type'
where sort_by_id = 3 AND lang_id = 2

-- 2004-01-14 Hasse
-- 1_8_0-RELEASE


print ' OBS !!!!! '
print 'F�ljande �tg�rder beh�ver genomf�ras efter detta script '
print ''
print '1. Du M�STE k�ra hela "sprocs.sql" som finns i "dist" katalogen'
print ''

GO

