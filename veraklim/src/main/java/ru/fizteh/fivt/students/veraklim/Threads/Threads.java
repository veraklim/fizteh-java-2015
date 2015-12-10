package ru.fizteh.fivt.students.veraklim.Threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.concurrent.CyclicBarrier;

public class Threads {
    private CyclicBarrier barrier;
    private boolean flag = false;
    private int numberOfThreads;
    private List<CallingThread> listOfThreads;

    public class CallingThread extends Thread {

        private boolean callingResult;
        private Random random = new Random();
        private Threads commander;

        public CallingThread(Threads callingCommander) {
            this.commander = callingCommander;
        }

        @Override
        public void run() {
            while (!commander.flag) {
                if (random.nextInt(10) != 0) {
                    callingResult = true;
                    System.out.println("YES");
                } else {
                    callingResult = false;
                    System.out.println("NO");
                }
                try {
                    commander.barrier.await();
                } catch (Exception e) {
                }
            }
        }
    }
    public Threads(int numberOfThreads) {
        listOfThreads = new ArrayList<CallingThread>();
        this.numberOfThreads = numberOfThreads;
        barrier = new CyclicBarrier(numberOfThreads + 1, new Runnable() {
            @Override
            public void run() {
                flag = true;
                for (int i = 0; i < listOfThreads.size(); ++i) {
                    flag &= listOfThreads.get(i).callingResult;
                    listOfThreads.get(i);
                }
                if (!flag) {
                    System.out.println("Are you ready?");
                }
            }
        });
    }
    public void start() {
        System.out.println("Are you ready?");
        for (int i = 0; i < numberOfThreads; ++i) {
            listOfThreads.add(new CallingThread(this));
            listOfThreads.get(i).start();
        }
        while (!flag) {
            try {
                barrier.await();
            } catch (Exception e) {
            }
        }
    }
    public static void main(String[] args) {
        int numberOfThreads = 0;
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException();
            }
            numberOfThreads = Integer.valueOf(args[0]);
            if (numberOfThreads <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Argument must be positive");
            System.exit(1);
        }
        Threads commander = new Threads(numberOfThreads);
        commander.start();
    }
}

