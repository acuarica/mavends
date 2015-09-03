
with 
  je   as (select * from jarentry where originalsize > 0),
	  crc as (select distinct crc32 from je),
		  cf as (select * from jarentry where filename like '%.class' and originalsize > 0)
			select
			  (select sum(originalsize)*1.0/(1024*1024) from jarentry) as totaloriginalsize,
				  (select sum(compressedsize)*1.0/(1024*1024) from jarentry) as totalcompressedsize,
					  (select count(*) from jarentry) as jarentrycount,
						  (select count(*) from je) as fileentries,
							  (select count(*) from cf) as classentries,
								  (select sum(originalsize)*1.0/(1024*1024) from cf) as cforiginalsize,
									  (select sum(compressedsize)*1.0/(1024*1024) from cf) as cfcompressedsize,
										  (select count(*) from crc) as nocrc32;

