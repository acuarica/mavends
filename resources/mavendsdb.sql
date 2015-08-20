

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

attach "mavenindex.sqlite3" as mi; 

/*
 * properties table
 */
create table properties as
select 
  DESCRIPTOR, 
  IDXINFO,
  headb as creationdate,
  date(creationdate, 'unixepoch' ) as creationdate
from mi.properties;


/*
 * stats table
 */
create table stats as
  select
    (select count(*) as artcount from mi.artifact ),
    (select count(*) as gav from mi.artifact where sat is null),
    (select count(*) as ga from (select distinct gid, aid from mi.artifact where sat is null));

----select count(gid) from art  group by gid

/*
drop view if exists bytecode; 

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


drop view if exists subclass;

create view subclass as 
WITH RECURSIVE subclass(root, cls) AS (
select distinct supername, supername from class
UNION 
select root, class.name from class, subclass where class.supername=subclass.cls )
SELECT root, cls FROM subclass
/

*/

/*
 * 



create table cp_class (
  name varchar(255) not null,
  primary key (name)
);

integer primary key

CREATE TABLE class2 (
  nameid      int not null, 
  supernameid int not null,
  version   int          not null, 
  access    int          not null, 
  signature varchar(255),
  primary key (nameid)
);

insert into class2 select (select rowid from cp_class where name=c.name), (select rowid from cp_class where name=c.supername), version, access, signature from class c

create view class_view as
select cp.name, cps.name as supername, c.version, c.access, c.signature
from class2 c 
inner join cp_class cp on cp.rowid = c.nameid 
inner join cp_class cps on cps.rowid = c.supernameid ;

drop table class;

--insert into cp_class (name) select name from class union select supername as name from class


create table cp_sig (
  signature varchar(255) not null
)

insert into cp_sig select distinct signature from class3 where signature is not null group by signature 

CREATE TABLE class4 (
  nameid      integer primary key, 
  supernameid int not null,
  version   int          not null, 
  access    int          not null, 
  signatureid int 
)

insert into class4 select nameid, supernameid, version, access, (select rowid from cp_sig where signature=c.signature) from class3 c

drop view class_view;
create view class_view as
select cp.name, cps.name as supername, c.version, c.access, cs.signature
from class4 c 
inner join cp_class cp on cp.rowid = c.nameid 
inner join cp_class cps on cps.rowid = c.supernameid 
inner join cp_sig cs on cs.rowid = c.signatureid ;


drop table class3;

 * 
 */
