local v = {}
local _t_ = '_T_'
local _t_n_ = '_T_N_'
local _t_t_c_ = '_T_<T>_C_'
local _t_t_d_c = '_T_<T>_:C_'

if (0 == redis.call('exists', _t_)) then
   v[_t_] = redis.call('hmset', _t_,
                       'TABLES', _t_n_,
                       'TABLES_COMMENT', 'The set hold tables name',
                       'CLOUMNS', _t_t_c_,
                       'COLUMNS_COMMENT', 'The set hold columns name, <T> will be replaced with Table name',
                       'COLUMN_DEFINE', _t_t_d_c,
                       'COLUMN_DEFINE_COMMENT', 'The hash hold column definition')
end

local n_keys = table.getn(KEYS)
local n_args = table.getn(ARGV)

if (0 < n_keys) then
   local t_ = string.format("_T_%s_", KEYS[1])
   v[_t_n_] = redis.call('sadd', _t_n_, t_)
end

return {v[_t_], v[_t_n_], n_args}
