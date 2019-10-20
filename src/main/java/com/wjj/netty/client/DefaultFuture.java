package com.wjj.netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultFuture {
    private static Logger logger= LoggerFactory.getLogger(DefaultFuture.class);
    private final static ConcurrentHashMap<Long, DefaultFuture> allDefaultFuture = new ConcurrentHashMap<>();
    private Response response;
    private final Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    public DefaultFuture(ClientRequest request) {
        allDefaultFuture.put(request.getId(), this);
    }

    /**
     * 主线程获取数据，首先要等待结果
     *
     * @return
     */
    public Response get() {
        lock.lock();
        try {
            while (!isGetResponse()) {
                condition.await();
            }
        } catch (Exception e) {
            logger.error("主线程获取数据异常",e);
        }

        return this.getResponse();
    }

    /**
     * 是否已经有了响应
     *
     * @return
     */
    private boolean isGetResponse() {
        if (this.getResponse() != null) {
            return true;
        }
        return false;

    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
