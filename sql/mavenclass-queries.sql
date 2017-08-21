
-- select count(*) from jar;

-- select count(*) from jar where jarid in (select distinct jarid from class);

-- select count(*) from code
-- where opcode=(select id from opcode where name='checkcast');

-- select count(*) from code
-- where opcode=(select id from opcode where name='instanceof');

-- select * from method where signature not null

-- select count(*)
-- from (
--   select distinct methodid from code
--   where opcode=(select id from opcode where name='checkcast')
--   )
-- ;

-- select count(*)
-- from (
-- select distinct methodid from code
-- where opcode=(select id from opcode where name='instanceof')
-- )
-- ;

-- select count(*) from method_view where methodname='equals' and methoddesc ='(Ljava/lang/Object;)Z'

-- select count(*)
-- from code
-- left join method_view on method_view.methodid = code.methodid
-- where opcode=(select id from opcode where name='checkcast') and
-- methodname='equals' and methoddesc ='(Ljava/lang/Object;)Z'

-- select t.args, cp.classname, t.cc
-- from (
--   select args, count(*) as cc
--   from code
--   where opcode=(select id from opcode where name='checkcast')
--   group by args
-- ) t
-- left join cp_classname cp on cp.classnameid=t.args
-- order by t.cc desc
-- limit 50
-- ;

select t.args, cp.classname, t.cc
from (
  select args, count(*) as cc
  from code
  where opcode=(select id from opcode where name='instanceof')
  group by args
) t
left join cp_classname cp on cp.classnameid=t.args
order by t.cc desc
limit 50
;



-- select *
-- from (
--   select
--     t.opcode,
--     (select name from opcode where id=t.opcode),
--     t.args,
--     (select fullmethodname from cp_methodref_view where methodrefid=t.args),
--     count(*) as cc
--   from (
--     select c.opcode, c.args
--     from (
--       select opcodeindex-1 as opcodeindex from code
--       where opcode=(select id from opcode where name='checkcast') -- limit 1000
--     ) t
--     left join code c on c.opcodeindex=t.opcodeindex
--   ) t
--   group by opcode, args
-- )
-- order by cc desc
-- limit 50
-- ;

-- select *
-- from (
--   select
--     t.opcode,
--     (select name from opcode where id=t.opcode),
--     t.args,
--     (select fullmethodname from cp_methodref_view where methodrefid=t.args),
--     count(*) as cc
--   from (
--     select c.opcode, c.args
--     from (
--       select opcodeindex-1 as opcodeindex from code
--       where opcode=(select id from opcode where name='instanceof') -- limit 1000
--     ) t
--     left join code c on c.opcodeindex=t.opcodeindex
--   ) t
--   group by opcode, args
-- )
-- order by cc desc
-- limit 50
-- ;

-- select * from code_view
-- where opcodeindex in (select opcodeindex-1 from code_view where opcode=(select id from opcode where name='checkcast') ) limit 20
-- ;

-- select * from code_view where instr(args, "java/lang/ClassCastException") limit 100





