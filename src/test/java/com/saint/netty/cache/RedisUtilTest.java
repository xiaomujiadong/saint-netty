package com.saint.netty.cache;

import static org.junit.Assert.*;

import com.saint.netty.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @description:
 * @date: 2019/9/10 20:08
 */
public class RedisUtilTest extends BaseTest {

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void testRedis(){
        System.out.println(redisUtil.get("test12121"));
        System.out.println(redisUtil.set("test12121", "hello redis"));
        System.out.println(redisUtil.get("test12121"));
    }

}