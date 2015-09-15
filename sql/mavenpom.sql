
--
-- dep table
-- 
-- POM dependencies.
--
create table dep (
	gid     varchar(255)  not null, -- 
	aid     varchar(255)  not null, -- 
	ver     varchar(128)  not null, -- 
	dgid    varchar(255),           -- 
	daid    varchar(255),           -- 
	dver    varchar(128),           -- 
	dscope  varchar(32)             -- 
);
