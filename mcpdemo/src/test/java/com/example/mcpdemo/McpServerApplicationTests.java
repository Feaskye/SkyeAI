package com.example.mcpdemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootTest
class McpServerApplicationTests {

    @Test
    void contextLoads() {

        CopyOnWriteArrayList copyOnWriteArrayList=new CopyOnWriteArrayList();
        copyOnWriteArrayList.add("");
        copyOnWriteArrayList.get(0);

        ConcurrentHashMap chashMap=new ConcurrentHashMap();
        chashMap.put("","");

        ThreadLocal threadLocal=new ThreadLocal();
        threadLocal.set(1);
        threadLocal.get();

        ReentrantLock reentrantLock=new ReentrantLock();
        reentrantLock.tryLock();

    }

}
