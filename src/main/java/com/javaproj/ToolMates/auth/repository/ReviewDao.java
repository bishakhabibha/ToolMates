package com.javaproj.ToolMates.auth.repository;

import com.javaproj.ToolMates.auth.dto.ReviewRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ReviewDao {

    @Autowired
    private JdbcTemplate jdbc;

    public void save(ReviewRequest request) {
        String sql = """
                INSERT INTO user_reviews (reviewer_id, reviewed_user_id, rating_stars, review_text)
                VALUES (?, ?, ?, ?)
                """;
        jdbc.update(sql,
                request.getReviewerId(),
                request.getReviewedUserId(),
                request.getRatingStars(),
                request.getReviewText());
    }

    public List<Map<String, Object>> findLeaderboard() {
        String sql = """
                SELECT
                    u.user_id AS userId,
                    CONCAT(u.first_name, ' ', u.last_name) AS name,
                    u.student_id AS studentId,
                    COALESCE(u.avatar_url, '') AS avatarUrl,
                    ROUND(AVG(r.rating_stars), 2) AS averageRating,
                    COUNT(r.review_id) AS reviewCount
                FROM users u
                JOIN user_reviews r ON r.reviewed_user_id = u.user_id
                GROUP BY u.user_id, u.first_name, u.last_name, u.student_id, u.avatar_url
                ORDER BY averageRating DESC, reviewCount DESC, name ASC
                """;
        return jdbc.queryForList(sql);
    }

    public Double findAverageForUser(Long userId) {
        String sql = "SELECT AVG(rating_stars) FROM user_reviews WHERE reviewed_user_id = ?";
        return jdbc.queryForObject(sql, Double.class, userId);
    }
}
