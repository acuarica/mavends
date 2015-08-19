

/*
 * jarentry_ table
 * 
 */
create table jarentry_str (
  filename        varchar(255)  not null, 
  originalsize    int           not null,
  compressedsize  int           not null,
  primary key (filename)
);

/*
 * class_str table
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
 * callsite_str table
 *
 * Contains a callsite to a method.
 */
create table callsite_str (
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
 * fieldaccess_str table 
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
 * literal_str table
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
 * zero_str table
 */
create table zero_str (
  classname   varchar(255)  not null,
  methodname  varchar(255)  not null,
  methoddesc  varchar(255)  not null,
  offset      int           not null,
  opcode      varchar(32)   not null,
  primary key (classname, methodname, methoddesc, offset)
);

