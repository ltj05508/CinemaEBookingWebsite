package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database functions for promotion management.
 * Handles creating and retrieving promotions.
 */
public class PromotionDBFunctions {

    /**
     * Create a new promotion.
     * 
     * @param promoCode Unique promotion code (e.g., "SAVE20")
     * @param discountPercent Discount percentage (1-100)
     * @param startDate Start date of promotion (YYYY-MM-DD)
     * @param endDate End date of promotion (YYYY-MM-DD)
     * @return The generated promotionId, or -1 if insert failed
     */
    public static int createPromotion(String promoCode, int discountPercent, 
                                     String startDate, String endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int promotionId = -1;

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "INSERT INTO Promotions (promo_code, discount_percent, start_date, end_date) " +
                        "VALUES (?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, promoCode);
            pstmt.setInt(2, discountPercent);
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    promotionId = rs.getInt(1);
                }
            }
            
            System.out.println("Promotion created successfully with ID: " + promotionId);
            
        } catch (SQLException e) {
            System.err.println("Error creating promotion: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return promotionId;
    }

    /**
     * Get all promotions.
     * 
     * @return List of promotion maps with details
     */
    public static List<Map<String, Object>> getAllPromotions() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> promotions = new ArrayList<>();

        try {
            conn = ConnectToDatabase.getConnection();
            stmt = conn.createStatement();
            
            String sql = "SELECT promotion_id, promo_code, discount_percent, start_date, end_date " +
                        "FROM Promotions ORDER BY start_date DESC";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> promotion = new HashMap<>();
                promotion.put("promotionId", rs.getInt("promotion_id"));
                promotion.put("promoCode", rs.getString("promo_code"));
                promotion.put("discountPercent", rs.getInt("discount_percent"));
                promotion.put("startDate", rs.getString("start_date"));
                promotion.put("endDate", rs.getString("end_date"));
                
                promotions.add(promotion);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting promotions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return promotions;
    }

    /**
     * Get a promotion by its code.
     * 
     * @param promoCode The promotion code to lookup
     * @return Promotion map with details, or null if not found
     */
    public static Map<String, Object> getPromotionByCode(String promoCode) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Object> promotion = null;

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "SELECT promotion_id, promo_code, discount_percent, start_date, end_date " +
                        "FROM Promotions WHERE promo_code = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, promoCode);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                promotion = new HashMap<>();
                promotion.put("promotionId", rs.getInt("promotion_id"));
                promotion.put("promoCode", rs.getString("promo_code"));
                promotion.put("discountPercent", rs.getInt("discount_percent"));
                promotion.put("startDate", rs.getString("start_date"));
                promotion.put("endDate", rs.getString("end_date"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting promotion by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return promotion;
    }

    /**
     * Validate if a promotion code is currently active.
     * 
     * @param promoCode The promotion code to validate
     * @return true if promotion exists and is currently active, false otherwise
     */
    public static boolean isPromotionActive(String promoCode) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isActive = false;

        try {
            conn = ConnectToDatabase.getConnection();
            
            String sql = "SELECT COUNT(*) as count FROM Promotions " +
                        "WHERE promo_code = ? AND CURDATE() BETWEEN start_date AND end_date";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, promoCode);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                isActive = rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error validating promotion: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return isActive;
    }
}
