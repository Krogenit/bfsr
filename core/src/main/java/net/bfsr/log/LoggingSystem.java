package net.bfsr.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.*;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class LoggingSystem {
    public static void initClient() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR);
        builder.setConfigurationName("BuilderTest");
        builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL).addAttribute("level", Level.INFO));

        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));
        builder.add(appenderBuilder);

        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n");
        ComponentBuilder triggeringPolicy = builder.newComponent("Policies").addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?")).addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100M"));
        appenderBuilder = builder.newAppender("rolling", "RollingFile").addAttribute("fileName", "client.log").addAttribute("filePattern", "log/client-%d{MM-dd-yy}.log.gz").add(layoutBuilder).addComponent(triggeringPolicy);
        builder.add(appenderBuilder);

        builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG).add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));

        builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("rolling")).add(builder.newAppenderRef("Stdout")));
        Configurator.initialize(builder.build());
    }

    public static void initServer() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR);
        builder.setConfigurationName("BuilderTest");
        builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL).addAttribute("level", Level.INFO));

        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable"));
        appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL).addAttribute("marker", "FLOW"));
        builder.add(appenderBuilder);

        LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%t] %-5level: %msg%n");
        ComponentBuilder triggeringPolicy = builder.newComponent("Policies").addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?")).addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100M"));
        appenderBuilder = builder.newAppender("rolling", "RollingFile").addAttribute("fileName", "server.log").addAttribute("filePattern", "log/server-%d{MM-dd-yy}.log.gz").add(layoutBuilder).addComponent(triggeringPolicy);
        builder.add(appenderBuilder);

        builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG).add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));

        builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("rolling")).add(builder.newAppenderRef("Stdout")));
        Configurator.initialize(builder.build());
    }
}
