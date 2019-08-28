package com.dotwait.redis.lock;

import redis.clients.jedis.Jedis;

import static com.dotwait.redis.constant.LockConstant.NOT_EXIST;
import static com.dotwait.redis.constant.LockConstant.OK;

public class LockCase1 extends RedisLock {

    public LockCase1(Jedis jedis, String lockKey) {
        super(jedis, lockKey);
    }

    /**
     * 自旋锁，无过期时间，可能导致锁一直存在，自旋没有指定时间，可能线程一直自旋
     */
    @Override
    public void lock(){
        while (true){
            String result = jedis.set(lockKey, "value", NOT_EXIST);
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
