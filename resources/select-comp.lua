local na = #ARGV
local cnt = 10
local level = redis.LOG_NOTICE
local m = nil

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

local to_int = function(n)
    local i = tonumber(n)
    if i then
        return math.floor(i)
    end
    return nil
end

if (5 > na) then
    m = "should provides enough arguments(>=5)"
    return {-1, m}
end

local t = ARGV[1]
if (not table_exists(t)) then
    m = string.format('table: %s does not exist', t)
    return {-00942, m}
end

local i = to_int(ARGV[2])
if (not i) then
    m = string.format('invalid cursor [%s]', ARGV[2])
    redis.log(level, m)
    return {-01001, m}
end

local pk = find_pk(t)
local c = ARGV[4]
if (nil == pk) or (c ~= pk['n']) then
    m = string.format('%s is not the primary key in table:%s', c, t)
    redis.log(level, m)
    return {-02270, m}
end

local op = ARGV[3]
local v = ARGV[5]
local zop = nil
if ('>' == op) then
    zop = {l='('..v, r='+'}
elseif ('>=' == op) then
    zop = {l='['..v, r='+'}
elseif ('<' == op) then
    zop = {l='-', r='('..v}
elseif ('<=' == op) then
    zop = {l='-', r='['..v}
else
    m = string.format('unsupported operator: %s', op)
    redis.log(level, m)
    return {-30462, m}
end

local pkd = string.format('_T_[%s]:[%s]_', t, pk['n'])
local pks = redis.call('zrangebylex', pkd, zop['l'], zop['r'], 'limit', i, cnt)
local rows = {}
rows[#rows+1] = {i, #pks}
for j=1,#pks do
    local rid = string.format('_T_[%s]:[%s]:<%s>_', t, pk['n'], pks[j])
    rows[#rows+1] = redis.call('hgetall', rid)
end
redis.log(level, string.format('retrived [%s %s]', i, #pks))

return rows;
