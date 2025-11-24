package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Database functions for promotion management.
 * Handles creating and retrieving promotions.
 */
public class PromotionDBFunctions {

    /**
     * Create a new promotion.
     * 
     * @param code Unique promotion code (e.g., "SAVE20")
     * @param description Description of the promotion
     * @param discountPercent Discount percentage (1-100)
     * @param validFrom Start date of promotion (YYYY-MM-DD)
     * @param validTo End date of promotion (YYYY-MM-DD)
     * @return The generated promoId (UUID), or null if insert failed
     */
    public static String createPromotion(String code, String description, double discountPercent, 
                                        String validFrom, String validTo) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        PreparedStatement pstmt = null;
        String promoId = UUID.randomUUID().toString();

        try {
            String sql = "INSERT INTO Promotions (promo_id, code, description, discount_percent, valid_from, valid_to) " +
                        "VALUES (?, ?, ?, ?, CAST(? AS DATE), CAST(? AS DATE))";
            
            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setString(1, promoId);
            pstmt.setString(2, code);
            pstmt.setString(3, description);
            pstmt.setDouble(4, discountPercent);
            pstmt.setString(5, validFrom);
            pstmt.setString(6, validTo);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Promotion created successfully with ID: " + promoId);
                return promoId;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating promotion: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    /**
     * Get all promotions.
     * 
     * @return List of promotion maps with details
     */
    public static List<Map<String, Object>> getAllPromotions() {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        Statement stmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> promotions = new ArrayList<>();

        try {
            stmt = dcs.getConn().createStatement();
            
            String sql = "SELECT promo_id, code, description, discount_percent, valid_from, valid_to " +
                        "FROM Promotions ORDER BY valid_from DESC";
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Map<String, Object> promotion = new HashMap<>();
                promotion.put("promoId", rs.getString("promo_id"));
                promotion.put("code", rs.getString("code"));
                promotion.put("description", rs.getString("description"));
                promotion.put("discountPercent", rs.getDouble("discount_percent"));
                promotion.put("validFrom", rs.getString("valid_from"));
                promotion.put("validTo", rs.getString("valid_to"));
                
                promotions.add(promotion);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting promotions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return promotions;
    }

    /**
     * Get a promotion by its code.
     * 
     * @param code The promotion code to lookup
     * @return Promotion map with details, or null if not found
     */
    public static Map<String, Object> getPromotionByCode(String code) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Object> promotion = null;

        try {
            String sql = "SELECT promo_id, code, description, discount_percent, valid_from, valid_to " +
                        "FROM Promotions WHERE code = ?";
            
            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setString(1, code);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                promotion = new HashMap<>();
                promotion.put("promoId", rs.getString("promo_id"));
                promotion.put("code", rs.getString("code"));
                promotion.put("description", rs.getString("description"));
                promotion.put("discountPercent", rs.getDouble("discount_percent"));
                promotion.put("validFrom", rs.getString("valid_from"));
                promotion.put("validTo", rs.getString("valid_to"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting promotion by code: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return promotion;
    }

    /**
     * Validate if a promotion code is currently active.
     * 
     * @param code The promotion code to validate
     * @return true if promotion exists and is currently active, false otherwise
     */
    public static boolean isPromotionActive(String code) {
        DatabaseConnectSingleton dcs = DatabaseConnectSingleton.getInstance();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isActive = false;

        try {
            String sql = "SELECT COUNT(*) as count FROM Promotions " +
                        "WHERE code = ? AND CURDATE() BETWEEN valid_from AND valid_to";
            
            pstmt = dcs.getConn().prepareStatement(sql);
            pstmt.setString(1, code);
            
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return isActive;
    }
}
