

/*
 * https://www.sqlite.org/pragma.html#pragma_page_size
 * 
 * Better performance by using the same page size as the OS?
 */
--pragma page_size = 4096;

/*
 * https://www.sqlite.org/pragma.html#pragma_journal_mode
 * 
 * Better performance by disabling journaling.
 * No need for journal since the db once is built, becomes read-only.
 */
--pragma journal_mode = off;

attach "mavenindex.sqlite3"         as mi; 
attach "mavenbytecode-test.sqlite3" as mb; 

/*
 * Contains some general stats of the Maven Index. 
 */
create table stats ( 
  DESCRIPTOR   varchar(255) not null,
  IDXINFO      varchar(255) not null,
  headb        varchar(32)  not null,
  creationdate date         not null,
  nart         integer      not null,
  gav          integer      not null,
  ga           integer      not null
);

/*
 * stats table data
 */
--insert into stats (DESCRIPTOR, IDXINFO, headb, creationdate, nart, gav, ga)
--  select
--    (select DESCRIPTOR from mi.properties), 
--    (select IDXINFO from mi.properties),
--    (select headb from mi.properties),
--    (select date(creationdate, 'unixepoch' ) from mi.properties),
--    (select count(*) from (select path from mi.art union all select path from mi.sec) ),
--    (select count(*) from mi.art),
--    (select count(*) from (select distinct gid, aid from mi.art));

    
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
