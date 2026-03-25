package com.xiaoji.toolkit.habit.service;

import com.xiaoji.toolkit.habit.model.HabitCheckinRecord;
import com.xiaoji.toolkit.habit.model.HabitItem;
import com.xiaoji.toolkit.habit.model.HabitStats;
import com.xiaoji.toolkit.shared.constants.ResultCode;
import com.xiaoji.toolkit.shared.exception.BizException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryHabitService implements HabitService {

    private final AtomicLong itemIdGen = new AtomicLong(8000L);
    private final AtomicLong recordIdGen = new AtomicLong(9000L);

    private final ConcurrentHashMap<Long, HabitItem> itemStore = new ConcurrentHashMap<Long, HabitItem>();
    private final CopyOnWriteArrayList<HabitCheckinRecord> recordStore = new CopyOnWriteArrayList<HabitCheckinRecord>();
    private final ConcurrentHashMap<String, Long> uniqueCheckinStore = new ConcurrentHashMap<String, Long>();

    @Override
    public HabitItem createHabitItem(String name, boolean enabled) {
        if (isBlank(name)) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "habit name is required");
        }

        long now = System.currentTimeMillis();
        Long id = itemIdGen.incrementAndGet();
        HabitItem item = new HabitItem(id, name.trim(), enabled, now, now);
        itemStore.put(id, item);
        return item;
    }

    @Override
    public List<HabitItem> listHabitItems() {
        return new ArrayList<HabitItem>(itemStore.values());
    }

    @Override
    public HabitCheckinRecord checkin(Long habitItemId, Long userId, String checkinDate) {
        if (habitItemId == null || userId == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "habitItemId and userId are required");
        }

        HabitItem item = itemStore.get(habitItemId);
        if (item == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "habit item not found: " + habitItemId);
        }
        if (!item.isEnabled()) {
            throw new BizException(ResultCode.BIZ_ERROR.getCode(), "habit item is disabled: " + habitItemId);
        }

        String date = isBlank(checkinDate) ? LocalDate.now().toString() : checkinDate.trim();
        String uniqueKey = habitItemId + "#" + userId + "#" + date;
        if (uniqueCheckinStore.containsKey(uniqueKey)) {
            throw new BizException(ResultCode.BIZ_ERROR.getCode(), "already checked in for this date");
        }

        long now = System.currentTimeMillis();
        Long id = recordIdGen.incrementAndGet();
        HabitCheckinRecord record = new HabitCheckinRecord(id, habitItemId, userId, date, now);
        recordStore.add(record);
        uniqueCheckinStore.put(uniqueKey, id);
        return record;
    }

    @Override
    public List<HabitCheckinRecord> listCheckinRecords() {
        return new ArrayList<HabitCheckinRecord>(recordStore);
    }

    @Override
    public HabitStats stats() {
        String today = LocalDate.now().toString();
        int todayCount = 0;
        for (HabitCheckinRecord record : recordStore) {
            if (today.equals(record.getCheckinDate())) {
                todayCount++;
            }
        }
        return new HabitStats(itemStore.size(), recordStore.size(), todayCount);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
