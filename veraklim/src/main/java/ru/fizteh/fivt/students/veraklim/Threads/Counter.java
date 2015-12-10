package ru.fizteh.fivt.students.veraklim.Threads;

public class Counter {
    private static volatile int cur_Id;
    private static Object monitor = new Object();

    private static class CountThread extends Thread {
        private int Id, next_Id;
        @Override
        public void run() {
            while (true) {
                synchronized (monitor) {
                    while (Id != cur_Id) {
                        try {
                            monitor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Thread-" + String.valueOf(Id + 1));
                    cur_Id = next_Id;
                    monitor.notifyAll();
                }
            }
        }
        CountThread(int id, int next_id) {
            this.Id = id;
            this.next_Id = next_id;
        }
    }
    public static void main(String[] args) {
        int n = 0;
        try {
            n = Integer.valueOf(args[0]);
            if (n <= 0) {
                throw new NumberFormatException("");
            }
        } catch (Exception e) {
            System.out.print("Argument must be positive");
            return;
        }
        cur_Id = 0;
        for (int i = 0; i < n; i++) {
            CountThread thread = new CountThread(i, (i + 1) % n);
            thread.start();
        }
    }
}
