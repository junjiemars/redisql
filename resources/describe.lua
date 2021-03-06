local _t_ = '_T_'
local _t_n_ = '_T_N_'
local na = #ARGV
local level = redis.LOG_NOTICE
local v = {}

local table_exists = function(t)
    return 1 == redis.call('sismember', _t_n_, t)
end

if (0 == na) then
    local d = redis.call('hgetall', _t_)
    if (nil == d) then
        v = {0, 'scheme does not exists'}
        redis.log(level, v[#v])
    else
        v = {#d, d}
    end
    return v
end

local t = ARGV[1]
if (not table_exists(t)) then
    v = {0, string.format('table %s does not exists', t)}
    redis.log(level, v[#v])
    return v
end

local d = string.format(_t_..'[%s]_C_', t)
local cc = redis.call('smembers', d)
if (nil == cc) then
    v = {0, string.format('column\'s definition of table %s does not exists', t)}
    redis.log(level, v[#v])
    return v
end

v[#v+1] = #cc
for i=1,#cc do
    local dc = string.format(d..'[%s]_', cc[i])
    v[#v+1] = redis.call('hgetall', dc)
end

return v

