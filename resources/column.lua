--get column's Hash

if (0 == table.getn(KEYS)) then
  return 0
else
  local t = KEYS[1]
  local c = KEYS[2]
  local k = string.format('_T_%s_C_', t)
  return redis.call('sadd', k, c)
end