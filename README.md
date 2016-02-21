# redisql
Play Redis as a rational store via SQL

## Rules of Design
* 1st Rule: Redis is just Redis
* 2nd Rule: See the 1st Rule
* 3rd Rule: As fast as possible

## Play on the Fly
* how to build: ```lein uberjar```
* general parser: java -jar <redisql.jar> -e"aaaabbbb" -b@sample.bnf
* redisql cli mode: java -jar <redisql.jar> -s"select * from t1;"
* redisql repl mode: java -jar <redisql.jar> 

## Schemed Redis
* implement a set of basic sql semantics
* models: single node or master-slave
* naming: _{*}_
* versioning: think about it in future
* clustering: ok, think about it in future

### [Table](https://en.wikipedia.org/wiki/Table_(database))
* row: use Hash reprensents the row
* column: just as Hash's field
* cell: the intersect of a row and a column, identified by Hash's field and value pair
* primary key: one Table one primary key, which identifies a row, use Zset/Set represents it
* constraints: integrity check, such as **not null** or **default <value>**

It's awesome to **create table** into Redis, and so easy
```sql
create table t1 (
  id number(2,0) primary key,
  last_name varchar2(30) not null,
  salary number(4,2) default 0.0);
```

Somethimes the [Identity column](https://en.wikipedia.org/wiki/Identity_column) is required, so Redisql supports it.
```sql
create table t2 (
  id number(2,0) identity,
  last_name varchar2(30) not null,
  salary number(4,2) default 0.0);
```

### Describe schema
How to view the schema, it's a problem, so
```
describe;
describe t1;
describe t2;
```
### Insert
Just go:
```sql
insert into t1(id, last_name, salary) values (101, 'Anny', 1200);
insert into t2(last_name, salary) values('Ben', 1200.50);
```

### Update

### Delete

### Select

### Query optimizer

### Order by

### Relations

## References
* [Twitter clone using PHP and the Redis key-value store](http://redis.io/topics/twitter-clone)
* [Tree visitor in Clojure](http://www.ibm.com/developerworks/library/j-treevisit/)
* [A experimental SQL client for the Redis key-value store](https://github.com/kmanley/redisql)


