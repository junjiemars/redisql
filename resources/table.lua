local _t_n_ = '_T_N_'
local nk = table.getn(KEYS)
local na = table.getn(ARGV)
local level = redis.LOG_NOTICE

local def_table = function(t)
    local td = string.format('_T_[%s]_', t)
    if (0 == redis.call('sismember', _t_n_, td)) then
        local s = redis.call('sadd', _t_n_, td)
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

local mk_column = function(cd, an, av)
    if (1 == redis.call('hset', cd, an, av)) then
    end
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
        mk_column(d, an, av)
    end

    return {t, d}
end

return {_t_n_}
