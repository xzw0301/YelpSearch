package edu.scu.xzw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.plaf.metal.MetalBorders.PaletteBorder;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Category {
    private String id;
    private String businessId;
    private String mainCategory;
    private String subCategory;

    public static final Set<String> MAIN_CATEGORIES = new HashSet<>();

    static {
        MAIN_CATEGORIES.add("Active Life");
        MAIN_CATEGORIES.add("Arts & Entertainment");
        MAIN_CATEGORIES.add("Automotive");
        MAIN_CATEGORIES.add("Car Rental");
        MAIN_CATEGORIES.add("Cafes");
        MAIN_CATEGORIES.add("Beauty & Spas");
        MAIN_CATEGORIES.add("Convenience Stores");
        MAIN_CATEGORIES.add("Dentists");
        MAIN_CATEGORIES.add("Doctors");
        MAIN_CATEGORIES.add("Drugstores");
        MAIN_CATEGORIES.add("Department Stores");
        MAIN_CATEGORIES.add("Education");
        MAIN_CATEGORIES.add("Event Planning & Services");
        MAIN_CATEGORIES.add("Flowers & Gifts");
        MAIN_CATEGORIES.add("Food");
        MAIN_CATEGORIES.add("Health & Medical");
        MAIN_CATEGORIES.add("Home Services");
        MAIN_CATEGORIES.add("Home & Garden");
        MAIN_CATEGORIES.add("Hospitals");
        MAIN_CATEGORIES.add("Hotels & Travel");
        MAIN_CATEGORIES.add("Hardware Stores");
        MAIN_CATEGORIES.add("Grocery");
        MAIN_CATEGORIES.add("Medical Centers");
        MAIN_CATEGORIES.add("Nurseries & Gardening");
        MAIN_CATEGORIES.add("Nightlife");
        MAIN_CATEGORIES.add("Restaurants");
        MAIN_CATEGORIES.add("Shopping");
        MAIN_CATEGORIES.add("Transportation");
    }

    public static List<Category> from(JsonNode business) {
        List<Category> categories = new ArrayList<>();
        String businessId = business.get("business_id").asText();
        List<String> mainCategories = new ArrayList<>();
        List<String> subCategories = new ArrayList<>();
        for(JsonNode c : business.get("categories")) {
            String category = c.asText();
            if (MAIN_CATEGORIES.contains(category)) {
                mainCategories.add(category);
            } else {
                subCategories.add(category);
            }
        }
        for (String mc : mainCategories) {
            for (String sc : subCategories) {
                String id = UUID.randomUUID().toString();
                categories.add(new Category(id, businessId, mc, sc));
            }
        }
        return categories;
    }

    public void addInsertStatement(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, id);
        preparedStatement.setString(2, businessId);
        preparedStatement.setString(3, mainCategory);
        preparedStatement.setString(4, subCategory);
        preparedStatement.addBatch();
    }

    public static String getPlaceholder(Set<String> set, String placeHolder) {
        List<String> placeholders = new ArrayList<>();
        set.forEach(c -> placeholders.add(placeHolder));
        return StringUtils.join(placeholders, ",");
    }

    public static void fillInPlaceHolder(PreparedStatement preparedStatement, Set<String> set, int idx) throws SQLException {
        for (String c : set) {
            preparedStatement.setString(idx++, c);
        }
    }
}
