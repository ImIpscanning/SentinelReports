package com.imipscanning.sentinelreports.paper.platform;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class PaperSchedulerAdapter {
    private final Plugin plugin;

    public PaperSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    public void runAsync(Runnable runnable) {
        if (invokeScheduler("getAsyncScheduler", "runNow", new Class<?>[]{Plugin.class, Consumer.class}, new Object[]{plugin, ignored(runnable)})) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public void runGlobal(Runnable runnable) {
        if (invokeScheduler("getGlobalRegionScheduler", "run", new Class<?>[]{Plugin.class, Consumer.class}, new Object[]{plugin, ignored(runnable)})) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public void runEntity(Entity entity, Runnable runnable) {
        try {
            Method getScheduler = entity.getClass().getMethod("getScheduler");
            Object scheduler = getScheduler.invoke(entity);
            Method run = scheduler.getClass().getMethod("run", Plugin.class, Consumer.class, Runnable.class);
            run.invoke(scheduler, plugin, ignored(runnable), (Runnable) () -> runGlobal(runnable));
            return;
        } catch (ReflectiveOperationException ignored) {
            runGlobal(runnable);
        }
    }

    public void runRegion(Location location, Runnable runnable) {
        try {
            Object scheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            Method run = scheduler.getClass().getMethod("run", Plugin.class, Location.class, Consumer.class);
            run.invoke(scheduler, plugin, location, ignored(runnable));
            return;
        } catch (ReflectiveOperationException ignored) {
            runGlobal(runnable);
        }
    }

    public void runLater(Runnable runnable, long ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks);
    }

    public void runRepeating(Runnable runnable, long delayTicks, long periodTicks) {
        Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
    }

    private boolean invokeScheduler(String schedulerMethod, String runMethod, Class<?>[] parameterTypes, Object[] args) {
        try {
            Object scheduler = Bukkit.class.getMethod(schedulerMethod).invoke(null);
            Method method = scheduler.getClass().getMethod(runMethod, parameterTypes);
            method.invoke(scheduler, args);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private Consumer<Object> ignored(Runnable runnable) {
        return ignored -> runnable.run();
    }
}
