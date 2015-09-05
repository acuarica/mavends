
drop table if exists cp_methodref_1;

create table cp_methodref_1 (
  methodrefid  integer       primary key,  -- rowid alias
  classnameid  int           not null,     --
  methodname   varchar(255)  not null,     --
  methoddesc   varchar(255)  not null     -- 
);

insert into cp_methodref_1 (classnameid, methodname, methoddesc) 
select classnameid, methodname, methoddesc from cp_methodref;

drop table cp_methodref;

alter table cp_methodref_1 rename to cp_method_ref;



drop table if exists jarentry_1;

create table jarentry_1 (
  coorid          int not null,
  filename        varchar(255)  not null, 
  originalsize    int           not null,
  compressedsize  int           not null,
  crc32 int,
  primary key (coorid, filename)
) without rowid;

insert into jarentry_1 (coorid,filename        , originalsize    ,compressedsize  ,crc32) 
select coorid,filename        , originalsize    ,compressedsize  ,crc32 from jarentry;

drop table jarentry;

alter table jarentry_1 rename to jarentry;

vacuum;
