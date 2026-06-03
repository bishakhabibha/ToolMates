package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.model.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ToolDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Tool> toolRowMapper = new RowMapper<Tool>() {
        @Override
        public Tool mapRow(ResultSet rs, int rowNum) throws SQLException {
            Tool tool = new Tool();
            tool.setId(rs.getLong("id"));
            tool.setName(rs.getString("name"));
            tool.setCategory(rs.getString("category"));
            tool.setPricePerHr(rs.getDouble("price_per_hr"));
            tool.setImageUrl(rs.getString("image_url"));
            tool.setMaxRentingPeriod(rs.getInt("max_renting_period"));
            return tool;
        }
    };

    public List<Tool> getRecentTools() {
        String sql = "SELECT * FROM tools ORDER BY id DESC LIMIT 6";
        return jdbcTemplate.query(sql, toolRowMapper);
    }

    public List<Tool> searchToolsByName(String query) {
        String sql = "SELECT * FROM tools WHERE LOWER(name) LIKE LOWER(?)";
        String searchPattern = "%" + query.trim() + "%";
        return jdbcTemplate.query(sql, toolRowMapper, searchPattern);
    }
}