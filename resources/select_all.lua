local nk = #KEYS
local na = #ARGV
local v = {}
local level = redis.LOG_NOTICE

if (0 < na) then
    local t = ARGV[1]
    local i = tonumber(ARGV[2])
    local ps = string.format('_T_[%s]_C_PK_', t)
    local pk = redis.call('smembers', ps)

    if (nil == pk) then
        redis.log(level, string.format('no primary keys in table %s', t))
        return v
    end

    repeat
        local c = pk[1]
        local cs = string.format('_T_[%s]:[%s]_', t, c)
        local pvs = redis.call('sscan', cs, i)
        local l = pvs[2]
        redis.debug(l)
        for j=1,#l  do
            local r = string.format('_T_[%s]:[%s]:<%s>_', t, c, l[j])
            v[#v+1] = redis.call('hgetall', r)
        end
    until 0 == i
    return v
end

return v
