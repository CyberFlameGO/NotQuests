package notquests.notquests.Managers;

import notquests.notquests.NotQuests;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

public class PerformanceManager {
    private final NotQuests main;

    private final boolean accurateTPS = true;

    //Inaccurate way of getting TPS through calculating tick times every time a task runs
    private final float tps = 20;
    //Accurate way of getting the TPS through reflection
    private final String name = Bukkit.getServer().getClass().getPackage().getName();
    private final DecimalFormat format = new DecimalFormat("##.##");
    private long msPerTick = 50;
    private long lastMS = 0;
    private float tickCounter = 0;
    private int msCounter = 0;
    private Object serverInstance;
    private Field tpsField;


    public PerformanceManager(final NotQuests main) {
        this.main = main;

        if (accurateTPS) {
            try {
                serverInstance = getNMSClass("MinecraftServer").getMethod("getServer").invoke(null);
                tpsField = serverInstance.getClass().getField("recentTps");
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            startMonitoringInaccurateTPS();
        }

    }


    //value of 0 will get the tps for the last minute, value of 1 will be 5min and 2 would be 15min
    public final String getTPSString(int time) {
        try {
            double[] tps = ((double[]) tpsField.get(serverInstance));
            return format.format(tps[time]);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //value of 0 will get the tps for the last minute, value of 1 will be 5min and 2 would be 15min
    public final double getTPSDouble(int time) {
        try {
            double[] tps = ((double[]) tpsField.get(serverInstance));
            return tps[time];
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public final double getTPS() {
        return getTPSDouble(0);
    }


    private Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + className);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    //Inaccurate
    public void startMonitoringInaccurateTPS() {
        main.getServer().getScheduler().scheduleSyncRepeatingTask(main, () -> {

            long timeNow = System.currentTimeMillis();
            if (lastMS != 0) {
                msPerTick = (timeNow - lastMS);

                msCounter += msPerTick;
                tickCounter++;

                if (msCounter >= 1000) {
                    int aboveASecond = msCounter - 1000;

                    tickCounter = tickCounter - (aboveASecond / tickCounter);

                    //System.out.println("TPS: " + tickCounter);

                    tickCounter = 0;
                    msCounter = 0;
                }
            }

            lastMS = timeNow;
        }, 1, 1);
    }


}