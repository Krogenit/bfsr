package net.bfsr.server.core;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext context;

    public static <T extends Object> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static void stop() {
        SpringApplication.exit(context);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        setContext(context);
    }

    private static synchronized void setContext(ApplicationContext context) {
        SpringContext.context = context;
    }

    public static boolean isAvailable() {
        return context != null;
    }

    public static void clear() {
        context = null;
    }
}