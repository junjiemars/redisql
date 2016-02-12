local _t_ = '_T_'
local _t_n_ = '_T_N_'
local na = #ARGV
local level = redis.LOG_NOTICE
local v = {}

local table_exists = function(t)
    return 1 == redis.call('sismember', _t_n_, t)
end

if (0 == na) then
    v[#v+1] = redis.call('hgetall', _t_)
    return v
end

local t = ARGV[1]
if (table_exists(t)) then
    local d = string.format(_t_..'[%s]_C_', t)
    local cc = redis.call('smembers', d)
    for i=1,#cc do
        local dc = string.format(d..'[%s]_', cc[i])
        v[#v+1] = redis.call('hgetall', dc)
    end
end

return v

