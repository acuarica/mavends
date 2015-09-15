
--
-- Header index properties.
-- This table should contain only 1 row.
--
create table header (
  headb        int   not null,
  creationdate date  not null
);

--
-- Contains all artifacts (with classifiers, e.g., 'sources' or 'javadoc').
-- Along each column is the original Nexus field 
-- that this column was taken from.
-- The fields (groupid, artifactid, version, classifier, packaging) are 
-- almost a primary key, because the classifier field is the only one
-- of those fields that can be null.
--
create table artifact (
  coordid      integer      primary key, -- Rowid alias
  groupid      varchar(128) not null,    -- Group name ( u[0] )
  artifactid   varchar(128) not null,    -- Artifact ID ( u[1] )
  version      varchar(64)  not null,    -- Version ( u[2] )
  classifier   varchar(64),              -- Secondary artifacts, e.g., 'sources'.
  packaging    varchar(64)  not null,    -- i[0] == u[4] if not null
  idate        date         not null,    -- ( i[1] )
  size         int          not null,    -- ( i[2] )
  is3          int          not null,    -- ( i[3] )
  is4          int          not null,    -- ( i[4] )
  is5          int          not null,    -- ( i[5] )
  extension    varchar(64)  not null,    -- Artifact file extension. ( i[6] )
  mdate        date         not null,    -- Modified date
  sha1         varchar(40),              -- SHA-1
  artifactname text,                     -- Artifact name
  artifactdesc text,                     -- Artifact description
  unique (groupid, artifactid, version, classifier, packaging)
);

---
--- Deleted artifacts.
--- These artifacts are not present in the Maven Repository.
---
create table del ( 
  groupid    varchar(128) not null, -- Group name ( u[0] )
  artifactid varchar(128) not null, -- Artifact ID ( u[1] )
  version    varchar(64)  not null, -- Version ( u[2] )
  classifier varchar(64),           -- Secondary artifacts, e.g., 'sources'.
  packaging  varchar(64),           -- i[0] == u[4] if not null
  mdate      date         not null  -- Modified date
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
create view artifact_view as
  select
    rowid as coorid,
    substr(groupid || '.', 1, instr(groupid || '.', '.') - 1) as rootgroup,
    groupid || ':' || artifactid || '@' || version ||  ifnull('/' || classifier, '') || '.' || extension as id,
    replace(groupid, '.', '/') || '/' || artifactid || '/' || version || '/' || artifactid || '-' || version || ifnull('-' || classifier, '') || '.' || extension as path,
    * 
  from artifact;

-- Main artifacts.
create view artifact_main as
  select * from artifact_view where classifier is null;

-- Secondary artifacts.
create view artifact_secondary as
  select * from artifact_view where classifier is not null;

-- Jar main artifacts.
create view artifact_jar as
  select * from artifact_main where packaging = 'jar' and extension = 'jar';


-- Quick stats of the Maven Index.
-- Similar to http://search.maven.org/#stats.
create view stats as
  select 
    (select count(*) from artifact)                                                     as gavcp,  -- Total number of artifacts and secondary artifacts (GAVCP).                               
    (select count(*) from (select distinct groupid, artifactid, version from artifact)) as gav,    -- Total number of artifacts indexed (GAV). 
    (select count(*) from (select distinct groupid, artifactid from artifact))          as ga,     -- Total number of unique artifacts indexed (GA).
    (select sum(size)/(1024*1024) from artifact)                                        as size;   -- Estimated size of the entire repository (in MB).             

-- Estimated size of the repository by 
-- packaging, extension (in MB). 
create view sizestats_by_packaging_extension as
  select 
    packaging, 
	extension, 
	sum(size)/(1024*1024) as size 
  from artifact_main 
  group by packaging, extension;

-- Estimated size of the repository by 
-- packaging, extension, rootgroup (in MB).
create view sizestats_by_packaging_extension_rootgroup as
  select 
    packaging, 
	extension, 
	rootgroup, 
	sum(size)/(1024*1024) as size 
  from artifact_main 
  group by packaging, extension, rootgroup;
