
--
--
--
create table inode (
  inodeid         integer       primary key,  -- 
  originalsize    int           not null,     -- 
  compressedsize  int           not null,     -- 
  crc32           int           not null,     -- 
  sha1            varchar(40)   not null,     -- 
  data            blob          not null,     --
  unique (sha1) on conflict ignore
);

create table ifile (
  coorid      int           not null,  -- 
  filename    varchar(255)  not null,  -- 
  inodeid     int           not null,  -- 
  primary key (coorid, filename) on conflict ignore
) without rowid;


create view file as
  select f.coorid, f.filename, n.originalsize, n.compressedsize, n.crc32, n.sha1, n.data
  from ifile f
  inner join inode n on n.inodeid = f.inodeid;

create trigger file_insert
instead of insert on file
begin
  insert into inode (originalsize, compressedsize, crc32, sha1, data) 
  select new.originalsize, new.compressedsize, new.crc32, new.sha1, new.data;
  
  insert into ifile (coorid, filename, inodeid) select new.coorid, new.filename,
    (select n.inodeid from inode n where n.sha1 = new.sha1);
end;


create view stats as
  with 
    je  as (select * from file),
    fe  as (select * from je where originalsize > 0),
    ce  as (select * from fe where filename like '%.class'),
    crc as (select distinct crc32, originalsize, compressedsize from fe),
    sha as (select distinct sha1, originalsize, compressedsize from fe),
    feinode as (select distinct crc32, sha1, originalsize, compressedsize from fe),
    ceinode as (select distinct crc32, sha1, originalsize, compressedsize from ce)
  select 'jarentry' as category, count(*) as entries, sum(originalsize)/(1024*1024) as size_MB, sum(compressedsize)/(1024*1024) as compressed_MB from je
  union all select 'files',   count(*), sum(originalsize)/(1024*1024), sum(compressedsize)/(1024*1024) from fe
  union all select 'classes', count(*), sum(originalsize)/(1024*1024), sum(compressedsize)/(1024*1024) from ce
  union all select 'crc', count(*), sum(originalsize)/(1024*1024), sum(compressedsize)/(1024*1024) from crc
  union all select 'sha', count(*), sum(originalsize)/(1024*1024), sum(compressedsize)/(1024*1024) from sha
  union all select 'feinode', count(*), sum(originalsize)/(1024*1024), sum(compressedsize)/(1024*1024) from feinode
  union all select 'ceinode', count(*), sum(originalsize)/(1024*1024), sum(compressedsize)/(1024*1024) from ceinode
;
