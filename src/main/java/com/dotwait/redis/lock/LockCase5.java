package com.dotwait.redis.lock;

import redis.clients.jedis.Jedis;

import java.time.LocalTime;

import static com.dotwait.redis.constant.LockConstant.*;

/**
 * 增加定时刷新锁过期时间，防止业务处理时间大于锁过期时间
 */
public class LockCase5 extends RedisLock {
    public LockCase5(Jedis jedis, String lockKey) {
        super(jedis, lockKey);
    }

    /**
     * 自旋锁
     */
    @Override
    public void lock() {
        while (true) {
            String result = jedis.set(lockKey, "value", NOT_EXIST, SECONDS, 30);
            if (OK.equals(result)) {
                System.out.println(Thread.currentThread().getId() + "加锁成功，时间：" + LocalTime.now());

                //开启定时刷新过期时间
                isOpenExpirationRenewal = true;
                scheduleExpirationRenewal();
                break;
            }
            System.out.println("线程id:" + Thread.currentThread().getId() + "获取锁失败，休眠10秒!时间:" + LocalTime.now());
            //休眠10秒
            sleepBySecond(10);
        }
    }

    /**
     * 释放锁分为获取值、判断和删除锁，未确保原子性
     * 利用lua脚本可实现解锁操作的原子性
     */
    @Override
    public void unlock() {
        String checkAndDelScript = "if redis.call(\"get\", KEYS[1]) == ARGV[1] then\n" +
                "    return redis.call(\"del\", KEYS[1])\n" +
                "else\n" +
                "    return 0\n" +
                "end";
        jedis.eval(checkAndDelScript, 1, lockKey, lockValue);
        isOpenExpirationRenewal = false;
    }
}
