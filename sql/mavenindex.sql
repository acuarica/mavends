
--
-- Contains all artifacts and its classifiers (e.g., 'sources' or 'javadoc').
-- Along each column is the original Nexus field 
-- that this column was taken from.
-- The fields (groupid, artifactid, version, classifier, packaging) are 
-- almost a primary key, because the classifier field is the only one
-- of those fields that can be null.
--
create table artifact (
  coorid        integer       primary key,  -- Autoincrement, rowid alias. 
  groupid       varchar(128)  not null,     -- Group name ( u[0] )
  artifactid    varchar(128)  not null,     -- Artifact ID ( u[1] )
  version       varchar(64)   not null,     -- Version ( u[2] )
  classifier    varchar(64),                -- Secondary artifacts (e.g., 'sources' or 'javadoc'). ( u[3] )
  packaging     varchar(64)   not null,     -- i[0] == u[4] if not null
  idate         date          not null,     -- ( i[1] )
  size          integer       not null,     -- ( i[2] )
  is3           integer       not null,     -- ( i[3] )
  is4           integer       not null,     -- ( i[4] )
  is5           integer       not null,     -- ( i[5] )
  extension     varchar(64)   not null,     -- Artifact file extension. ( i[6] )
  mdate         date          not null,     -- m
  sha           varchar(128),               -- SHA1 hash (optional) ( 1 )
  artifactname  text,                       -- Artifact title ( n )
  artifactdesc  text,                       -- Artifact description ( d )
  unique (groupid, artifactid, version, classifier, packaging)
);

--
-- Deleted artifacts.
-- These artifacts are not present in the Maven Repository.
--
create table del ( 
  groupid       varchar(128)  not null,     -- Group name ( u[0] )
  artifactid    varchar(128)  not null,     -- Artifact ID ( u[1] )
  version       varchar(64)   not null,     -- Version ( u[2] )
  classifier    varchar(64),                -- Secondary artifacts (e.g., 'sources' or 'javadoc'). ( u[3] )
  packaging     varchar(64),                -- i[0] == u[4] if not null
  mdate         date          not null,     -- Modified date
  unique (groupid, artifactid, version, classifier, packaging)
);

--
-- Header index properties.
-- This table should contain only 1 row.
--
create table header (
  headb        varchar(32)  not null,
  creationdate date         not null
);

--
-- Descriptor index properties.
-- This table should contain only 1 row.
--
create table descriptor (
  DESCRIPTOR   varchar(255) not null,
  IDXINFO      varchar(255) not null
);

--
-- Contains all maven groups.
--
create table allgroups (
  groupid varchar(128) not null primary key
);

--
-- Contains only the root groups.
--
create table rootgroups (
  groupid varchar(128) not null primary key
);



--
-- Adds to the artifact table the rootgroup (using the groupid) and
-- an unique id string to identify the artifact. 
--

drop view if exists artifact_view;

create view artifact_view as
  select
    substr(groupid || '.', 1, instr(groupid || '.', '.') - 1) as rootgroup,
    groupid || ':' || artifactid || '@' || version ||  ifnull('/' || classifier, '') || '.' || extension as id,
    replace(groupid, '.', '/') || '/' || artifactid || '/' || version || '/' || artifactid || '-' || version || ifnull('-' || classifier, '') || '.' || extension as path,
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
drop view if exists stats;

create view stats as
  select 
    (select count(*) from artifact)                                                     as gavcp,  -- Total number of artifacts and secondary artifacts (GAVCP).                               
    (select count(*) from (select distinct groupid, artifactid, version from artifact)) as gav,    -- Total number of artifacts indexed (GAV). 
    (select count(*) from (select distinct groupid, artifactid from artifact))          as ga,     -- Total number of unique artifacts indexed (GA).
    (select sum(size)/(1024*1024) from artifact)                                        as size;   -- Estimated size of the entire repository (in MB).             

--
-- Estimated size of the repository by 
-- packaging, extension (in MB).
-- 

drop view if exists sizestats_by_packaging_extension;

create view sizestats_by_packaging_extension as
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

drop view if exists sizestats_by_packaging_extension_rootgroup;

create view sizestats_by_packaging_extension_rootgroup as
  select 
    packaging, 
	extension, 
	rootgroup, 
	sum(size)/(1024*1024) as size 
  from artifact_main 
  group by packaging, extension, rootgroup;