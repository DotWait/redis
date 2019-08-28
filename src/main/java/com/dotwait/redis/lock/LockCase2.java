package com.dotwait.redis.lock;

import redis.clients.jedis.Jedis;

import static com.dotwait.redis.constant.LockConstant.*;

/**
 * 增加了锁的过期时间
 */
public class LockCase2 extends RedisLock {
    public LockCase2(Jedis jedis, String lockKey) {
        super(jedis, lockKey);
    }

    /**
     * 自旋锁，有过期时间，可能占有锁的线程A处理业务的时间超过了锁的过期时间，
     * 导致锁自动释放后被其他线程B重新获取，线程A处理完后又释放了线程B的锁
     *
     * 问题：
     * （1）锁的过期时间如何保证大于业务的处理时间
     * （2）如何保证线程A的锁不会被其他线程误删除
     * 解决：设置value在足够长的时间内每个线程的值唯一
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

    @Override
    public void unlock(){
        jedis.del(lockKey);
    }
}
