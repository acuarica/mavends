

attach "mavenindex.sqlite3"         as mi; 
attach "mavenbytecode-test.sqlite3" as mb; 

    
/*
 * Contains all main artifacts.
 * Along each column is the original Nexus field that this column was taken from.
 */
create table art (
  pid         integer        primary key, -- rowid alias
  gid         varchar(128)   not null,  -- Group ID ( u[0] )
  aid         varchar(128)   not null,  -- Artifact ID ( u[1] )
  ver         varchar(64)    not null,  -- Version ( u[2] )
  packaging   varchar(32)    not null,  -- i[0] == u[4] if not null
  idate       date           not null,  -- ( i[1] )
  size        integer        not null,  -- ( i[2] )
  ext         varchar(64)    not null,  -- Artifact extension ( i[6] )
  mdate       date           not null,  -- m
  sha         varchar(64),              -- SHA1 hash (optional) ( 1 )
  gdesc       text,                     -- Group description ( n )
  adesc       text,                     -- Artifact description ( d )
  unique (gid, aid, ver)
);

/*
 * art table data.
 */
insert into art (gid, aid, ver, packaging, idate, size, ext, mdate, sha, gdesc, adesc) 
  select 
    gid, 
    aid, 
    ver, 
    packaging, 
    date(idate, 'unixepoch' ), 
    size, 
    ext, 
    date(mdate, 'unixepoch' ), 
    sha, 
    gdesc, 
    adesc
  from mi.art;

/*
 * 
 */
create table jarentry (
  eid             integer       primary key,
  pid             int not null,
  filename        varchar(255)  not null, 
  originalsize    int           not null,
  compressedsize  int           not null,
  unique (pid, filename)
);

insert into jarentry (pid, filename, originalsize, compressedsize)
  select 
    (select pid from art a where a.gid=j.gid and a.aid=j.aid and a.ver=j.ver), 
    filename, 
    originalsize, 
    compressedsize
  from mb.jarentry j;


create view jarentry_view as
  select 
    art.gid,
    art.aid,
    art.ver,
    filename, 
    originalsize, 
    compressedsize
  from jarentry 
  inner join art   on art.pid = jarentry.pid;

/*
 * 
 */
create table cp_class (
  nameid  integer       primary key, -- rowid alias
  name    varchar(255)  not null unique
);

insert into cp_class (name) 
    select classname as name 
    from mb.class 
  union 
    select supername as name 
    from mb.class;

create table cp_sig (
  sigid  integer       primary key, -- rowid alias
  sig    varchar(255)  not null
);

insert into cp_sig (sig) 
  select distinct signature 
  from mb.class 
  where signature is not null;

create table class (
  cid        integer primary key,
  pid         int not null,
  nameid       int  not null,
  --nameid       integer primary key,
  supernameid  int  not null,
  version      int  not null, 
  access       int  not null, 
  sigid        int--,
 -- primary key (nameid)
);

insert into class (pid, nameid, supernameid, version, access, sigid)
  select 
    (select pid from art a where a.gid=c.gid and a.aid=c.aid and a.ver=c.ver),
    (select rowid from cp_class where name = c.classname), 
    (select rowid from cp_class where name = c.supername), 
    version, 
    access, 
    (select rowid from cp_sig where sig = c.signature)
  from mb.class c;

create view class_view as
  select 
    cid,
    art.gid,
    art.aid,
    art.ver,
    cp.name, 
    cps.name as supername, 
    c.version, 
    c.access, 
    cs.sig
  from class c 
  inner join cp_class cp  on cp.rowid  = c.nameid 
  inner join cp_class cps on cps.rowid = c.supernameid
  left  join cp_sig   cs  on cs.rowid  = c.sigid
  inner join art      on art.pid = c.pid;


create table method (
  mid  integer primary key,
  cid int not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  unique (cid, methodname, methoddesc)
);

insert into method (cid, methodname, methoddesc)
  select 
    (select cid 
      from class_view cw 
      where cw.gid=m.gid and cw.aid=m.aid and cw.ver=m.ver and cw.name=m.classname),
    methodname,
    methoddesc
  from mb.method m;

create view method_view as
  select 
    m.mid,
    m.cid,
    cw.gid,
    cw.aid,
    cw.ver,
    cw.name,
    methodname,
    methoddesc
  from method m
  inner join class_view cw on cw.cid = m.cid;
 
--
-- 
-- 
create table callsite (
  mid    int not null,
  offset        int           not null,
  targetclass   varchar(255)  not null,
  targetmethod  varchar(255)  not null,
  targetdesc    varchar(255)  not null,
  primary key (mid, offset)
);

insert into callsite (mid, offset, targetclass, targetmethod, targetdesc)
  select
    (select mid 
      from method_view mw 
      where mw.gid=cs.gid 
        and mw.aid=cs.aid 
        and mw.ver=cs.ver 
        and mw.name=cs.classname 
        and mw.methodname=cs.methodname 
        and mw.methoddesc=cs.methoddesc),
    offset,
    targetclass, 
    targetmethod, 
    targetdesc
  from mb.callsite cs;

/*
 
create view bytecode as
 select * from (
select clsname, methodname, methoddesc, offset, 'invoke' as op, targetclass || '.' || targetmethod || targetdesc as args from callsite
union all
select clsname, methodname, methoddesc, offset, opcode as op, type as args from allocsite
union all
select clsname, methodname, methoddesc, offset, 'get/put', targetclass  || '.' ||  targetfield || targetdesc as args from fieldaccess
union all
select clsname, methodname, methoddesc, offset, 'ldc', literal from literal
union all
select clsname, methodname, methoddesc, offset, opcode, '' from zero
)
order by clsname, methodname, methoddesc, offset;

create view bytecode as
 select * from (
select gid, aid, ver, classname, methodname, methoddesc, offset, 'invoke' as op, targetclass || '.' || targetmethod || targetdesc as args from callsite
union all
select gid, aid, ver, classname, methodname, methoddesc, offset, opcode as op, type as args from allocsite
union all
select gid, aid, ver, classname, methodname, methoddesc, offset, 'get/put', targetclass  || '.' ||  targetfield || targetdesc as args from fieldaccess
union all
select gid, aid, ver, classname, methodname, methoddesc, offset, 'ldc', literal from literal
union all
select gid, aid, ver, classname, methodname, methoddesc, offset, opcode, '' from zero
)
order by gid, aid, ver, classname, methodname, methoddesc, offset;


create view subclass as 
WITH RECURSIVE subclass(root, cls) AS (
select distinct supername, supername from class
UNION 
select root, class.name from class, subclass where class.supername=subclass.cls )
SELECT root, cls FROM subclass
 




 */



/*
 * To re-everything.  
 */
vacuum;





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
