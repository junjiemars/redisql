-- get column's Hash

local n = table.getn(KEYS)
local k1 = tonumber(KEYS[1])

if (0 < n) then
  local t = KEYS[1]
  local c = KEYS[2]
  local k = string.format('_T_%s', t)
  local s = k .. '_C_'

  if (1 == redis.call('sadd', s, c)) then
     return k .. '_:%s_'
  end
end
