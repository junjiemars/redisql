local _t_ = '_T_'
local _t_n_ = '_T_N_'
local na = #ARGV
local level = redis.LOG_NOTICE
local v = {}

local table_exists = function(t)
    return 1 == redis.call('sismember', _t_n_, t)
end

if (0 == na) then
    v = {0, 'should provides table name argument(>=1)'}
    redis.log(level, v[#v])
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
    v = {0, 'can not find column defition'}
else
    v = {#cc, cc}
end

return v

