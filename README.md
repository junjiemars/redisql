# redisql
Play Redis as a rational database via SQL

## Rules of Design
* 1st Rule: Redis is just Redis
* 2nd Rule: See 1st Rule
* 3rd Rule: As Fast as Redis

## Play on the Fly
* java -jar <redisql.jar> -e"aaaabbbb" -b@sample.bnf

## Debug in Cider
* (-main "-e" "aaaabbb")

## Schemed Redis
* naming: _S{set}_
* versioning: base on 0

### Tables
* _T_ as set, store the scheme root;
* _T_DN_ as set, store rational **Database** tables name;
* _T_RN_ as set, store **Redis** tables name;
* _T_<T>_ as hash, sstore table's scheme: NAME, COLUMN;
* _T_<T>_C_ as set, store column's name of the table _T_<T>_::COLUMN
* _T_<T>_C_:<C> as hash, store column's scheme of the column _T_<T>_C_: NAME, NULL?, PK?, DEFAULT



