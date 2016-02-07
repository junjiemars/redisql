local nk = #KEYS
local na = #ARGV
local v = {}
local level = redis.LOG_NOTICE

local table_exists = function(t) 
    if (1 == redis.call('sismember', '_T_N_', t)) then
        return true
    end
    return false
end

local to_int = function(n)
    local i = tonumber(n)
    if i then
        return math.floor(i)
    end
    return nil
end

local pk_exists = function(t)
    local ps = string.format('_T_[%s]_C_PK_', t)
    local pk = redis.call('smembers', ps)
    return pk
end

if (2 > na) then
    return {-1, "should provides arguments [table cursor]"}
end

local t = ARGV[1]
if (not table_exists(t)) then
    return {00942, string.format('table does not exist', t)}
end

local i = to_int(ARGV[2])
if (not i) then
    return {01001, string.format('invalid cursor [%s]', ARGV[2])}
end

local pk = pk_exists(t)
if (nil == pk) then
    redis.log(level, string.format('no primary keys in table %s', t))
    return {02270, 'primary key does not exist'}
end

local c = pk[1]
local cs = string.format('_T_[%s]:[%s]_', t, c)
local pvs = redis.call('sscan', cs, i, 'count', 5)
local l = pvs[2]
v[#v+1] = pvs[1]
redis.debug(l)
for j=1,#l  do
    local r = string.format('_T_[%s]:[%s]:<%s>_', t, c, l[j])
    v[#v+1] = redis.call('hgetall', r)
end

return v
