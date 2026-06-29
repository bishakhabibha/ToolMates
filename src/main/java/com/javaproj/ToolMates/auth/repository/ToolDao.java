package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.model.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
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
        String selectToolsSql = "SELECT * FROM tools ORDER BY id DESC LIMIT 12";

        return jdbcTemplate.query(selectToolsSql, (rs, rowNum) -> {
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

            // Pure JDBC subquery to pull matching images for this row
            String selectImagesSql = "SELECT image_url FROM tool_images WHERE tool_id = ?";
            List<String> imageUrls = jdbcTemplate.query(selectImagesSql,
                    (imgRs, imgRowNum) -> imgRs.getString("image_url"),
                    tool.getId()
            );

            tool.setImageUrls(imageUrls);

            return tool;
        });
    }

    /**
     * Finds a single tool by its unique ID, alongside all associated images.
     */
    public Tool findById(Long id) {
        String selectToolSql = "SELECT * FROM tools WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(selectToolSql, (rs, rowNum) -> {
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

                String selectImagesSql = "SELECT image_url FROM tool_images WHERE tool_id = ?";
                List<String> imageUrls = jdbcTemplate.query(selectImagesSql,
                        (imgRs, imgRowNum) -> imgRs.getString("image_url"),
                        tool.getId()
                );
                tool.setImageUrls(imageUrls);

                return tool;
            }, id);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Tool> findByOwnerId(String ownerId) {
        String selectToolsSql = "SELECT * FROM tools WHERE owner_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(selectToolsSql, (rs, rowNum) -> {
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

            String selectImagesSql = "SELECT image_url FROM tool_images WHERE tool_id = ?";
            List<String> imageUrls = jdbcTemplate.query(selectImagesSql,
                    (imgRs, imgRowNum) -> imgRs.getString("image_url"),
                    tool.getId()
            );
            tool.setImageUrls(imageUrls);
            return tool;
        }, ownerId);
    }
}
