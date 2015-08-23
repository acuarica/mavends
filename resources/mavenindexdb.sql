
/*
 * Contains all pom files.
 * Along each column is the original Nexus field that this column was taken from.
 */
create table pom (
  gid         varchar(128)   not null,  -- Group ID ( u[0] )
  aid         varchar(128)   not null,  -- Artifact ID ( u[1] )
  ver         varchar(64)    not null,  -- Version ( u[2] )
  packaging   varchar(32)    not null,  -- i[0] == u[4] if not null
  idate       date           not null,  -- ( i[1] )
  size        integer        not null,  -- ( i[2] )
  is3         integer        not null,  -- ( i[3] )
  is4         integer        not null,  -- ( i[4] )
  is5         integer        not null,  -- ( i[5] )
  ext         varchar(64)    not null,  -- Artifact extension ( i[6] )
  mdate       date           not null,  -- m
  sha         varchar(64),              -- SHA1 hash (optional) ( 1 )
  gdesc       text,                     -- Group description ( n )
  adesc       text,                     -- Artifact description ( d )
  path        varchar(255)   not null,  -- Relative path of the artifact within a repo. 
  primary key (gid, aid, ver)
);

/*
 * Contains all main artifacts.
 * Along each column is the original Nexus field that this column was taken from.
 */
create table art (
  gid         varchar(128)   not null,  -- Group ID ( u[0] )
  aid         varchar(128)   not null,  -- Artifact ID ( u[1] )
  ver         varchar(64)    not null,  -- Version ( u[2] )
  packaging   varchar(32)    not null,  -- i[0] == u[4] if not null
  idate       date           not null,  -- ( i[1] )
  size        integer        not null,  -- ( i[2] )
  is3         integer        not null,  -- ( i[3] )
  is4         integer        not null,  -- ( i[4] )
  is5         integer        not null,  -- ( i[5] )
  ext         varchar(64)    not null,  -- Artifact extension ( i[6] )
  mdate       date           not null,  -- m
  sha         varchar(64),              -- SHA1 hash (optional) ( 1 )
  gdesc       text,                     -- Group description ( n )
  adesc       text,                     -- Artifact description ( d )
  path        varchar(255)   not null,  -- Relative path of the artifact within a repo.
  primary key (gid, aid, ver)
);

/*
 * Contains all secondary artifacts (e.g., 'sources' or 'javadoc').
 * Along each column is the original Nexus field that this column was taken from.
 */
create table sec (
  gid         varchar(128)   not null,  -- Group ID ( u[0] )
  aid         varchar(128)   not null,  -- Artifact ID ( u[1] )
  ver         varchar(64)    not null,  -- Version ( u[2] )
  classifier  varchar(32)    not null,  -- Used for secondary artifacts (e.g., 'sources' or 'javadoc'). ( u[3] )
  packaging   varchar(32)    not null,  -- i[0] == u[4] if not null
  idate       date           not null,  -- ( i[1] )
  size        integer        not null,  -- ( i[2] )
  is3         integer        not null,  -- ( i[3] )
  is4         integer        not null,  -- ( i[4] )
  is5         integer        not null,  -- ( i[5] )
  ext         varchar(64)    not null,  -- Artifact extension ( i[6] )
  mdate       date           not null,  -- m
  sha         varchar(64),              -- SHA1 hash (optional) ( 1 )
  gdesc       text,                     -- Group description ( n )
  adesc       text,                     -- Artifact description ( d )
  path        varchar(255)   not null,  -- Relative path of the artifact within a repo.
  primary key (gid, aid, ver, classifier, packaging)
);

/*
 * Several misc index properties. This table should contain only 1 row.
 */
create table properties (
  DESCRIPTOR   varchar(255) not null,
  IDXINFO      varchar(255) not null,
  headb        varchar(32)  not null,
  creationdate date         not null
);

/*
 * Contains all maven groups.
 */
create table allgroups (
  value varchar(64) not null primary key
);

/*
 * Contains only the root groups of the Maven Repository.
 */
create table rootgroups (
  value varchar(64) not null primary key
);
