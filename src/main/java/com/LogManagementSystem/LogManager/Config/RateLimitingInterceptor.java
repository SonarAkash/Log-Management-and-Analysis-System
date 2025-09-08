package com.LogManagementSystem.LogManager.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 1000;
    private static final long TIME_WINDOW_MS = 60 * 1000; // Per minute

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();

        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());

        long currentTime = System.currentTimeMillis();

        if (currentTime - counter.getTimestamp() > TIME_WINDOW_MS) {
            counter.reset();
        }

        if (counter.getCount() >= MAX_REQUESTS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too Many Requests");
            return false; // This stops the request from reaching the controller
        }

        counter.increment();
        return true; // This allows the request to proceed
    }

    private static class RequestCounter {
        private int count;
        private long timestamp;

        public RequestCounter() {
            this.count = 0;
            this.timestamp = System.currentTimeMillis();
        }
        public void increment() { this.count++; }
        public void reset() {
            this.count = 1;
            this.timestamp = System.currentTimeMillis();
        }
        public int getCount() { return count; }
        public long getTimestamp() { return timestamp; }
    }
}
