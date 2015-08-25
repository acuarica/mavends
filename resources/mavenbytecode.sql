

--
-- jarentry table
-- 
create table jarentry (
--  gid             varchar(255)  not null,
--  aid             varchar(255)  not null,
--  ver             varchar(64)   not null,
  pid           int           not null,
  filename        varchar(255)  not null, 
  originalsize    int           not null,
  compressedsize  int           not null,
  crc32             varchar(64) not null,
  primary key (pid, filename)
);

--
-- class table
--
create table class (
--  gid             varchar(255)  not null,
--  aid             varchar(255)  not null,
--  ver             varchar(64)   not null,
  pid           int           not null,
  classname  varchar(255)  not null, 
  supername  varchar(255)  not null,
  version    int           not null, 
  access     int           not null, 
  signature  varchar(255),
  primary key (pid, classname)
);

--
-- method
--
create table method (
--  gid             varchar(255)  not null,
--  aid             varchar(255)  not null,
--  ver             varchar(64)   not null,
  pid           int           not null,
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  primary key (pid, classname, methodname, methoddesc)
);

--
-- Contains a callsite to a method.
--
create table callsite (
--  gid             varchar(255)  not null,
--  aid             varchar(255)  not null,
--  ver             varchar(64)   not null,
  pid           int           not null,
  classname     varchar(255)  not null,
  methodname    varchar(255)  not null,
  methoddesc    varchar(255)  not null,
  offset        int           not null,
  targetclass   varchar(255)  not null,
  targetmethod  varchar(255)  not null,
  targetdesc    varchar(255)  not null,
  primary key (pid, classname, methodname, methoddesc, offset)
);

--
-- allocsite
--
create table allocsite (
  gid             varchar(255)  not null,
  aid             varchar(255)  not null,
  ver             varchar(64)   not null,
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  offset      int           not null,
  opcode      varchar(255)  not null,
  type        varchar(255)  not null,
  primary key (gid, aid, ver, classname, methodname, methoddesc, offset)
);

--
--
--
create table fieldaccess (
  gid             varchar(255)  not null,
  aid             varchar(255)  not null,
  ver             varchar(64)   not null,
  classname    varchar(255)  not null,
  methodname   varchar(255)  not null,
  methoddesc   varchar(255)  not null,
  offset       int           not null,
  targetclass  varchar(255)  not null,
  targetfield  varchar(255)  not null,
  targetdesc   varchar(255)  not null,
  primary key (gid, aid, ver, classname, methodname, methoddesc, offset)
);

--
--
--
create table literal (
  gid             varchar(255)  not null,
  aid             varchar(255)  not null,
  ver             varchar(64)   not null,
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  offset      int           not null,
  literal     text          not null,
  primary key (gid, aid, ver, classname, methodname, methoddesc, offset)
);

--
--
--
create table zero (
  gid             varchar(255)  not null,
  aid             varchar(255)  not null,
  ver             varchar(64)   not null,
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  offset      int           not null,
  opcode          varchar(32)   not null,
  primary key (gid, aid, ver, classname, methodname, methoddesc, offset)
);
