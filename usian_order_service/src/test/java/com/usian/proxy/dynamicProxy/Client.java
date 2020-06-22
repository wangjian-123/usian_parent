package com.usian.proxy.dynamicProxy;

import java.lang.reflect.Proxy;

public class Client {

    public static void main(String[] args) {
        RealStar realStar = new RealStar();
        ProxyStar proxyStar = new ProxyStar(realStar);
        Star proxy = (Star) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                //Start：要生哪个接口的代理类
                //handler：代理类要做的事情
                new Class[]{Star.class}, proxyStar);
        proxy.sing();
    }
}
