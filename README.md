# redisql
Play Redis as a rational store via SQL

## Rules of Design
* The 1st Rule: Redis is just Redis
* The 2nd Rule: See the 1st Rule
* The 3rd Rule: As fast as possible

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

### Table

### Column

### Describe schemed objects

### Insert

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


