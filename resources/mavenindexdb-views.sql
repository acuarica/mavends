
drop table if exists stats;

create table stats (
  artcount integer,
  gav integer,
  ga integer
);

insert into stats (artcount, gav, ga)
select
(select count(*) as artcount from artifact ),
(select count(*) as gav from artifact where sat is null),
(select count(*) as ga from (select distinct gid, aid from artifact where sat is null));

drop view if exists properties_view;

create view properties_view as
select 
  DESCRIPTOR, 
  IDXINFO,
  headb,
  date(creationdate, 'unixepoch' ) as creationdate
from properties;
