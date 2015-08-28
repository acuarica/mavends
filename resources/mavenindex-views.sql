
--
-- Adds to the artifact table the rootgroup (using the groupid) and
-- an unique id string to identify the artifact. 
--

drop view if exists artifact_view;

create view artifact_view as
  select
    substr(groupid || '.', 1, instr(groupid || '.', '.') - 1) as rootgroup,
    groupid || ':' || artifactid || '@' || version ||  ifnull('/' || classifier, '') || '.' || extension as id, 
    * 
  from artifact;

--
--
--

drop view if exists artifact_main;

create view artifact_main as
  select * from artifact_view where classifier is null;

--
-- 
--

drop view if exists artifact_secondary;

create view artifact_secondary as
  select * from artifact_view where classifier is not null;

--
--
--

drop view if exists artifact_jar;

create view artifact_jar as
  select * from artifact_main where packaging = 'jar' and extension = 'jar';

--
-- Quick stats of the Maven Index.
-- Similar to http://search.maven.org/#stats.
--
drop table if exists stats;

create table stats as
  select 
    (select count(*) from artifact)                                                     as gavcp,  -- Total number of artifacts and secondary artifacts (GAVCP).                               
    (select count(*) from (select distinct groupid, artifactid, version from artifact)) as gav,    -- Total number of artifacts indexed (GAV). 
    (select count(*) from (select distinct groupid, artifactid from artifact))          as ga,     -- Total number of unique artifacts indexed (GA).
    (select sum(size)/(1024*1024) from artifact)                                        as size;   -- Estimated size of the entire repository (in MB).             

--
-- Estimated size of the repository by 
-- packaging, extension (in MB).
-- 

drop table if exists sizestats_by_packaging_extension;

create table sizestats_by_packaging_extension as
  select 
    packaging, 
	extension, 
	sum(size)/(1024*1024) as size 
  from artifact_main 
  group by packaging, extension;

--
-- Estimated size of the repository by 
-- packaging, extension, rootgroup (in MB).
--    

drop table if exists sizestats_by_packaging_extension_rootgroup;

create table sizestats_by_packaging_extension_rootgroup as
  select 
    packaging, 
	extension, 
	rootgroup, 
	sum(size)/(1024*1024) as size 
  from artifact_main 
  group by packaging, extension, rootgroup;
