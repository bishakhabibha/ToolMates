package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbc;

    public boolean existsByStudentId(String studentId) {
        String sql = "SELECT COUNT(*) FROM users WHERE student_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, studentId);
        return count != null && count > 0;
    }

    public boolean existsByStudentEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE student_email = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public Optional<User> findByStudentEmail(String email) {
        String sql = "SELECT * FROM users WHERE student_email = ?";
        List<User> results = jdbc.query(sql, new UserRowMapper(), email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> findByUserId(Long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> results = jdbc.query(sql, new UserRowMapper(), userId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public User save(User user) {
        String sql = """
            INSERT INTO users
                (first_name, last_name, student_id, department, student_email,
                 phone, address, password_hash, email_verified, created_at,
                 total_tools_rented, total_tools_received)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,    user.getFirstName());
            ps.setString(2,    user.getLastName());
            ps.setString(3,    user.getStudentId());
            ps.setString(4,    user.getDepartment());
            ps.setString(5,    user.getStudentEmail());
            ps.setString(6,    user.getPhone());
            ps.setString(7,    user.getAddress());
            ps.setString(8,    user.getPasswordHash());
            ps.setBoolean(9,   user.isEmailVerified());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(11,      user.getTotalToolsRented());
            ps.setInt(12,      user.getTotalToolsReceived());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) user.setUserId(key.longValue());

        return user;
    }

    public void updatePassword(Long userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        jdbc.update(sql, newPasswordHash, userId);
    }

    public Optional<User> findByStudentId(String studentId) {
        String sql = "SELECT * FROM users WHERE student_id = ?";
        List<User> results = jdbc.query(sql, new UserRowMapper(), studentId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> findByEmail(String email) {
        return findByStudentEmail(email);
    }

    public boolean existsByEmail(String email) {
        return existsByStudentEmail(email);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User u = new User();
            u.setUserId(rs.getLong("user_id"));
            u.setFirstName(rs.getString("first_name"));
            u.setLastName(rs.getString("last_name"));
            u.setStudentId(rs.getString("student_id"));
            u.setDepartment(rs.getString("department"));
            u.setStudentEmail(rs.getString("student_email"));
            u.setPhone(rs.getString("phone"));
            u.setAddress(rs.getString("address"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setEmailVerified(rs.getBoolean("email_verified"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
            u.setTotalToolsRented(rs.getInt("total_tools_rented"));
            u.setTotalToolsReceived(rs.getInt("total_tools_received"));
            return u;
        }
    }
}
