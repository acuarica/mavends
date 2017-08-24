
create table class_flags (
  id   int not null,
  name varchar(256) not null
);

insert into class_flags (name, id) values ('public', 0x0001);
insert into class_flags (name, id) values ('final', 0x0010);
insert into class_flags (name, id) values ('super', 0x0020);
insert into class_flags (name, id) values ('interface', 0x0200);
insert into class_flags (name, id) values ('abstract', 0x0400);
insert into class_flags (name, id) values ('synthetic', 0x1000);
insert into class_flags (name, id) values ('annotation', 0x2000);
insert into class_flags (name, id) values ('enum', 0x4000);

create table opcode (
  id   int primary key,
  name varchar(256) not null,
  kind int not null
);

insert into opcode (id, name, kind) values (0, 'nop', 0);
insert into opcode (id, name, kind) values (1, 'aconst_null', 0);
insert into opcode (id, name, kind) values (2, 'iconst_m1', 0);
insert into opcode (id, name, kind) values (3, 'iconst_0', 0);
insert into opcode (id, name, kind) values (4, 'iconst_1', 0);
insert into opcode (id, name, kind) values (5, 'iconst_2', 0);
insert into opcode (id, name, kind) values (6, 'iconst_3', 0);
insert into opcode (id, name, kind) values (7, 'iconst_4', 0);
insert into opcode (id, name, kind) values (8, 'iconst_5', 0);
insert into opcode (id, name, kind) values (9, 'lconst_0', 0);
insert into opcode (id, name, kind) values (10, 'lconst_1', 0);
insert into opcode (id, name, kind) values (11, 'fconst_0', 0);
insert into opcode (id, name, kind) values (12, 'fconst_1', 0);
insert into opcode (id, name, kind) values (13, 'fconst_2', 0);
insert into opcode (id, name, kind) values (14, 'dconst_0', 0);
insert into opcode (id, name, kind) values (15, 'dconst_1', 0);
insert into opcode (id, name, kind) values (16, 'bipush', 1);
insert into opcode (id, name, kind) values (17, 'sipush', 2);
insert into opcode (id, name, kind) values (18, 'ldc', 3);
insert into opcode (id, name, kind) values (19, 'ldc_w', 3);
insert into opcode (id, name, kind) values (20, 'ldc2_w', 3);
insert into opcode (id, name, kind) values (21, 'iload', 4);
insert into opcode (id, name, kind) values (22, 'lload', 4);
insert into opcode (id, name, kind) values (23, 'fload', 4);
insert into opcode (id, name, kind) values (24, 'dload', 4);
insert into opcode (id, name, kind) values (25, 'aload', 4);
insert into opcode (id, name, kind) values (26, 'iload_0', 0);
insert into opcode (id, name, kind) values (27, 'iload_1', 0);
insert into opcode (id, name, kind) values (28, 'iload_2', 0);
insert into opcode (id, name, kind) values (29, 'iload_3', 0);
insert into opcode (id, name, kind) values (30, 'lload_0', 0);
insert into opcode (id, name, kind) values (31, 'lload_1', 0);
insert into opcode (id, name, kind) values (32, 'lload_2', 0);
insert into opcode (id, name, kind) values (33, 'lload_3', 0);
insert into opcode (id, name, kind) values (34, 'fload_0', 0);
insert into opcode (id, name, kind) values (35, 'fload_1', 0);
insert into opcode (id, name, kind) values (36, 'fload_2', 0);
insert into opcode (id, name, kind) values (37, 'fload_3', 0);
insert into opcode (id, name, kind) values (38, 'dload_0', 0);
insert into opcode (id, name, kind) values (39, 'dload_1', 0);
insert into opcode (id, name, kind) values (40, 'dload_2', 0);
insert into opcode (id, name, kind) values (41, 'dload_3', 0);
insert into opcode (id, name, kind) values (42, 'aload_0', 0);
insert into opcode (id, name, kind) values (43, 'aload_1', 0);
insert into opcode (id, name, kind) values (44, 'aload_2', 0);
insert into opcode (id, name, kind) values (45, 'aload_3', 0);
insert into opcode (id, name, kind) values (46, 'iaload', 0);
insert into opcode (id, name, kind) values (47, 'laload', 0);
insert into opcode (id, name, kind) values (48, 'faload', 0);
insert into opcode (id, name, kind) values (49, 'daload', 0);
insert into opcode (id, name, kind) values (50, 'aaload', 0);
insert into opcode (id, name, kind) values (51, 'baload', 0);
insert into opcode (id, name, kind) values (52, 'caload', 0);
insert into opcode (id, name, kind) values (53, 'saload', 0);
insert into opcode (id, name, kind) values (54, 'istore', 4);
insert into opcode (id, name, kind) values (55, 'lstore', 4);
insert into opcode (id, name, kind) values (56, 'fstore', 4);
insert into opcode (id, name, kind) values (57, 'dstore', 4);
insert into opcode (id, name, kind) values (58, 'astore', 4);
insert into opcode (id, name, kind) values (59, 'istore_0', 0);
insert into opcode (id, name, kind) values (60, 'istore_1', 0);
insert into opcode (id, name, kind) values (61, 'istore_2', 0);
insert into opcode (id, name, kind) values (62, 'istore_3', 0);
insert into opcode (id, name, kind) values (63, 'lstore_0', 0);
insert into opcode (id, name, kind) values (64, 'lstore_1', 0);
insert into opcode (id, name, kind) values (65, 'lstore_2', 0);
insert into opcode (id, name, kind) values (66, 'lstore_3', 0);
insert into opcode (id, name, kind) values (67, 'fstore_0', 0);
insert into opcode (id, name, kind) values (68, 'fstore_1', 0);
insert into opcode (id, name, kind) values (69, 'fstore_2', 0);
insert into opcode (id, name, kind) values (70, 'fstore_3', 0);
insert into opcode (id, name, kind) values (71, 'dstore_0', 0);
insert into opcode (id, name, kind) values (72, 'dstore_1', 0);
insert into opcode (id, name, kind) values (73, 'dstore_2', 0);
insert into opcode (id, name, kind) values (74, 'dstore_3', 0);
insert into opcode (id, name, kind) values (75, 'astore_0', 0);
insert into opcode (id, name, kind) values (76, 'astore_1', 0);
insert into opcode (id, name, kind) values (77, 'astore_2', 0);
insert into opcode (id, name, kind) values (78, 'astore_3', 0);
insert into opcode (id, name, kind) values (79, 'iastore', 0);
insert into opcode (id, name, kind) values (80, 'lastore', 0);
insert into opcode (id, name, kind) values (81, 'fastore', 0);
insert into opcode (id, name, kind) values (82, 'dastore', 0);
insert into opcode (id, name, kind) values (83, 'aastore', 0);
insert into opcode (id, name, kind) values (84, 'bastore', 0);
insert into opcode (id, name, kind) values (85, 'castore', 0);
insert into opcode (id, name, kind) values (86, 'sastore', 0);
insert into opcode (id, name, kind) values (87, 'pop', 0);
insert into opcode (id, name, kind) values (88, 'pop2', 0);
insert into opcode (id, name, kind) values (89, 'dup', 0);
insert into opcode (id, name, kind) values (90, 'dup_x1', 0);
insert into opcode (id, name, kind) values (91, 'dup_x2', 0);
insert into opcode (id, name, kind) values (92, 'dup2', 0);
insert into opcode (id, name, kind) values (93, 'dup2_x1', 0);
insert into opcode (id, name, kind) values (94, 'dup2_x2', 0);
insert into opcode (id, name, kind) values (95, 'swap', 0);
insert into opcode (id, name, kind) values (96, 'iadd', 0);
insert into opcode (id, name, kind) values (97, 'ladd', 0);
insert into opcode (id, name, kind) values (98, 'fadd', 0);
insert into opcode (id, name, kind) values (99, 'dadd', 0);
insert into opcode (id, name, kind) values (100, 'isub', 0);
insert into opcode (id, name, kind) values (101, 'lsub', 0);
insert into opcode (id, name, kind) values (102, 'fsub', 0);
insert into opcode (id, name, kind) values (103, 'dsub', 0);
insert into opcode (id, name, kind) values (104, 'imul', 0);
insert into opcode (id, name, kind) values (105, 'lmul', 0);
insert into opcode (id, name, kind) values (106, 'fmul', 0);
insert into opcode (id, name, kind) values (107, 'dmul', 0);
insert into opcode (id, name, kind) values (108, 'idiv', 0);
insert into opcode (id, name, kind) values (109, 'ldiv', 0);
insert into opcode (id, name, kind) values (110, 'fdiv', 0);
insert into opcode (id, name, kind) values (111, 'ddiv', 0);
insert into opcode (id, name, kind) values (112, 'irem', 0);
insert into opcode (id, name, kind) values (113, 'lrem', 0);
insert into opcode (id, name, kind) values (114, 'frem', 0);
insert into opcode (id, name, kind) values (115, 'drem', 0);
insert into opcode (id, name, kind) values (116, 'ineg', 0);
insert into opcode (id, name, kind) values (117, 'lneg', 0);
insert into opcode (id, name, kind) values (118, 'fneg', 0);
insert into opcode (id, name, kind) values (119, 'dneg', 0);
insert into opcode (id, name, kind) values (120, 'ishl', 0);
insert into opcode (id, name, kind) values (121, 'lshl', 0);
insert into opcode (id, name, kind) values (122, 'ishr', 0);
insert into opcode (id, name, kind) values (123, 'lshr', 0);
insert into opcode (id, name, kind) values (124, 'iushr', 0);
insert into opcode (id, name, kind) values (125, 'lushr', 0);
insert into opcode (id, name, kind) values (126, 'iand', 0);
insert into opcode (id, name, kind) values (127, 'land', 0);
insert into opcode (id, name, kind) values (128, 'ior', 0);
insert into opcode (id, name, kind) values (129, 'lor', 0);
insert into opcode (id, name, kind) values (130, 'ixor', 0);
insert into opcode (id, name, kind) values (131, 'lxor', 0);
insert into opcode (id, name, kind) values (132, 'iinc', 5);
insert into opcode (id, name, kind) values (133, 'i2l', 0);
insert into opcode (id, name, kind) values (134, 'i2f', 0);
insert into opcode (id, name, kind) values (135, 'i2d', 0);
insert into opcode (id, name, kind) values (136, 'l2i', 0);
insert into opcode (id, name, kind) values (137, 'l2f', 0);
insert into opcode (id, name, kind) values (138, 'l2d', 0);
insert into opcode (id, name, kind) values (139, 'f2i', 0);
insert into opcode (id, name, kind) values (140, 'f2l', 0);
insert into opcode (id, name, kind) values (141, 'f2d', 0);
insert into opcode (id, name, kind) values (142, 'd2i', 0);
insert into opcode (id, name, kind) values (143, 'd2l', 0);
insert into opcode (id, name, kind) values (144, 'd2f', 0);
insert into opcode (id, name, kind) values (145, 'i2b', 0);
insert into opcode (id, name, kind) values (146, 'i2c', 0);
insert into opcode (id, name, kind) values (147, 'i2s', 0);
insert into opcode (id, name, kind) values (148, 'lcmp', 0);
insert into opcode (id, name, kind) values (149, 'fcmpl', 0);
insert into opcode (id, name, kind) values (150, 'fcmpg', 0);
insert into opcode (id, name, kind) values (151, 'dcmpl', 0);
insert into opcode (id, name, kind) values (152, 'dcmpg', 0);
insert into opcode (id, name, kind) values (153, 'ifeq', 6);
insert into opcode (id, name, kind) values (154, 'ifne', 6);
insert into opcode (id, name, kind) values (155, 'iflt', 6);
insert into opcode (id, name, kind) values (156, 'ifge', 6);
insert into opcode (id, name, kind) values (157, 'ifgt', 6);
insert into opcode (id, name, kind) values (158, 'ifle', 6);
insert into opcode (id, name, kind) values (159, 'if_icmpeq', 6);
insert into opcode (id, name, kind) values (160, 'if_icmpne', 6);
insert into opcode (id, name, kind) values (161, 'if_icmplt', 6);
insert into opcode (id, name, kind) values (162, 'if_icmpge', 6);
insert into opcode (id, name, kind) values (163, 'if_icmpgt', 6);
insert into opcode (id, name, kind) values (164, 'if_icmple', 6);
insert into opcode (id, name, kind) values (165, 'if_acmpeq', 6);
insert into opcode (id, name, kind) values (166, 'if_acmpne', 6);
insert into opcode (id, name, kind) values (167, 'goto', 6);
insert into opcode (id, name, kind) values (168, 'jsr', 6);
insert into opcode (id, name, kind) values (169, 'ret', 4);
insert into opcode (id, name, kind) values (170, 'tableswitch', 7);
insert into opcode (id, name, kind) values (171, 'lookupswitch', 8);
insert into opcode (id, name, kind) values (172, 'ireturn', 0);
insert into opcode (id, name, kind) values (173, 'lreturn', 0);
insert into opcode (id, name, kind) values (174, 'freturn', 0);
insert into opcode (id, name, kind) values (175, 'dreturn', 0);
insert into opcode (id, name, kind) values (176, 'areturn', 0);
insert into opcode (id, name, kind) values (177, 'return', 0);
insert into opcode (id, name, kind) values (178, 'getstatic', 9);
insert into opcode (id, name, kind) values (179, 'putstatic', 9);
insert into opcode (id, name, kind) values (180, 'getfield', 9);
insert into opcode (id, name, kind) values (181, 'putfield', 9);
insert into opcode (id, name, kind) values (182, 'invokevirtual', 10);
insert into opcode (id, name, kind) values (183, 'invokespecial', 10);
insert into opcode (id, name, kind) values (184, 'invokestatic', 10);
insert into opcode (id, name, kind) values (185, 'invokeinterface', 11);
insert into opcode (id, name, kind) values (186, 'invokedynamic', 12);
insert into opcode (id, name, kind) values (187, 'new', 13);
insert into opcode (id, name, kind) values (188, 'newarray', 14);
insert into opcode (id, name, kind) values (189, 'anewarray', 13);
insert into opcode (id, name, kind) values (190, 'arraylength', 0);
insert into opcode (id, name, kind) values (191, 'athrow', 0);
insert into opcode (id, name, kind) values (192, 'checkcast', 13);
insert into opcode (id, name, kind) values (193, 'instanceof', 13);
insert into opcode (id, name, kind) values (194, 'monitorenter', 0);
insert into opcode (id, name, kind) values (195, 'monitorexit', 0);
insert into opcode (id, name, kind) values (196, 'wide', 0);
insert into opcode (id, name, kind) values (197, 'multianewarray', 15);
insert into opcode (id, name, kind) values (198, 'ifnull', 6);
insert into opcode (id, name, kind) values (199, 'ifnonnull', 6);
insert into opcode (id, name, kind) values (200, 'goto_w', 16);
insert into opcode (id, name, kind) values (201, 'jsr_w', 16);
insert into opcode (id, name, kind) values (202, 'breakpoint', 17);
insert into opcode (id, name, kind) values (203, 'RESERVED', 17);
insert into opcode (id, name, kind) values (204, 'RESERVED', 17);
insert into opcode (id, name, kind) values (205, 'RESERVED', 17);
insert into opcode (id, name, kind) values (206, 'RESERVED', 17);
insert into opcode (id, name, kind) values (207, 'RESERVED', 17);
insert into opcode (id, name, kind) values (208, 'RESERVED', 17);
insert into opcode (id, name, kind) values (209, 'RESERVED', 17);
insert into opcode (id, name, kind) values (210, 'RESERVED', 17);
insert into opcode (id, name, kind) values (211, 'RESERVED', 17);
insert into opcode (id, name, kind) values (212, 'RESERVED', 17);
insert into opcode (id, name, kind) values (213, 'RESERVED', 17);
insert into opcode (id, name, kind) values (214, 'RESERVED', 17);
insert into opcode (id, name, kind) values (215, 'RESERVED', 17);
insert into opcode (id, name, kind) values (216, 'RESERVED', 17);
insert into opcode (id, name, kind) values (217, 'RESERVED', 17);
insert into opcode (id, name, kind) values (218, 'RESERVED', 17);
insert into opcode (id, name, kind) values (219, 'RESERVED', 17);
insert into opcode (id, name, kind) values (220, 'RESERVED', 17);
insert into opcode (id, name, kind) values (221, 'RESERVED', 17);
insert into opcode (id, name, kind) values (222, 'RESERVED', 17);
insert into opcode (id, name, kind) values (223, 'RESERVED', 17);
insert into opcode (id, name, kind) values (224, 'RESERVED', 17);
insert into opcode (id, name, kind) values (225, 'RESERVED', 17);
insert into opcode (id, name, kind) values (226, 'RESERVED', 17);
insert into opcode (id, name, kind) values (227, 'RESERVED', 17);
insert into opcode (id, name, kind) values (228, 'RESERVED', 17);
insert into opcode (id, name, kind) values (229, 'RESERVED', 17);
insert into opcode (id, name, kind) values (230, 'RESERVED', 17);
insert into opcode (id, name, kind) values (231, 'RESERVED', 17);
insert into opcode (id, name, kind) values (232, 'RESERVED', 17);
insert into opcode (id, name, kind) values (233, 'RESERVED', 17);
insert into opcode (id, name, kind) values (234, 'RESERVED', 17);
insert into opcode (id, name, kind) values (235, 'RESERVED', 17);
insert into opcode (id, name, kind) values (236, 'RESERVED', 17);
insert into opcode (id, name, kind) values (237, 'RESERVED', 17);
insert into opcode (id, name, kind) values (238, 'RESERVED', 17);
insert into opcode (id, name, kind) values (239, 'RESERVED', 17);
insert into opcode (id, name, kind) values (240, 'RESERVED', 17);
insert into opcode (id, name, kind) values (241, 'RESERVED', 17);
insert into opcode (id, name, kind) values (242, 'RESERVED', 17);
insert into opcode (id, name, kind) values (243, 'RESERVED', 17);
insert into opcode (id, name, kind) values (244, 'RESERVED', 17);
insert into opcode (id, name, kind) values (245, 'RESERVED', 17);
insert into opcode (id, name, kind) values (246, 'RESERVED', 17);
insert into opcode (id, name, kind) values (247, 'RESERVED', 17);
insert into opcode (id, name, kind) values (248, 'RESERVED', 17);
insert into opcode (id, name, kind) values (249, 'RESERVED', 17);
insert into opcode (id, name, kind) values (250, 'RESERVED', 17);
insert into opcode (id, name, kind) values (251, 'RESERVED', 17);
insert into opcode (id, name, kind) values (252, 'RESERVED', 17);
insert into opcode (id, name, kind) values (253, 'RESERVED', 17);
insert into opcode (id, name, kind) values (254, 'impdep1', 17);
insert into opcode (id, name, kind) values (255, 'impdep2', 17);

