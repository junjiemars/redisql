local na = #ARGV
local v = {}
local level = redis.LOG_NOTICE

local table_exists = function(t) 
    if (1 == redis.call('sismember', '_T_N_', t)) then
        return true
    end
    return false
end

local find_pk = function(t)
    local pkd = string.format('_T_[%s]_C_PK_', t)
    local cd = redis.call('get', pkd)
    if (nil == cd) then
        return nil
    end
    local d = string.format('_T_[%s]_C_[%s]_', t, cd)
    local v = redis.call('hmget', d, 'PRIMARY_KEY', 'TYPE')
    if (nil == v) then
        return nil
    end
    return {n=cd, pk=v[1], t=v[2]}
end

if (2 > na) then
    return {-1, "should provides arguments [table cursor]"}
end

local t = ARGV[1]
if (not table_exists(t)) then
    return {-00942, string.format('table does not exist', t)}
end

local pk = find_pk(t)
if (nil == pk) then
    redis.log(level, string.format('no primary keys in table %s', t))
    return {-02270, 'primary key does not exist'}
end

local v = ARGV[2]
local vd = string.format('_T_[%s]:[%s]:<%s>_', t, pk['n'], v)
local r1 = {}
r1[#r1+1] = {0, 10}
r1[#r1+1] = redis.call('hgetall', vd)

return r1;
