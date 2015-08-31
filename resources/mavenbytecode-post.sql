
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

vacuum;
