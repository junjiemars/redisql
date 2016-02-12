local v = {}
local _t_ = '_T_'
local _t_n_ = '_T_N_'
local _t_t_c_ = '_T_[T]_C_'
local _t_t_d_c = '_T_[T]_<C>_'
local level = redis.LOG_NOTICE
local m = 'create redisql\'s scheme %s'

local t = redis.call('hmset', _t_,
                     'TABLES', _t_n_,
                     'TABLES_COMMENT', 'The set hold tables name',
                     'CLOUMNS', _t_t_c_,
                     'COLUMNS_COMMENT', 'The set hold columns name, [T] will be replaced with Table name',
                     'COLUMN_DEFINE', _t_t_d_c,
                     'COLUMN_DEFINE_COMMENT', 'The hash hold column definition, [C] will be replaced with Column name')

if ('OK' == t['ok']) then
    v[#v+1] = 0
    v[#v+1] = string.format(m, 'ok')
else
    v[#v+1] = -1
    v[#v+1] = string.format(m, 'failed')
end

redis.log(level, v[#v])
return v
