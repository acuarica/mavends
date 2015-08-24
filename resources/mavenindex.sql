

--
-- groupname is unique
--
create table allgroups (
  groupid    integer     primary key,  -- rowid alias
  groupname  varchar(64) not null      -- Group name
);

--
-- unique (groupid, artname)
--
create table allarts (
  artid       integer        primary key,  -- rowid alias
  groupid     integer        not null,     -- Group ID ( u[0] )
  artname     varchar(128)   not null,     -- Artifact Name ( u[1] )
  arttitle    text,                        -- Artifact title ( n )
  artdesc     text                         -- Artifact description ( d )
);

--
--
--
create view allarts_view as
  select 
    allarts.artid, 
    allarts.groupid, 
    (select allgroups.groupname 
       from allgroups
      where allgroups.groupid=allarts.groupid) as groupname,
    allarts.artname,
    allarts.arttitle,
    allarts.artdesc
  from allarts;

--
-- unique (artid, version)
--
create table art (
  pomid       integer primary key, -- rowid alias
  artid       int   not null,  -- Artifact ID ( u[1] )
  version     varchar(64)    not null,  -- Version ( u[2] )
  packaging   varchar(32)    not null,  -- i[0] == u[4] if not null
  idate       date           not null,  -- ( i[1] )
  size        integer        not null,  -- ( i[2] )
  is3         integer        not null,  -- ( i[3] )
  is4         integer        not null,  -- ( i[4] )
  is5         integer        not null,  -- ( i[5] )
  ext         varchar(64)    not null,  -- Artifact extension ( i[6] )
  mdate       date           not null,  -- m
  sha         varchar(64)               -- SHA1 hash (optional) ( 1 )
);

--
--
--
create view art_view as
  select
    art.pomid,
    art.artid,
    allarts_view.groupid,
    allarts_view.groupname,
    allarts_view.artname,
    allarts_view.arttitle,
    allarts_view.artdesc,
    art.version,
    art.packaging,
    art.idate,
    art.size,
    art.is3,
    art.is4,
    art.is5,
    art.ext,
    art.mdate,
    art.sha
  from art
  inner join allarts_view on allarts_view.artid=art.artid;

--
-- unique (pomid, classifier, packaging)
--
create table sec (
  secid       integer primary key, -- rowid alias
  pomid       int not null,
  classifier  varchar(32)  not null,
  packaging   varchar(32)    not null,  -- i[0] == u[4] if not null
  idate       date           not null,  -- ( i[1] )
  size        integer        not null,  -- ( i[2] )
  is3         integer        not null,  -- ( i[3] )
  is4         integer        not null,  -- ( i[4] )
  is5         integer        not null,  -- ( i[5] )
  ext         varchar(64)    not null,  -- Artifact extension ( i[6] )
  mdate       date           not null,  -- m
  sha         varchar(64)               -- SHA1 hash (optional) ( 1 )
);


--
--
--
create view sec_view as
  select
    sec.secid,
    art_view.pomid,
    art_view.artid,
    art_view.groupid,
    art_view.groupname,
    art_view.artname,
    art_view.arttitle,
    art_view.artdesc,
    art_view.version,
    sec.classifier,
    sec.packaging,
    sec.idate,
    sec.size,
    sec.is3,
    sec.is4,
    sec.is5,
    sec.ext,
    sec.mdate,
    sec.sha
  from sec
  inner join art_view on art_view.pomid=sec.pomid;



attach "out/xmavenindex.sqlite3" as mi; 

insert into allgroups (groupname)
  select distinct groupname 
  from mi.art 
  order by groupname;

insert into allarts (groupid, artname, arttitle, artdesc)
  select 
    (select allgroups.groupid 
       from allgroups 
      where allgroups.groupname=mi.art.groupname), 
    mi.art.artname, 
    mi.art.arttitle, 
    mi.art.artdesc 
  from mi.art 
  group by mi.art.groupname, mi.art.artname 
  having mi.art.idate= max(mi.art.idate);

insert into art (artid, version, packaging, idate, size, is3, is4, is5, ext, mdate, sha)
  select
    allarts_view.artid,
    mi.art.version, 
    mi.art.packaging, 
    mi.art.idate, 
    mi.art.size, 
    mi.art.is3, 
    mi.art.is4, 
    mi.art.is5, 
    mi.art.ext, 
    mi.art.mdate, 
    mi.art.sha
  from mi.art
  inner join allarts_view 
          on allarts_view.groupname = mi.art.groupname 
         and allarts_view.artname   = mi.art.artname
  where mi.art.classifier is null;

insert into sec (pomid, classifier, packaging, idate, size, is3, is4, is5, ext, mdate, sha)
  select
    art_view.pomid,
    mi.art.classifier, 
    mi.art.packaging, 
    mi.art.idate, 
    mi.art.size, 
    mi.art.is3, 
    mi.art.is4, 
    mi.art.is5, 
    mi.art.ext, 
    mi.art.mdate, 
    mi.art.sha
  from mi.art
  inner join art_view 
          on art_view.groupname = mi.art.groupname 
         and art_view.artname   = mi.art.artname
         and art_view.version   = mi.art.version
  where mi.art.classifier is not null;
