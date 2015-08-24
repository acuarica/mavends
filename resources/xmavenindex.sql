
--
-- Contains all artifacts and its classifiers (e.g., 'sources' or 'javadoc').
-- Along each column is the original Nexus field 
-- that this column was taken from.
--
create table art (
  groupname   varchar(64)    not null,  -- Group name ( u[0] )
  artname     varchar(128)   not null,  -- Artifact ID ( u[1] )
  version     varchar(64)    not null,  -- Version ( u[2] )
  classifier  varchar(32),              -- Secondary artifacts (e.g., 'sources' or 'javadoc'). ( u[3] )
  packaging   varchar(32)    not null,  -- i[0] == u[4] if not null
  idate       date           not null,  -- ( i[1] )
  size        integer        not null,  -- ( i[2] )
  is3         integer        not null,  -- ( i[3] )
  is4         integer        not null,  -- ( i[4] )
  is5         integer        not null,  -- ( i[5] )
  ext         varchar(64)    not null,  -- Artifact extension ( i[6] )
  mdate       date           not null,  -- m
  sha         varchar(64),              -- SHA1 hash (optional) ( 1 )
  arttitle    text,                     -- Artifact title ( n )
  artdesc     text,                     -- Artifact description ( d )
  unique (groupname, artname, version, classifier, packaging)
);

--
-- Several misc index properties.
-- This table should contain only 1 row.
--
create table properties (
  DESCRIPTOR   varchar(255) not null,
  IDXINFO      varchar(255) not null,
  headb        varchar(32)  not null,
  creationdate date         not null
);

--
-- Contains all maven groups.
--
create table allgroups (
  groupname varchar(64) not null primary key
);

--
-- Contains only the root groups.
--
create table rootgroups (
  groupname varchar(64) not null primary key
);
