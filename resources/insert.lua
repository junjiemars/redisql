local _t_n_ = '_T_N_'
local v = 0
local e_00947 = 947 --not enough values
local e_00913 = 913 --too many values
local e_00942 = 942 --table does not exist
local e_2270 = 2270 --primary key does not exist

local nk = table.getn(KEYS)
local na = table.getn(ARGV)
local insert = function (s, h, fn, fv) 
    local s1 = redis.call('sadd', s, h)
    local h1 = redis.call('hset', h, fn, fv)
    redis.debug(s1, h1)
    return s1+h1
end

if (0 < nk) then
    if (na < nk-1) then
        return e_00947
    elseif (na > nk-1) then
        return e_00913
    end

    local t = KEYS[1]
    local _t = string.format('_T_[%s]_', t)
    if (0 == redis.call('sismember', '_T_N_', _t)) then
        return e_00942
    else
        local npk = 0
        local del = {}
        for i=2,nk do
            local fn = KEYS[i]
            local fv = ARGV[i-1]
            local d = string.format(_t..'[%s]_', fn)
            local s = string.format(_t..'<%s>_', fn)
            local h = string.format(_t..':<%s>_', fn)
            local _t_t_pk_ = _t..'PK_'

            insert(s, h, fn, fv)

            if (1 == redis.call('sismember', _t_t_pk_, s)) then
                npk = npk+1
            elseif ('1' == redis.call('hget', d, 'PRIMARY_KEY')) then
                npk = npk+1 
                redis.call('sadd', _t_t_pk_, s)
            else
                del[#del+1] = h
            end
        end

        if (0 == npk) then
            local rm = redis.call('del', unpack(del))
            return e_2270
        end
    end
end

return v
