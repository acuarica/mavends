
create table class (
    classid     integer primary key,
    version     int,
    access      int,
    classname   varchar(256),
    signature   varchar(256),
    superclass  varchar(256),
    interfaces  varchar(256)
);

create table method (
    methodid    integer primary key,
    classid     integer,
    access      int,
    methodname  varchar(256),
    methoddesc  varchar(256),
    signature   varchar(256),
    exceptions  varchar(256),
    codetext    text
);

create table zero (
    methodid integer,
    opcode  int
);

create table opcode (
    id   int,
    name varchar(256)
);

insert into opcode (name, id) values ('nop', 0x00);
insert into opcode (name, id) values ('aconst_null', 0x01);
insert into opcode (name, id) values ('iconst_m1', 0x02);
insert into opcode (name, id) values ('iconst_0', 0x03);
insert into opcode (name, id) values ('iconst_1', 0x04);
insert into opcode (name, id) values ('iconst_2', 0x05);
insert into opcode (name, id) values ('iconst_3', 0x06);
insert into opcode (name, id) values ('iconst_4', 0x07);
insert into opcode (name, id) values ('iconst_5', 0x08);
insert into opcode (name, id) values ('lconst_0', 0x09);
insert into opcode (name, id) values ('lconst_1', 0x0a);
insert into opcode (name, id) values ('fconst_0', 0x0b);
insert into opcode (name, id) values ('fconst_1', 0x0c);
insert into opcode (name, id) values ('fconst_2', 0x0d);
insert into opcode (name, id) values ('dconst_0', 0x0e);
insert into opcode (name, id) values ('dconst_1', 0x0f);
insert into opcode (name, id) values ('bipush', 0x10);
insert into opcode (name, id) values ('sipush', 0x11);
insert into opcode (name, id) values ('ldc', 0x12);
insert into opcode (name, id) values ('ldc_w', 0x13);
insert into opcode (name, id) values ('ldc2_w', 0x14);
insert into opcode (name, id) values ('iload', 0x15);
insert into opcode (name, id) values ('lload', 0x16);
insert into opcode (name, id) values ('fload', 0x17);
insert into opcode (name, id) values ('dload', 0x18);
insert into opcode (name, id) values ('aload', 0x19);
insert into opcode (name, id) values ('iload_0', 0x1a);
insert into opcode (name, id) values ('iload_1', 0x1b);
insert into opcode (name, id) values ('iload_2', 0x1c);
insert into opcode (name, id) values ('iload_3', 0x1d);
insert into opcode (name, id) values ('lload_0', 0x1e);
insert into opcode (name, id) values ('lload_1', 0x1f);
insert into opcode (name, id) values ('lload_2', 0x20);
insert into opcode (name, id) values ('lload_3', 0x21);
insert into opcode (name, id) values ('fload_0', 0x22);
insert into opcode (name, id) values ('fload_1', 0x23);
insert into opcode (name, id) values ('fload_2', 0x24);
insert into opcode (name, id) values ('fload_3', 0x25);
insert into opcode (name, id) values ('dload_0', 0x26);
insert into opcode (name, id) values ('dload_1', 0x27);
insert into opcode (name, id) values ('dload_2', 0x28);
insert into opcode (name, id) values ('dload_3', 0x29);
insert into opcode (name, id) values ('aload_0', 0x2a);
insert into opcode (name, id) values ('aload_1', 0x2b);
insert into opcode (name, id) values ('aload_2', 0x2c);
insert into opcode (name, id) values ('aload_3', 0x2d);
insert into opcode (name, id) values ('iaload', 0x2e);
insert into opcode (name, id) values ('laload', 0x2f);
insert into opcode (name, id) values ('faload', 0x30);
insert into opcode (name, id) values ('daload', 0x31);
insert into opcode (name, id) values ('aaload', 0x32);
insert into opcode (name, id) values ('baload', 0x33);
insert into opcode (name, id) values ('caload', 0x34);
insert into opcode (name, id) values ('saload', 0x35);
insert into opcode (name, id) values ('istore', 0x36);
insert into opcode (name, id) values ('lstore', 0x37);
insert into opcode (name, id) values ('fstore', 0x38);
insert into opcode (name, id) values ('dstore', 0x39);
insert into opcode (name, id) values ('astore', 0x3a);
insert into opcode (name, id) values ('istore_0', 0x3b);
insert into opcode (name, id) values ('istore_1', 0x3c);
insert into opcode (name, id) values ('istore_2', 0x3d);
insert into opcode (name, id) values ('istore_3', 0x3e);
insert into opcode (name, id) values ('lstore_0', 0x3f);
insert into opcode (name, id) values ('lstore_1', 0x40);
insert into opcode (name, id) values ('lstore_2', 0x41);
insert into opcode (name, id) values ('lstore_3', 0x42);
insert into opcode (name, id) values ('fstore_0', 0x43);
insert into opcode (name, id) values ('fstore_1', 0x44);
insert into opcode (name, id) values ('fstore_2', 0x45);
insert into opcode (name, id) values ('fstore_3', 0x46);
insert into opcode (name, id) values ('dstore_0', 0x47);
insert into opcode (name, id) values ('dstore_1', 0x48);
insert into opcode (name, id) values ('dstore_2', 0x49);
insert into opcode (name, id) values ('dstore_3', 0x4a);
insert into opcode (name, id) values ('astore_0', 0x4b);
insert into opcode (name, id) values ('astore_1', 0x4c);
insert into opcode (name, id) values ('astore_2', 0x4d);
insert into opcode (name, id) values ('astore_3', 0x4e);
insert into opcode (name, id) values ('iastore', 0x4f);
insert into opcode (name, id) values ('lastore', 0x50);
insert into opcode (name, id) values ('fastore', 0x51);
insert into opcode (name, id) values ('dastore', 0x52);
insert into opcode (name, id) values ('aastore', 0x53);
insert into opcode (name, id) values ('bastore', 0x54);
insert into opcode (name, id) values ('castore', 0x55);
insert into opcode (name, id) values ('sastore', 0x56);
insert into opcode (name, id) values ('pop', 0x57);
insert into opcode (name, id) values ('pop2', 0x58);
insert into opcode (name, id) values ('dup', 0x59);
insert into opcode (name, id) values ('dup_x1', 0x5a);
insert into opcode (name, id) values ('dup_x2', 0x5b);
insert into opcode (name, id) values ('dup2', 0x5c);
insert into opcode (name, id) values ('dup2_x1', 0x5d);
insert into opcode (name, id) values ('dup2_x2', 0x5e);
insert into opcode (name, id) values ('swap', 0x5f);
insert into opcode (name, id) values ('iadd', 0x60);
insert into opcode (name, id) values ('ladd', 0x61);
insert into opcode (name, id) values ('fadd', 0x62);
insert into opcode (name, id) values ('dadd', 0x63);
insert into opcode (name, id) values ('isub', 0x64);
insert into opcode (name, id) values ('lsub', 0x65);
insert into opcode (name, id) values ('fsub', 0x66);
insert into opcode (name, id) values ('dsub', 0x67);
insert into opcode (name, id) values ('imul', 0x68);
insert into opcode (name, id) values ('lmul', 0x69);
insert into opcode (name, id) values ('fmul', 0x6a);
insert into opcode (name, id) values ('dmul', 0x6b);
insert into opcode (name, id) values ('idiv', 0x6c);
insert into opcode (name, id) values ('ldiv', 0x6d);
insert into opcode (name, id) values ('fdiv', 0x6e);
insert into opcode (name, id) values ('ddiv', 0x6f);
insert into opcode (name, id) values ('irem', 0x70);
insert into opcode (name, id) values ('lrem', 0x71);
insert into opcode (name, id) values ('frem', 0x72);
insert into opcode (name, id) values ('drem', 0x73);
insert into opcode (name, id) values ('ineg', 0x74);
insert into opcode (name, id) values ('lneg', 0x75);
insert into opcode (name, id) values ('fneg', 0x76);
insert into opcode (name, id) values ('dneg', 0x77);
insert into opcode (name, id) values ('ishl', 0x78);
insert into opcode (name, id) values ('lshl', 0x79);
insert into opcode (name, id) values ('ishr', 0x7a);
insert into opcode (name, id) values ('lshr', 0x7b);
insert into opcode (name, id) values ('iushr', 0x7c);
insert into opcode (name, id) values ('lushr', 0x7d);
insert into opcode (name, id) values ('iand', 0x7e);
insert into opcode (name, id) values ('land', 0x7f);
insert into opcode (name, id) values ('ior', 0x80);
insert into opcode (name, id) values ('lor', 0x81);
insert into opcode (name, id) values ('ixor', 0x82);
insert into opcode (name, id) values ('lxor', 0x83);
insert into opcode (name, id) values ('iinc', 0x84);
insert into opcode (name, id) values ('i2l', 0x85);
insert into opcode (name, id) values ('i2f', 0x86);
insert into opcode (name, id) values ('i2d', 0x87);
insert into opcode (name, id) values ('l2i', 0x88);
insert into opcode (name, id) values ('l2f', 0x89);
insert into opcode (name, id) values ('l2d', 0x8a);
insert into opcode (name, id) values ('f2i', 0x8b);
insert into opcode (name, id) values ('f2l', 0x8c);
insert into opcode (name, id) values ('f2d', 0x8d);
insert into opcode (name, id) values ('d2i', 0x8e);
insert into opcode (name, id) values ('d2l', 0x8f);
insert into opcode (name, id) values ('d2f', 0x90);
insert into opcode (name, id) values ('i2b', 0x91);
insert into opcode (name, id) values ('i2c', 0x92);
insert into opcode (name, id) values ('i2s', 0x93);
insert into opcode (name, id) values ('lcmp', 0x94);
insert into opcode (name, id) values ('fcmpl', 0x95);
insert into opcode (name, id) values ('fcmpg', 0x96);
insert into opcode (name, id) values ('dcmpl', 0x97);
insert into opcode (name, id) values ('dcmpg', 0x98);
insert into opcode (name, id) values ('ifeq', 0x99);
insert into opcode (name, id) values ('ifne', 0x9a);
insert into opcode (name, id) values ('iflt', 0x9b);
insert into opcode (name, id) values ('ifge', 0x9c);
insert into opcode (name, id) values ('ifgt', 0x9d);
insert into opcode (name, id) values ('ifle', 0x9e);
insert into opcode (name, id) values ('if_icmpeq', 0x9f);
insert into opcode (name, id) values ('if_icmpne', 0xa0);
insert into opcode (name, id) values ('if_icmplt', 0xa1);
insert into opcode (name, id) values ('if_icmpge', 0xa2);
insert into opcode (name, id) values ('if_icmpgt', 0xa3);
insert into opcode (name, id) values ('if_icmple', 0xa4);
insert into opcode (name, id) values ('if_acmpeq', 0xa5);
insert into opcode (name, id) values ('if_acmpne', 0xa6);
insert into opcode (name, id) values ('goto', 0xa7);
insert into opcode (name, id) values ('jsr', 0xa8);
insert into opcode (name, id) values ('ret', 0xa9);
insert into opcode (name, id) values ('tableswitch', 0xaa);
insert into opcode (name, id) values ('lookupswitch', 0xab);
insert into opcode (name, id) values ('ireturn', 0xac);
insert into opcode (name, id) values ('lreturn', 0xad);
insert into opcode (name, id) values ('freturn', 0xae);
insert into opcode (name, id) values ('dreturn', 0xaf);
insert into opcode (name, id) values ('areturn', 0xb0);
insert into opcode (name, id) values ('return', 0xb1);
insert into opcode (name, id) values ('getstatic', 0xb2);
insert into opcode (name, id) values ('putstatic', 0xb3);
insert into opcode (name, id) values ('getfield', 0xb4);
insert into opcode (name, id) values ('putfield', 0xb5);
insert into opcode (name, id) values ('invokevirtual', 0xb6);
insert into opcode (name, id) values ('invokespecial', 0xb7);
insert into opcode (name, id) values ('invokestatic', 0xb8);
insert into opcode (name, id) values ('invokeinterface', 0xb9);
insert into opcode (name, id) values ('invokedynamic', 0xba);
insert into opcode (name, id) values ('new', 0xbb);
insert into opcode (name, id) values ('newarray', 0xbc);
insert into opcode (name, id) values ('anewarray', 0xbd);
insert into opcode (name, id) values ('arraylength', 0xbe);
insert into opcode (name, id) values ('athrow', 0xbf);
insert into opcode (name, id) values ('checkcast', 0xc0);
insert into opcode (name, id) values ('instanceof', 0xc1);
insert into opcode (name, id) values ('monitorenter', 0xc2);
insert into opcode (name, id) values ('monitorexit', 0xc3);
insert into opcode (name, id) values ('wide', 0xc4);
insert into opcode (name, id) values ('multianewarray', 0xc5);
insert into opcode (name, id) values ('ifnull', 0xc6);
insert into opcode (name, id) values ('ifnonnull', 0xc7);
insert into opcode (name, id) values ('goto_w', 0xc8);
insert into opcode (name, id) values ('jsr_w', 0xc9);
insert into opcode (name, id) values ('breakpoint', 0xca);
insert into opcode (name, id) values ('impdep1', 0xfe);
insert into opcode (name, id) values ('impdep2', 0xff);

create view method_view as
    select class.*, method.* from method left join class on class.classid = method.classid;
--select * from zero left join opcode on opcode.id = zero.opcode
