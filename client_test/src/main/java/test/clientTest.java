package test;

import thread.RunnableDemo2;

public class clientTest {
     //运行测试
    public static void main(String[] args) throws Exception {
        //RunnableDemo r = new RunnableDemo();
        RunnableDemo2 r = new RunnableDemo2();
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        Thread t3 = new Thread(r);
        t1.start();
        t2.start();
        t3.start();
    }
}