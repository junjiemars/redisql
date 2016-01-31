local nk = table.getn(KEYS)
local na = table.getn(ARGV)
local ll = redis.LOG_NOTICE

redis.log(ll, '-----------------')

for i=1,nk do
    local m = string.format('KEYS[%s]=%s', i, KEYS[i])
    redis.log(ll, m)
end

for j=1,na do
    local m = string.format('ARGV[%s]=%s', j, ARGV[j])
    redis.log(ll, m)
end

return {nk, na}
