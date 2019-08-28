package com.dotwait.redis.lock;

import redis.clients.jedis.Jedis;

import static com.dotwait.redis.constant.LockConstant.*;

/**
 * 释放锁使用lua脚本，保证原子性
 */
public class LockCase4 extends RedisLock {
    public LockCase4(Jedis jedis, String lockKey) {
        super(jedis, lockKey);
    }

    /**
     * 自旋锁，有过期时间，可能占有锁的线程A处理业务的时间超过了锁的过期时间，
     * 导致锁自动释放后被其他线程B重新获取
     *
     * 问题：
     * （1）锁的过期时间如何保证大于业务的处理时间
     */
    @Override
    public void lock(){
        while (true){
            String result = jedis.set(lockKey, "value", NOT_EXIST, SECONDS, 30);
            if (OK.equals(result)){
                System.out.println(Thread.currentThread().getId() + "加锁成功");
                break;
            }
        }
    }

    /**
     * 释放锁分为获取值、判断和删除锁，未确保原子性
     * 利用lua脚本可实现解锁操作的原子性
     */
    @Override
    public void unlock(){
        String checkAndDelScript = "if redis.call(\"get\", KEYS[1]) == ARGV[1] then\n" +
                "    return redis.call(\"del\", KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end";
        jedis.eval(checkAndDelScript,1, lockKey, lockValue);
    }
}
