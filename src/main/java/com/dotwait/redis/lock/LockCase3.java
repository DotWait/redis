package com.dotwait.redis.lock;

import redis.clients.jedis.Jedis;

import static com.dotwait.redis.constant.LockConstant.*;

/**
 * 加锁的value指定线程id+UUID随机值，保证线程A不会释放线程B的锁
 */
public class LockCase3 extends RedisLock {
    public LockCase3(Jedis jedis, String lockKey) {
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
        String lockValue = jedis.get(lockKey);
        if (this.lockValue.equals(lockValue)){
            jedis.del(lockKey);
        }
    }
}
