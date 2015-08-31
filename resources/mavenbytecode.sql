

--
--
--
create table idx_cp_package (
  packageid integer       primary key,  --
  package   varchar(255)  not null,     --
  unique (package) on conflict ignore
);

create table idx_cp_class (
  classnameid  integer       primary key, -- rowid alias
  packageid int not null,
  classname    varchar(255)  not null,    -- Name of the class
  unique (packageid, classname) on conflict ignore
);

create view cp_class as
  select c.classnameid, p.package, c.classname
  from idx_cp_class c
  inner join idx_cp_package p on p.packageid = c.packageid;

create trigger cp_class_insert
instead of insert on cp_class
begin
  insert into idx_cp_package (package) select new.package;
  insert into idx_cp_class (packageid, classname) select 
    (select p.packageid from idx_cp_package p where p.package = new.package), 
    new.classname;
end;

--
--
--
--create table cp_class (
--  classnameid  integer       primary key, -- rowid alias
--  classname    varchar(255)  not null,    -- Name of the class
--  unique (classname) on conflict ignore
--);

--
--
--
create table cp_methodref (
  methodrefid  integer       primary key,  -- rowid alias
  classnameid  int           not null,     --
  methodname   varchar(255)  not null,     --
  methoddesc   varchar(255)  not null,     --
  unique (classnameid, methodname, methoddesc) on conflict ignore 
);

--
--
--
create view cp_methodref_view as
  select m.methodrefid, m.classnameid, c.package, c.classname, m.methodname, m.methoddesc 
  from cp_methodref m 
  inner join cp_class c on c.classnameid = m.classnameid;

--
--
--
create table callsite (
  coorid     int           not null,  --
  sm         int           not null,  --
  tm         int           not null,  --
  primary key (coorid, sm, tm) on conflict ignore
) without rowid;

--
--
--
create view callsite_view as
  select 
    cs.coorid, 
    sm.classname, sm.methodname, sm.methoddesc, 
    tm.classname as targetclassname, tm.methodname as targetmethodname, tm.methoddesc as targetmethoddesc  
  from callsite cs 
  inner join cp_methodref_view sm on sm.methodrefid = cs.sm
  inner join cp_methodref_view tm on tm.methodrefid = cs.tm;


--
--
--
create trigger callsite_view_insert
instead of insert on callsite_view
begin
  insert into cp_class (classname) select 
    new.classname;
  insert into cp_methodref (classnameid, methodname, methoddesc) select 
    (select c.classnameid from cp_class c where c.classname = new.classname), 
    new.methodname, 
    new.methoddesc;
  insert into callsite (coorid, tm) select 
    new.coorid, 
    (select m.methodrefid from cp_methodref_view m where m.classname=new.classname and m.methodname=new.methodname and m.methoddesc=new.methoddesc); 
end;

--  inner join mi.artifact a on a.coorid = cs.pid

--
-- class table
--
--create table class (
----  gid             varchar(255)  not null,
----  aid             varchar(255)  not null,
----  ver             varchar(64)   not null,
--  pid           int           not null,
--  classname  varchar(255)  not null, 
--  supername  varchar(255)  not null,
--  version    int           not null, 
--  access     int           not null, 
--  signature  varchar(255),
--  primary key (pid, classname)
--);

--create table class (
----  gid             varchar(255)  not null,
----  aid             varchar(255)  not null,
----  ver             varchar(64)   not null,
--  cid           int           not null,
--  classname  varchar(255)  not null, 
--  primary key (cid, classname) on conflict ignore
--);

--
-- Contains a callsite to a method.
--
--create table callsite1 (
----  gid             varchar(255)  not null,
----  aid             varchar(255)  not null,
----  ver             varchar(64)   not null,
--  pid           int           not null,
--  classname     varchar(255)  not null,
--  methodname    varchar(255)  not null,
--  methoddesc    varchar(255)  not null,
--  offset        int           not null,
--  targetclass   varchar(255)  not null,
--  targetmethod  varchar(255)  not null,
--  targetdesc    varchar(255)  not null,
--  primary key (pid, classname, methodname, methoddesc, offset)
--);



--
--
--
--create table fieldaccess (
----  gid             varchar(255)  not null,
----  aid             varchar(255)  not null,
----  ver             varchar(64)   not null,
--  pid           int           not null,
--  classname    varchar(255)  not null,
--  methodname   varchar(255)  not null,
--  methoddesc   varchar(255)  not null,
--  offset       int           not null,
--  targetclass  varchar(255)  not null,
--  targetfield  varchar(255)  not null,
--  targetdesc   varchar(255)  not null,
--  primary key (pid, classname, methodname, methoddesc, offset)
--);

--
--
--
--create table literal (
----  gid             varchar(255)  not null,
----  aid             varchar(255)  not null,
----  ver             varchar(64)   not null,
--  pid           int           not null,
--  classname   varchar(255)  not null,
--  methodname  varchar(255)  not null,
--  methoddesc  varchar(255)  not null,
--  offset      int           not null,
--  literal     text          not null,
--  primary key (pid, classname, methodname, methoddesc, offset)
--);

--select * from callsite cs inner join method m on m.mid = cs.tm
--inner join class c on c.cid = m.cid
--inner join mi.artifact a on a.coorid = cs.pid
