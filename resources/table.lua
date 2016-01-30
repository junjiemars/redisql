local _t_n_ = '_T_N_'
local nk = table.getn(KEYS)
local na = table.getn(ARGV)

local def_table = function(t)
    local _t_t_ = string.format('_T_[%s]_', t)
    if (0 == redis.call('sismember', _t_n_, _t_t_)) then
        local s = redis.call('sadd', _t_n_, _t_t_)
        redis.debug(s)
    end 
    return _t_t_
end

local def_column = function(t, c)
    local cs = t..'C_'
    local cd = string.format(t..'[%s]_', c)
    if (1 == redis.call('sadd', cs, c)) then
        redis.call('hset', cd, 'NAME', c)
    end
    return cd
end

local make_column = function(t, cn, c, n, v)
    local d = redis.call('hset', c, n, v)
    if ('PRIMARY_KEY' == n) then
        local pk = t..'PK_'
        if ('1' == v) then
            redis.call('sadd', pk, cn)
        else
            redis.call('srem', pk, cn)
        end
    end
    return d
end

-- keys argv:
-- table column_name column_defintions column
if (nk+2 == na) then
    local t = def_table(ARGV[1])
    local cn = ARGV[2]
    local c = def_column(t, cn)
    if (0 < nk) then
        for i=1,nk do
            local n = KEYS[i]
            local v = ARGV[i+2]
            local d = make_column(t, cn, c, n, v)
            redis.debug(d)
        end
        return 1
    end
end

return 0
