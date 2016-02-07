local _t_n_ = '_T_N_'
local nk = #KEYS
local na = #ARGV
local level = redis.LOG_NOTICE
local m = 'not enough columns or values'

local table_exists = function(t) 
    return 1 == redis.call('sismember', _t_n_, t)
end

local is_pk = function(pk, c)
    return (1 == redis.call('sismember', pk, c))
end

local ins = function (t, pks, row) 
    local n = 0
    for k,v in pairs(pks) do
        local pk = string.format('_T_[%s]:[%s]_', t, k)
        local r = string.format('_T_[%s]:[%s]:<%s>_', t, k, v)
        redis.call('sadd', pk, v)
        redis.call('hmset', r, unpack(row))
        n = n+1
    end
    return n
end

if (3 > na) or (0 == na % 2) then
    m = 'not enough columns or values(>=3)'
    return {00947, m}
end

local t = ARGV[1]
if (not table_exists(t)) then
    m = string.format('table %s does not exist', t)
    redis.log(level, m)
    return {00942, m}
end

local pk = string.format('_T_[%s]_C_PK_', t)
if (0 == redis.call('scard', pk)) then
    m = 'primary key does not exist'
    redis.log(level, m)
    return {02270, m}
end

local pks = {}
local row = {}
local cnt = 0
for i=2,na,2 do
    local c = ARGV[i]
    local v = ARGV[i+1]
    if (is_pk(pk, c)) then
        pks[c] = v
        cnt = cnt+1
    end
    row[#row+1] = c
    row[#row+1] = v
end

if (0 == cnt) then
    m = string.format('primary key column is missing in table %s', t) 
    redis.log(level, m)
    return {01400, m}
end

ins(t, pks, row)

m = string.format('insert table %s is ok', t)
redis.log(level, m)
return {1, m}

