-- KEYS[1] 库存hash：seckill:stock:{activityId}  field=productId  value=剩余库存
-- KEYS[2] 已购hash：seckill:user:{activityId}   field=productId:userId  value=1
-- ARGV[1] productId  ARGV[2] userId  ARGV[3] 购买数量
-- 返回：1预扣成功  0库存不足  -1重复下单  -2秒杀商品不存在

-- 1.一人一单：已购标记不存在时设置成功，已存在说明抢过
if redis.call('HSETNX', KEYS[2], ARGV[1] .. ':' .. ARGV[2], '1') == 0 then
    return -1
end

-- 2.读取剩余库存；缓存不存在说明活动未发布或缓存失效，回滚已购标记
local stock = tonumber(redis.call('HGET', KEYS[1], ARGV[1]))
if stock == nil then
    redis.call('HDEL', KEYS[2], ARGV[1] .. ':' .. ARGV[2])
    return -2
end

-- 3.库存不足，回滚已购标记（不能挡住用户去抢其它商品）
if stock < tonumber(ARGV[3]) then
    redis.call('HDEL', KEYS[2], ARGV[1] .. ':' .. ARGV[2])
    return 0
end

-- 4.预扣库存
redis.call('HSET', KEYS[1], ARGV[1], stock - tonumber(ARGV[3]))
return 1
