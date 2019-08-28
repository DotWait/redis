package com.dotwait.redis;

import com.dotwait.redis.lock.LockCase5;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisApplicationTests {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Test
    public void contextLoads() {
        redisTemplate.opsForValue().get("key");
    }

    private String lockName = "lock";

    /**
     * 显示锁的过期时间
     */
    @Before
    public void showLockExpireTime() {
        new Thread(() -> {
            while (true) {
                try {
                    Jedis jedis = new Jedis("127.0.0.1", 6379);
                    Thread.sleep(5000);
                    System.out.println("锁的过期时间:" + jedis.ttl(lockName) + "秒");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Test
    public void testLockCase5() {
        //定义线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(0, 10,
                1, TimeUnit.SECONDS,
                new SynchronousQueue<>());

        //添加10个线程获取锁
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> {
                try {
                    Jedis jedis = new Jedis("localhost");
                    LockCase5 lock = new LockCase5(jedis, lockName);
                    lock.lock();

                    //模拟业务执行15秒
                    lock.sleepBySecond(15);

                    lock.unlock();
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        }

        //当线程池中的线程数为0时，退出
        while (pool.getPoolSize() != 0) {}
    }

}
