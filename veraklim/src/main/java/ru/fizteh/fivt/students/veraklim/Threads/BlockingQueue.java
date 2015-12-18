package ru.fizteh.fivt.students.veraklim.Threads;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class BlockingQueue<T> {
    private Queue<T> queue;
    private int maxSize;
    private Lock lock = new ReentrantLock(true);
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();


    public BlockingQueue(int maxSize) {
        queue = new LinkedList<T>();
        this.maxSize = maxSize;
    }

    public List<T> take(int n) {
        try {
            lock.lock();
            List<T> answer = new ArrayList<T>();
            for (int i = 0; i < n; ++i) {
                while (queue.size() == 0) {
                    try {
                        notEmpty.await();
                    } catch (InterruptedException e) {
                        return null;
                    }
                }

                answer.add(queue.poll());
                notFull.signalAll();
            }
            return answer;
        } finally {
            lock.unlock();
        }
    }
    public void offer(List<T> list) {
        try {
           lock.lock();
            for (T element : list) {
                while (queue.size() == maxSize) {
                        try {
                            notFull.await();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    queue.add(element);
                    notEmpty.signal();
            }
        } finally {
           lock.unlock();
        }
    }
}
