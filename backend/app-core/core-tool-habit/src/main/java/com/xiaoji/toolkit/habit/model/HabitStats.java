package com.xiaoji.toolkit.habit.model;

public class HabitStats {
    private final int itemCount;
    private final int totalCheckinCount;
    private final int todayCheckinCount;

    public HabitStats(int itemCount, int totalCheckinCount, int todayCheckinCount) {
        this.itemCount = itemCount;
        this.totalCheckinCount = totalCheckinCount;
        this.todayCheckinCount = todayCheckinCount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getTotalCheckinCount() {
        return totalCheckinCount;
    }

    public int getTodayCheckinCount() {
        return todayCheckinCount;
    }
}
