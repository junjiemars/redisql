local nk = #KEYS
local na = #ARGV

if (0 < na) then
    local t = ARGV[1]
    local i = tonumber(ARGV[2])
    local ps = string.format('_T_[%s]_C_PK_', t)
    local pk = redis.call('smembers', ps)

    if (pk ~= nil) then
        local c = pk[1]
        local m = string.format('_T_[%s]:[%s]:<*>_', t, c)
        return redis.call('hgetall', r.edis.call('scan', i, 'match', m))
    end
end

return 0
