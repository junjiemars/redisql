local _t_n_ = '_T_N_'
local v = {} 
local nk = table.getn(KEYS)
local na = table.getn(ARGV)

if (0 < nk) then
    if (na < nk-1) then
        v = {00947, 'not enough values'}
        return v
    else if (na > nk-1) then
        v = {00913, 'too many values'}
        return v
    end

    local t = KEYS[1]
    local _t = string.format('_T_[%s]_', t)
    if (0 == redis.call('sismember', '_T_N_', _t)) then
        v = {00942, string.format('table %s does not exist', t)}
        return v
    else
        -- needs column verification
        --
        local is_pk = false
        for c in redis.call('smembers', _t_n_) do
            local d = string.format(_t..'<%s>_', c) 
            if (0 == redis.call(d, 'PRIMARY_KEY')) then
                is_pk = true
            end 
        end
        if (not is_pk) then
            v = {}
        end
    end
end

return v
