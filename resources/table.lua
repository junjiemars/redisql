local _t_n_ = '_T_N_'
local na = #ARGV
local level = redis.LOG_NOTICE

local def_table = function(t)
    local td = string.format('_T_[%s]_', t)
    if (0 == redis.call('sismember', _t_n_, t)) then
        local s = redis.call('sadd', _t_n_, t)
    end 
    redis.log(level, string.format('def table %s', td))
    return td
end

local def_column = function(t, c, v)
    local cs = t..'C_'
    local cd = string.format(cs..'[%s]_', v)
    redis.call('sadd', cs, v)
    redis.call('hset', cd, c, v)
    redis.log(level, string.format('def column %s', cd))
    return cd
end

local def_pk = function(t, v, an, av)
    local pk = t..'C_PK_'
    if ('PRIMARY_KEY' == an) then
        if (1 == tonumber(av)) then
            redis.call('sadd', pk, v)
        else
            redis.call('srem', pk, v)
        end
        redis.log(level, string.format('def pk %s:%s=%s', pk, v, av))
    end
    return v
end

local def_attribute = function(cd, an, av)
    redis.call('hset', cd, an, av)
    redis.log(level, string.format('def attribute %s:%s=%s', cd, an, av))
    return an
end

if (3 > na) or (0 == na % 2) then
    local m = 'should provides table, column, value arguments(>=3)'
    redis.log(level, m)
    return {-1, m}
end

local t = def_table(ARGV[1])
local c = ARGV[2]
local v = ARGV[3]
local d = def_column(t, c, v)

if (4 <= na) then
    for i=4,na,2 do
        local an = ARGV[i]
        local av = ARGV[i+1]
        def_pk(t, v, an, av)
        def_attribute(d, an, av)
    end
end

return {0, t, d}

