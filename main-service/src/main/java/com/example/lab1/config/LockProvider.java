package com.example.lab1.config;

import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class LockProvider {
    private final ReentrantLock lock = new ReentrantLock();

    public ReentrantLock getReentLock() {
        return lock;
    }
}
