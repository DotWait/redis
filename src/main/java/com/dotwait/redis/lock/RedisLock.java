package com.dotwait.redis.lock;

import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class RedisLock implements Lock {
    protected Jedis jedis;
    protected String lockKey;
    protected String lockValue;
    protected volatile boolean isOpenExpirationRenewal = true;

    public RedisLock(Jedis jedis, String lockKey){
        this(jedis, lockKey,
                UUID.randomUUID().toString() + Thread.currentThread().getId());
    }

    public RedisLock(Jedis jedis, String lockKey, String lockValue){
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.lockValue = lockValue;
    }

    /**
     * 开启定时刷新
     */
    protected void scheduleExpirationRenewal(){
        new Thread(new ExpirationRenewal()).start();
    }

    /**
     * 刷新key的过期时间
     */
    private class ExpirationRenewal implements Runnable{

        @Override
        public void run() {
            while (isOpenExpirationRenewal){
                System.out.println("执行延迟失效时间中...");
                String checkAndExpireScript = "if redis.call(\"get\", KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call(\"expire\", KEYS[1], ARGV[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                jedis.eval(checkAndExpireScript, 1, lockKey, lockValue, "30");

                sleepBySecond(10);
            }
        }
    }

    public void sleepBySecond(int second){
        try {
            java.lang.Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
