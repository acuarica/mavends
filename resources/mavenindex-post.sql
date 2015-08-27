
--
--
--
create view artifact_view as
  select groupid || ':' || artifactid || '@' || version ||  ifnull('/' || classifier, '') || '.' || extension as id, * 
  from artifact;

--
--
--
create view artifact_main as
  select * from artifact where classifier is null;

--
--
--
create view artifact_secondary as
  select * from artifact where classifier is not null;

--
--
--
create view artifact_jar as
  select * from artifact where classifier is null and packaging = 'jar' and extension = 'jar';

--
--
--
create view artifact_war as
  select * from artifact where classifier is null and packaging = 'war' and extension = 'war';

--
-- Quick stats of the Maven Index.
-- Similar to http://search.maven.org/#stats.
--
create table stats (
  gavcp       integer not null, -- Total number of artifacts and secondary artifacts (GAVCP).
  gav         integer not null, -- Total number of artifacts indexed (GAV).
  ga          integer not null, -- Total number of unique artifacts indexed (GA).
  size        integer not null, -- Estimated size of the entire repository (in MB).
  jarsize     integer not null, -- Estimated size of jar artifacts (in MB).
  netjarsize  integer not null, -- Estimated size of jar artifacts in the net root group (in MB).
  comjarsize  integer not null, -- Estimated size of jar artifacts in the net root group (in MB).
  orgjarsize  integer not null, -- Estimated size of jar artifacts in the net root group (in MB).
  warsize     integer not null, -- Estimated size of war artifacts (in MB).
  netwarsize  integer not null, -- Estimated size of war artifacts in the net root group (in MB).
  comwarsize  integer not null, -- Estimated size of war artifacts in the net root group (in MB).
  orgwarsize  integer not null  -- Estimated size of war artifacts in the net root group (in MB).
);

insert into stats (gavcp, gav, ga, size, jarsize, netjarsize, comjarsize, orgjarsize, warsize, netwarsize, comwarsize, orgwarsize)
  select 
    (select count(*) from artifact), 
    (select count(*) from (select distinct groupid, artifactid, version from artifact)), 
    (select count(*) from (select distinct groupid, artifactid from artifact)), 
    (select sum(size)/(1024*1024) from artifact), 
    (select sum(size)/(1024*1024) from artifact_jar), 
    (select sum(size)/(1024*1024) from artifact_jar where substr(groupid, 1, 4) = 'net.'), 
    (select sum(size)/(1024*1024) from artifact_jar where substr(groupid, 1, 4) = 'com.'), 
    (select sum(size)/(1024*1024) from artifact_jar where substr(groupid, 1, 4) = 'org.'), 
    (select sum(size)/(1024*1024) from artifact_war), 
    (select sum(size)/(1024*1024) from artifact_war where substr(groupid, 1, 4) = 'net.'), 
    (select sum(size)/(1024*1024) from artifact_war where substr(groupid, 1, 4) = 'com.'), 
    (select sum(size)/(1024*1024) from artifact_war where substr(groupid, 1, 4) = 'org.');
