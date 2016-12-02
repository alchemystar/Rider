# Rider 文档解析中间件
## 用SQL解析文件
(1)完全支持MySql协议  
(2)完全支持Mybatis-Generator  
(3)支持Schema和Table  
(4)支持客户端创建Schema和Table  
(5)支持常用select语句where,condition,行运算等 
(6)支持视图功能，即用旧表的SQL定义新表 
(7)支持各种文件格式
##启动
```
git clone https://github.com/alchemystar/Rider.git
mvn clean package
cd target && tar zxvf rider.tar.gz
cd ./bin
sh start.sh
```
配置文件,在./conf中

```
<database>
    <port>8090</port> <!--server 端口号-->
    <user>pa123</user> <!--server 用户名-->
    <pass>Mi123</pass> <!--server 密码-->
    <schema>  <!--schema定义-->
        <name>test</name> <!--schema名称-->
         <table>
            <sql> create table if not exists t_view (id BIGINT comment 'id view',nameExtension VARCHAR comment 'name extension')Engine='archer',Charset='gbk'
            </sql>
            <!--这是一个视图表，其表内容为下面的viewSql的执行结果-->
            <viewSql> select a.id*1000,a.name||b.extension,a.id from t_archer as a join t_archer as b where a.id=b.id </viewSql>
        </table>
        <table> <!--表定义-->
            <sql> <!--表定义sql-->
                create table if not exists t_archer( id BIGINT comment 'id test ', name VARCHAR comment 'name
                test',
                extension VARCHAR comment 'extension' )Engine='archer' SEP=',' SKIPWRONG='false' comment='just for test'
            </sql>
            <skipRows>3</skipRows> <!--忽略掉前skipRows行-->
            <pathPattern>/Users/alchemystar/tmp/rider/rider_%d{yyyy-MM-dd}.txt</pathPattern> <!--当前表对应的文件地址，可用时间格式渲染-->
        </table>
    </schema>
</database>
```
## 使用
直接连接 mysql -upa123 -pMi123 -P8090 -h127.0.0.1  
jdbc连接 jdbc:mysql://127.0.0.1/test?user=pay&password=MiraCle
### 创建表
```
create table if not exists t_archer( 
 id BIGINT comment 'id test ', 
 name VARCHAR comment 'name test',
extension VARCHAR comment 'extension' )
Engine='archer' SEP=',' Charset='gbk' SkipWrong='false'  comment='just for test'
```
Engine=archer 默认引擎，即默认以换行符和分隔符来组织文件的结构  
SEP=',',可以指定当前文件用哪种分隔符来分隔  
Charset='gbk',指定当前文件的编码格式   
SkipWrong='false' false:文件某一行列数和定义列数不匹配，则报error|true,忽略当前行   
### 配置表对应的文件路径
session内配置:  
```
set table_path="t_archer:/home/work/archer.txt" 
```  
配置文件内配置:   
```
<pathPattern>/Users/alchemystar/tmp/rider/rider_%d{yyyy-MM-dd}.txt</pathPattern> <!--当前表对应的文件地址，可用时间格式渲染-->
```
视图配置,viewSql可以是任意sql(可以用视图再次创建视图):
```
 <viewSql> select a.id*1000,a.name||b.extension,a.id from t_archer as a join t_archer as b where a.id=b.id </viewSql>
```
### 查询表
(1)支持\*符
```
select * from t_archer; 
```   
(2)支持行运算    
```
select (id+1)*6,name||extesion,extension from t_archer；
```
(3)支持where condition     
```
select * from t_archer where (id >1 and extension='rider') or (extension='archer')
```
(4)支持设置字符集
```
set names gbk;
```
(5)支持两表join
```
select a.id,b.id from t_rider as a join t_archer as b on a.id=b.id where id>1;
```




