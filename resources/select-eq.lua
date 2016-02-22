local na = #ARGV
local m = nil
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

if (4 > na) then
    m = "should provides enough arguments(>=4)"
    redis.log(level, m)
    return {-1, m}
end

local t = ARGV[1]
if (not table_exists(t)) then
    m = string.format('table does not exist', t)
    redis.log(level, m)
    return {-00942, m}
end

local op = ARGV[2]
if ('=' ~= op) then
    m = string.format('unsupported operator: %s', op)
    redis.log(level, m)
    return {-30462, m}
end

local pk = find_pk(t)
local c = ARGV[3]
if (nil == pk) or (c ~= pk['n']) then
    m = string.format('%s is not the primary key in table:%s', c, t)
    redis.log(level, m)
    return {-02270, m}
end

local v = ARGV[4]
local pkd = string.format('_T_[%s]:[%s]:<%s>_', t, pk['n'], v)
local r1 = {}
r1[#r1+1] = {0, 1}
r1[#r1+1] = redis.call('hgetall', pkd)

redis.log(level, string.format('retrived [%s %s]', 0, 1))

return r1;