create table cp_classname (
       classnameid     integer primary key,
       classname       varchar(256) not null unique
);

create table cp_methoddesc (
  methoddescid     integer primary key,
  methoddesc       varchar(256) not null unique on conflict ignore
);

create table cp_signature (
  signatureid     integer primary key,
  signature       varchar(256) not null unique on conflict ignore
);

create table cp_methodref (
  methodrefid integer primary key,
  classnameid integer not null references cp_classname(classnameid),
  methodname varchar(256) not null,
  methoddescid integer not null references cp_methoddesc(methoddescid),
  unique (classnameid, methodname, methoddescid)
);

create table jar (
    jarid    integer primary key,
    coord    varchar(256) unique not null,
    path     varchar(256) unique not null
);

create table class (
    classid     integer primary key,
    jarid       int not null references jar(jarid),
    minor_version     int not null,
    major_version     int not null,
    access      int not null,
    classnameid     int not null references cp_classname(classnameid),
    signatureid       int references cp_signature(signatureid),
    superclassid    int not null references cp_classname(classnameid)
);

create table interface (
       classid int not null references class(classid),
       interfaceid int not null references cp_classname(classnameid),
       primary key (classid, interfaceid)
);

create table method (
    methodid    integer primary key,
    classid     integer not null references class(classid),
    access      int not null,
    methodname  varchar(256) not null,
    methoddescid int not null references cp_methoddesc(methoddescid),
    signature   varchar(256),
    exceptions  varchar(256)
);

create table code (
    opcodeindex integer primary key,
    methodid    integer not null references method(methodid),
    opcode      integer not null references opcode(id),
    args        text
);

create index code_methodid on code(methodid);

create index code_opcode on code(opcode);
