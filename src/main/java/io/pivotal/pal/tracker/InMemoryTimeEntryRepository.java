package io.pivotal.pal.tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTimeEntryRepository implements TimeEntryRepository {
    private Map<Long, TimeEntry> db = new HashMap<>();
    private long commonId = 0;

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        TimeEntry copy = timeEntry.copy();
        copy.setId(++commonId);
        db.put(commonId, copy);
        return copy;
    }

    @Override
    public TimeEntry find(long id) {
        return db.get(id);
    }

    @Override
    public List<TimeEntry> list() {
        return new ArrayList<>( db.values());
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        if (! db.containsKey(id)) {
            return null;
        }
        TimeEntry copy = timeEntry.copy();

        copy.setId(id);
        db.put(id, copy);
        return copy;
    }

    @Override
    public void delete(long id) {
        db.remove(id);
    }
}
