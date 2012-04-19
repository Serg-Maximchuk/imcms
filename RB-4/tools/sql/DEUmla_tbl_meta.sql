

/*
Change extended characters in column meta.meta_headline and meta.meta_text where meta_id > 1000
*/

-- table meta
declare @meta_id int

create table #tmpmeta(

	meta_id int not null,
	meta_headline varchar (255) collate Finnish_Swedish_CS_AS ,
        meta_text varchar(1000) collate Finnish_Swedish_CS_AS 

)

declare posCursor  Cursor scroll 
for select meta_id 
    from meta
    where meta_id > 1000  

open posCursor
fetch next from posCursor 
into @meta_id
while @@fetch_status = 0
   begin
        insert into #tmpmeta (meta_id, meta_headline, meta_text)
        select meta_id, meta_headline, meta_text from meta where meta_id =@meta_id 
               
       
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&aring;', '�'),
			       meta_text =  replace((select meta_text from #tmpmeta where meta_id =@meta_id ),'&aring;', '�')	
        where meta_id = @meta_id 
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id),'&Aring;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&Aring;', '�')
	where meta_id = @meta_id 
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id),'&auml;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&auml;', '�')
	where meta_id = @meta_id 
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&Auml;', '�'),
       				meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&Auml;', '�')
	where meta_id = @meta_id
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&ouml;', '�'),
 				meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&ouml;', '�')
        where meta_id = @meta_id
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&Ouml;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&Ouml;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&amp;', '&'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&amp;', '&')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&quot;', '"'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&quot;', '"')
	where meta_id = @meta_id               
	              
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&eacute;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&eacute;', '�')
	where meta_id = @meta_id 
        update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&acute;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&acute;', '�')
	where meta_id = @meta_id
        update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&Eacute;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&Eacute;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&aacute;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&aacute;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&Aacute;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&Aacute;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&oslash;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&oslash;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&Oslash;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&Oslash;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&agrave;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&agrave;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&Agrave;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&Agrave;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&egrave;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&egrave;', '�')
	where meta_id = @meta_id               
	update #tmpmeta set meta_headline = replace((select meta_headline from #tmpmeta where meta_id =@meta_id ),'&Egrave;', '�'),
        			meta_text = replace((select meta_text from #tmpmeta where meta_id =@meta_id),'&Egrave;', '�')
	where meta_id = @meta_id               


       
        update meta set meta_headline = (select meta_headline from #tmpmeta where meta_id =@meta_id),
			meta_text = (select meta_text from #tmpmeta where meta_id =@meta_id)
	where meta_id = @meta_id
						    
   	fetch next from posCursor 
   	into @meta_id
   end
close posCursor
deallocate posCursor

drop table #tmpmeta
GO 



	

