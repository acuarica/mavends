
drop view if exists cp_methodref_view;

create view cp_methodref_view as
  select mr.methodrefid, mr.classnameid, cn.classname, mr.methodname,
  mr.methoddescid, md.methoddesc
  from cp_methodref mr
  left join cp_classname cn on cn.classnameid = mr.classnameid
  left join cp_methoddesc md on md.methoddescid = mr.classnameid;

drop view if exists class_view;

create view class_view as
  select jar.jarid, jar.coord,
  class.classid, class.minor_version, class.major_version,
  case when class.access & 1 then 'public ' else '' end ||
  case when class.access & 16 then 'final ' else '' end ||
  case when class.access & 32 then 'super ' else '' end ||
  case when class.access & 512 then 'interface ' else '' end ||
  case when class.access & 1024 then 'abstract ' else '' end ||
  case when class.access & 4096 then 'synthetic ' else '' end ||
  case when class.access & 8192 then 'annotation ' else '' end ||
  case when class.access & 16384 then 'enum ' else '' end,
  cp_classname.classname,
  cp_signature.signature,
  s.classname as superclass,
  (select group_concat(interface) from interface_view where classid=class.classid) as interfaces
  from class
  left join jar on jar.jarid = class.jarid
  left join cp_classname on cp_classname.classnameid = class.classnameid
  left join cp_signature on cp_signature.signatureid = class.signatureid
  left join cp_classname s on s.classnameid = class.superclassid;

drop view if exists interface_view;

create view interface_view as
  select
    interface.classid,
    cp_classname.classname as interface
  from interface
  left join cp_classname on cp_classname.classnameid = interface.interfaceid;

drop view if exists method_view;

create view method_view as
    select class.*, method.*, cp_methoddesc.*
    from method
    left join class on class.classid = method.classid
    left join cp_methoddesc on cp_methoddesc.methoddescid = method.methoddescid;

drop view if exists code_view;

create view code_view as
  select cv.classname, mv.methodname, mv.methoddesc,
  c.opcodeindex, c.opcode, op.name, args,
  case
  when op.kind=0 then ''
  when op.kind=1 or op.kind=2 then args
  when op.kind=10 then (select classname||'.'||methodname||methoddesc from cp_methodref_view where methodrefid=args)
  when op.kind=13 then (select classname from cp_classname where classnameid=args)
  else '?'
  end as argstext
  from code c
  left join opcode op on op.id = c.opcode
  left join method_view as mv on mv.methodid = c.methodid
  left join class_view cv on cv.classid = mv.classid;

drop view if exists equals_method_count;

create view equals_method_count as
  select count(*) from method_view where methodname='equals' and methoddesc ='(Ljava/lang/Object;)Z';

drop view if exists code_stats;

create view code_stats as
  select 'bytecode ops' as name, count(*) as value from code union
  select 'checkcast ops', count(*) from code where opcode=(select id from opcode where name='checkcast')
;
