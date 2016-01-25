local root = 0
local _t_dn_ = '_T_DN_'
local _t_rn_ = '_T_RN_'
local table = string.upper(keys[1])

redis.call(multi)

if (0 == redis.call('exists', root)) then
   root = redis.call('sadd', '_T_', _t_dn_)
   root = root + redis.call('sadd', '_T_', _t_rn_) 
end

if (0 == redis.call('sismember', table) then
   local trn = string.format('_T_%s_', table)
   local ttc = string.format('_T_%s_C_', table)
   redis.call('sadd', _t_dn_, table)
   redis.call('sadd', _t_rn_, trn)
   redis.call('hmset', _t_rn_, 'NAME', table, 'COLUMN', ttc)
end

return {root}