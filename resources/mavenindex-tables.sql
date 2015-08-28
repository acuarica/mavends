
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
  artifactdesc  text--,                       -- Artifact description ( d )
  --unique (groupid, artifactid, version, classifier, packaging)
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
