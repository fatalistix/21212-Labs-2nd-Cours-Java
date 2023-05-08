package ru.nsu.balashov.torrent;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;

public class App {
    private static Runnable parametrizedCallable(final int i) {
        return () -> {
            System.out.printf("I am %d with i = %d%n", Thread.currentThread().threadId(), i);
            int time = 0;
            while (time++ < 5000) {
                ArrayList<Integer> list = new ArrayList<>();
                for (int j = 0; j < time; ++j) {
                    list.add(j);
                }
                int res = 0;
                for (int j = 0; j < time; ++j) {
                    for (int k = 0; k < time; ++k) {
                        res += list.get(j) * list.get(k);
                    }
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static void main(String[] args) {
        BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
        for (int i = 0; i < 1_000_000; ++i) {
            tasks.add(parametrizedCallable(i));
        }
        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            while (!tasks.isEmpty()) {
                executorService.submit(tasks.poll());
            }
        }
    }
}
