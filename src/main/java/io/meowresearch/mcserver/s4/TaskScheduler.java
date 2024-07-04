package io.meowresearch.mcserver.s4;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaskScheduler {
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();

    public void tick(MinecraftServer server) {
        long currentTick = server.getTicks();
        Iterator<ScheduledTask> iterator = scheduledTasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            if (currentTick >= task.scheduledTick) {
                task.runnable.run();
                iterator.remove();
            }
        }
    }

    public void scheduleTask(long ticks, Runnable task, MinecraftServer server) {
        long scheduledTick = server.getTicks() + ticks;
        scheduledTasks.add(new ScheduledTask(scheduledTick, task));
    }

    private record ScheduledTask(long scheduledTick, Runnable runnable) {
    }
}
