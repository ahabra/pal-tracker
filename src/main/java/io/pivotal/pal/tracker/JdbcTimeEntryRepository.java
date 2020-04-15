package io.pivotal.pal.tracker;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class JdbcTimeEntryRepository implements TimeEntryRepository {
    private JdbcTemplate jdbcTemplate;

     public JdbcTimeEntryRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

     @Override
    public TimeEntry find(long id) {
        List<TimeEntry> entries = read(Sql.findById, id);
        if (entries.isEmpty()) {
            return null;
        }

        return entries.get(0);
    }

    @Override
    public List<TimeEntry> list() {
        return read(Sql.selectAll);
    }

    private List<TimeEntry> read(String sql, Object... args) {
        return jdbcTemplate.query(sql, args, TIME_ENTRY_ROW_MAPPER);
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        PreparedStatementCreator psCreator = con -> buildInsertStatement(con, timeEntry);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(psCreator, keyHolder);

        //noinspection ConstantConditions
        return find(keyHolder.getKey().longValue());
    }

    private static PreparedStatement buildInsertStatement(Connection con, TimeEntry timeEntry) throws SQLException {
        PreparedStatement ps = con.prepareStatement(Sql.insert, RETURN_GENERATED_KEYS);
        ps.setLong(1, timeEntry.getProjectId());
        ps.setLong(2, timeEntry.getUserId());
        ps.setDate(3, Date.valueOf( timeEntry.getDate()));
        ps.setInt(4, timeEntry.getHours());
        return ps;
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

    private interface Sql {
        String insert = "insert into time_entries (project_id, user_id, date, hours) values (?, ?, ?, ?)";
        String findById = "select id, project_id, user_id, date, hours from time_entries where id = ?";
        String selectAll = "SELECT id, project_id, user_id, date, hours FROM time_entries";
        String update = "update time_entries set project_id=?, user_id=?, date=?, hours=? where id = ?";
        String delete = "delete from time_entries where id=?";
    }

    private static final RowMapper<TimeEntry> TIME_ENTRY_ROW_MAPPER = (rs, rowNum) -> {
        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setId(rs.getLong("id") );
        timeEntry.setProjectId(rs.getLong("project_id"));
        timeEntry.setUserId( rs.getLong("user_id") );
        timeEntry.setDate(rs.getDate("date").toLocalDate());
        timeEntry.setHours(rs.getInt("hours"));
        return timeEntry;
    };


}
