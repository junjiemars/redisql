# redisql
redis query language as sql

## Play on the Fly
* java -jar <redisql.jar> -e"aaaabbbb" -b@sample.bnf

## Debug in Cider
* (-main "-e" "aaaabbb")

## Schemed Redis
* naming: _S{set}_
* versioning: base on 0

### Tables
* _TN_ as set, store table names;
* _T_<T>_ as hash, sstore table's scheme: NAME, COLUMN;
* _T_<T>_C_ as set, store column's name of the table _T_<T>_::COLUMN
* _T_<T>_C_:<C> as hash, store column's scheme of the column _T_<T>_C_: NAME, NULL?, PK?, DEFAULT

