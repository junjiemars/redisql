local n = table.getn(ARGV)

if (2 <= n) then
  local t = ARGV[1]
  local c = ARGV[2]
  local k = string.format('_T_%s', t)
  local s = k .. '_C_'

  if (1 == redis.call('sadd', s, c)) then
     return k .. '_:%s_'
  end
end
