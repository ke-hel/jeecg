--删除
drop user jeecg cascade;
drop tablespace jeecg including contents and datafiles;
--创建用户
create tablespace jeecg datafile 'jeecg.dbf' size 100m reuse autoextend on next 15m;
create user jeecg identified by vip2015lbzc default tablespace jeecg ;
grant resource,connect to jeecg;
grant create any snapshot,alter any snapshot,drop any snapshot to jeecg;
grant select any table,update any table to jeecg;
grant insert any table,delete any table to jeecg;
grant dba to jeecg with admin option;
grant create any table,alter any table,drop any table to jeecg;
grant create any trigger,alter any trigger,drop any trigger to jeecg;
grant create any procedure,alter any procedure,drop any procedure to jeecg;
grant create any view,create any synonym,drop any view to jeecg;
grant execute any procedure to jeecg;
grant dba to jeecg with admin option;
--导入
host imp jeecg/vip2015lbzc fromuser=(jeecg) touser=(jeecg) log=imp.log file=jeecg_3.7.6_oracle11g.dmp buffer=10240000 commit=y ignore=y;