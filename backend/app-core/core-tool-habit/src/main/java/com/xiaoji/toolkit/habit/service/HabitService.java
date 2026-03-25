package com.xiaoji.toolkit.habit.service;

import com.xiaoji.toolkit.habit.model.HabitCheckinRecord;
import com.xiaoji.toolkit.habit.model.HabitItem;
import com.xiaoji.toolkit.habit.model.HabitStats;

import java.util.List;

public interface HabitService {
    HabitItem createHabitItem(String name, boolean enabled);

    List<HabitItem> listHabitItems();

    HabitCheckinRecord checkin(Long habitItemId, Long userId, String checkinDate);

    List<HabitCheckinRecord> listCheckinRecords();

    HabitStats stats();
}
