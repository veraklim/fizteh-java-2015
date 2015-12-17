package ru.fizteh.fivt.students.veraklim.Threads;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class BlockingQueue<T> {
    private Queue<T> Queue;
    private Lock write_Lock = new ReentrantLock(true);
    private Lock read_Lock = new ReentrantLock(true);
    private Lock Lock = new ReentrantLock(true);
    private Condition not_Full = Lock.newCondition();
    private Condition not_Empty = Lock.newCondition();
    private int maxSize;

    public BlockingQueue(int maxSize) {
        Queue = new LinkedList<T>();
        this.maxSize = maxSize;
    }

    public List<T> take(int n) {
        try {
            read_Lock.lock();
            List<T> answer = new ArrayList<T>();
            for (int i = 0; i < n; ++i) {
                try {
                    Lock.lock();
                    while (Queue.size() == 0) {
                        try {
                            not_Empty.await();
                        } catch (InterruptedException e) {
                            return null;
                        }
                    }
                    answer.add(Queue.poll());
                    not_Full.signalAll();
                } finally {
                    Lock.unlock();
                }
            }
            return answer;
        } finally {
            read_Lock.unlock();
        }
    }

    public void offer(List<T> list) {
        try {
            write_Lock.lock();
            for (T element : list) {
                try {
                    Lock.lock();
                    while (Queue.size() == maxSize) {
                        try {
                            not_Full.await();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    Queue.add(element);
                    not_Empty.signal();
                } finally {
                    Lock.unlock();
                }
            }
        } finally {
            write_Lock.unlock();
        }
    }
}
