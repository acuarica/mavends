

/*
 * jarentry_str table
 * 
 */
create table jarentry_str (
  filename        varchar(255)  not null, 
  originalsize    int           not null,
  compressedsize  int           not null,
  primary key (filename)
);

/*
 * class_raw table
 */
create table class_str (
  classname  varchar(255)  not null, 
  supername  varchar(255)  not null,
  version    int           not null, 
  access     int           not null, 
  signature  varchar(255),
  primary key (classname)
);

/*
 * method_str
 */
create table method_str (
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  primary key (classname, methodname, methoddesc)
);

/*
 * callsite_raw table
 *
 * Contains a callsite to a method.
 */
create table callsite_raw (
  classname     varchar(255)  not null,
  methodname    varchar(255)  not null,
  methoddesc    varchar(255)  not null,
  offset        int           not null,
  targetclass   varchar(255)  not null,
  targetmethod  varchar(255)  not null,
  targetdesc    varchar(255)  not null,
  primary key (classname, methodname, methoddesc, offset)
);

/*
 * allocsite_str
 */
create table allocsite_str (
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  offset      int           not null,
  opcode      varchar(255)  not null,
  type        varchar(255)  not null,
  primary key (classname, methodname, methoddesc, offset)
);

/*
 * 
 */
create table fieldaccess_str (
  classname    varchar(255)  not null,
  methodname   varchar(255)  not null,
  methoddesc   varchar(255)  not null,
  offset       int           not null,
  targetclass  varchar(255)  not null,
  targetfield  varchar(255)  not null,
  targetdesc   varchar(255)  not null,
  primary key (classname, methodname, methoddesc, offset)
);

/*
 * 
 */
create table literal_str (
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  offset      int           not null,
  literal     text          not null,
  primary key (classname, methodname, methoddesc, offset)
);

/*
 *
 */
create table zero (
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  offset      int           not null,
  opcode          varchar(32)   not null,
  primary key (classname, methodname, methoddesc, offset)
);


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
 */*/
