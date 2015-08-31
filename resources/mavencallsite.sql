
--
--
--
create table callsite (
  coorid            int,
  package           varchar(255),  --
  classname         varchar(255),  --
  methodname        varchar(255),  --
  methoddesc        varchar(255),  --
  offset            int,           --
  targetpackage     varchar(255),  --
  targetclassname   varchar(255),  --
  targetmethodname  varchar(255),  -- 
  targetmethoddesc  varchar(255)   -- 
);
