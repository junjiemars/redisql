local _t_n_ = '_T_N_'
local na = #ARGV
local level = redis.LOG_NOTICE
local m = 'not enough columns or values'

local table_exists = function(t) 
    return 1 == redis.call('sismember', _t_n_, t)
end

local find_pk = function(t) 
    local pkd = string.format('_T_[%s]_C_PK_', t)
    local c = redis.call('get', pkd)
    if (nil == c) then
        return nil
    end
    local cd = string.format('_T_[%s]_C_[%s]_', t, c)
    local pkv = redis.call('hmget', cd, 'PRIMARY_KEY', 'TYPE')
    if (nil == pkv) or ('0' == pkv[1]) then
        return nil
    end
    local v = {n=c, p=tonumber(pkv[1]), t=pkv[2]}
    redis.log(level, v)
    return v
end

local ins_id = function(t, c) 
    local pkd = string.format('_T_[%s]:[%s]_', t, c)
    local snd = string.format('_T_[%s]_C_SN_', t)
    local sn = redis.call('incr', snd)
    local v = redis.call('zadd', pkd, sn, sn)
    local rd = string.format('_T_[%s]:[%s]:<%s>_', t, c, sn)
    local r = redis.call('hset', rd, c, sn)
    redis.log(level, string.format('ins_id(%s, %s)', t, c))
    return sn
end

local ins = function (t, pk, row) 
    local n = 0
    local pkd = string.format('_T_[%s]:[%s]_', t, pk['n'])
    if (nil ~= pk['v']) then
        if ('STRING' == pk['t']) then
            n = redis.call('zadd', pkd, 0, pk['v'])
        else
            n = redis.call('zadd', pkd, pk['v'], pk['v'])
        end
    else
        local snd = string.format('_T_[%s]_C_SN_', t)
        pk['v'] = redis.call('get', snd)
    end
    local rd = string.format('_T_[%s]:[%s]:<%s>_', t, pk['n'], pk['v'])
    if ('+OK' == redis.call('hmset', rd, unpack(row))) then
        n = n+1
    end
    return n
end

if (3 > na) or (0 == na % 2) then
    m = 'not enough columns or values(>=3)'
    redis.log(level, m)
    return {00947, m}
end

local t = ARGV[1]
if (not table_exists(t)) then
    m = string.format('table %s does not exist', t)
    redis.log(level, m)
    return {00942, m}
end

local pk = find_pk(t)
if (nil == pk) then
    m = 'primary key does not exists'
    redis.log(level, m)
    return {02270, m}
end

local row = {}
local cnt = 0
for i=2,na,2 do
    local c = ARGV[i]
    local v = ARGV[i+1]
    if (c == pk['n']) then
        pk['v'] = v
        cnt = cnt+1
    end
    row[#row+1] = c
    row[#row+1] = v
end

if (0 == cnt) then
    if (3 > pk['p']) then
        m = string.format('primary key column is missing in table %s', t) 
        redis.log(level, m)
        return {01400, m}
    else
        ins_id(t, pk['n'])
    end
end

ins(t, pk, row)

m = string.format('insert table %s is ok', t)
redis.log(level, m)
return {1, m}

