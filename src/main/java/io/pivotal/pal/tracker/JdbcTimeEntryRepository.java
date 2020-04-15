package io.pivotal.pal.tracker;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class JdbcTimeEntryRepository implements TimeEntryRepository {
    private JdbcTemplate jdbcTemplate;

    private interface Sql {
        String insert = "insert into time_entries (project_id, user_id, date, hours) " +
                "values (?, ?, ?, ?)";
        String findById = "select id, project_id, user_id, date, hours from time_entries " +
                "where id = ?";
        String selectAll = "SELECT id, project_id, user_id, date, hours FROM time_entries";
        String update = "update time_entries set project_id=?, user_id=?, date=?, hours=? " +
                "where id = ?";
        String delete = "delete from time_entries where id=?";
    }

    private static class TimeEntryRowMapper implements RowMapper<TimeEntry> {
        @Override
        public TimeEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            TimeEntry timeEntry = new TimeEntry();
            timeEntry.setId(rs.getLong("id") );
            timeEntry.setProjectId(rs.getLong("project_id"));
            timeEntry.setUserId( rs.getLong("user_id") );
            timeEntry.setDate(rs.getDate("date").toLocalDate());
            timeEntry.setHours(rs.getInt("hours"));
            return timeEntry;
        }
    }

    private static final TimeEntryRowMapper TIME_ENTRY_ROW_MAPPER = new TimeEntryRowMapper();

    public JdbcTimeEntryRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator psc = con -> {
            PreparedStatement statement = con.prepareStatement(Sql.insert, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, timeEntry.getProjectId());
            statement.setLong(2, timeEntry.getUserId());
            statement.setDate(3, Date.valueOf( timeEntry.getDate()));
            statement.setInt(4, timeEntry.getHours());
            return statement;
        };
        jdbcTemplate.update(psc, keyHolder);

        long id = keyHolder.getKey().longValue();
        return find(id);
    }

    @Override
    public TimeEntry find(long id) {
        Object[] args = {id};
        List<TimeEntry> entries = jdbcTemplate.query(Sql.findById, args, TIME_ENTRY_ROW_MAPPER);
        if (entries.isEmpty()) {
            return null;
        }

        return entries.get(0);
    }

    @Override
    public List<TimeEntry> list() {
        return jdbcTemplate.query(Sql.selectAll, TIME_ENTRY_ROW_MAPPER);
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        jdbcTemplate.update(Sql.update, timeEntry.getProjectId(), timeEntry.getUserId(),
                timeEntry.getDate(), timeEntry.getHours(), id);
        return find(id);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(Sql.delete, id);
    }

}
