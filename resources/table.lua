local _t_n_ = '_T_N_'
local nk = table.getn(KEYS)
local na = table.getn(ARGV)
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
        redis.log(level, string.format('def pk %s=%s', v, av))
    end
    return v
end

local mk_column = function(cd, an, av)
    redis.call('hset', cd, an, av)
    redis.log(level, string.format('mk column %s', an))
    return an
end

if (0 < na) then
    local t = def_table(ARGV[1])
    local c = ARGV[2]
    local v = ARGV[3]
    local d = def_column(t, c, v)

    for i=4,na,2 do
        local an = ARGV[i]
        local av = ARGV[i+1]
        def_pk(t, v, an, av)
        mk_column(d, an, av)
    end

    return {t, d}
end

return {_t_n_}
