package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.model.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class ToolDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Tool save(Tool tool) {
        String insertToolSql = "INSERT INTO tools (owner_name, owner_id, name, category, tool_condition, price_per_day, max_renting_period, pickup_location, description, additional_info) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertToolSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tool.getOwnerName());
            ps.setString(2, tool.getOwnerId());
            ps.setString(3, tool.getName());
            ps.setString(4, tool.getCategory());
            ps.setString(5, tool.getCondition());
            ps.setDouble(6, tool.getPricePerDay());
            ps.setInt(7, tool.getMaxRentingPeriod());
            ps.setString(8, tool.getPickupLocation());
            ps.setString(9, tool.getDescription());
            ps.setString(10, tool.getAdditionalInfo());
            return ps;
        }, keyHolder);

        long generatedToolId = keyHolder.getKey().longValue();
        tool.setId(generatedToolId);

        String insertImageSql = "INSERT INTO tool_images (tool_id, image_url) VALUES (?, ?)";
        if (tool.getImageUrls() != null) {
            for (String url : tool.getImageUrls()) {
                jdbcTemplate.update(insertImageSql, generatedToolId, url);
            }
        }

        return tool;
    }

    public List<Tool> findRecent() {
        String selectToolsSql = "SELECT * FROM tools WHERE COALESCE(is_active, TRUE) = TRUE ORDER BY id DESC LIMIT 12";
        return jdbcTemplate.query(selectToolsSql, (rs, rowNum) -> mapTool(rs));
    }

    public List<Tool> searchActive(String query) {
        String like = "%" + (query == null ? "" : query.trim().toLowerCase()) + "%";
        String sql = """
                SELECT * FROM tools
                WHERE COALESCE(is_active, TRUE) = TRUE
                  AND (LOWER(name) LIKE ? OR LOWER(category) LIKE ?)
                ORDER BY id DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapTool(rs), like, like);
    }

    /**
     * Finds a single tool by its unique ID, alongside all associated images.
     */
    public Tool findById(Long id) {
        String selectToolSql = "SELECT * FROM tools WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(selectToolSql, (rs, rowNum) -> {
                return mapTool(rs);
            }, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Tool> findByOwnerId(String ownerId) {
        String selectToolsSql = "SELECT * FROM tools WHERE owner_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(selectToolsSql, (rs, rowNum) -> {
            return mapTool(rs);
        }, ownerId);
    }

    public int update(Tool tool) {
        String sql = """
                UPDATE tools
                SET name = ?,
                    category = ?,
                    tool_condition = ?,
                    price_per_day = ?,
                    max_renting_period = ?,
                    pickup_location = ?,
                    description = ?,
                    additional_info = ?
                WHERE id = ? AND owner_id = ?
                """;
        int updated = jdbcTemplate.update(sql,
                tool.getName(),
                tool.getCategory(),
                tool.getCondition(),
                tool.getPricePerDay(),
                tool.getMaxRentingPeriod(),
                tool.getPickupLocation(),
                tool.getDescription(),
                tool.getAdditionalInfo(),
                tool.getId(),
                tool.getOwnerId()
        );
        if (updated == 1 && tool.getImageUrls() != null) {
            jdbcTemplate.update("DELETE FROM tool_images WHERE tool_id = ?", tool.getId());
            for (String url : tool.getImageUrls()) {
                jdbcTemplate.update("INSERT INTO tool_images (tool_id, image_url) VALUES (?, ?)", tool.getId(), url);
            }
        }
        return updated;
    }

    public int unlist(Long toolId, String ownerId) {
        String sql = "UPDATE tools SET is_active = FALSE WHERE id = ? AND owner_id = ?";
        return jdbcTemplate.update(sql, toolId, ownerId);
    }

    public boolean existsByExactName(String name) {
        String sql = "SELECT COUNT(*) FROM tools WHERE BINARY name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);
        return count != null && count > 0;
    }

    private Tool mapTool(ResultSet rs) throws SQLException {
        Tool tool = new Tool();
        tool.setId(rs.getLong("id"));
        tool.setOwnerName(rs.getString("owner_name"));
        tool.setOwnerId(rs.getString("owner_id"));
        tool.setName(rs.getString("name"));
        tool.setCategory(rs.getString("category"));
        tool.setCondition(rs.getString("tool_condition"));
        tool.setPricePerDay(rs.getDouble("price_per_day"));
        tool.setMaxRentingPeriod(rs.getInt("max_renting_period"));
        tool.setPickupLocation(rs.getString("pickup_location"));
        tool.setDescription(rs.getString("description"));
        tool.setAdditionalInfo(rs.getString("additional_info"));
        try {
            boolean active = rs.getBoolean("is_active");
            tool.setActive(rs.wasNull() || active);
        } catch (SQLException ignored) {
            tool.setActive(true);
        }
        List<String> imageUrls = jdbcTemplate.query("SELECT image_url FROM tool_images WHERE tool_id = ?",
                (imgRs, imgRowNum) -> imgRs.getString("image_url"),
                tool.getId()
        );
        tool.setImageUrls(imageUrls);
        return tool;
    }
}
