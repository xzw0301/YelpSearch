package edu.scu.xzw;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Attribute {
    private String id;
    private String businessId;
    private String attributeName;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static List<Attribute> from(JsonNode business) {
        
        String businessId = business.get("business_id").asText();
        JsonNode attributes = business.get("attributes");
        try {
            Map<String, Object> flattenedJsonMap = JsonFlattener.flattenAsMap(OBJECT_MAPPER.writeValueAsString(attributes));
            List<Attribute> result = new ArrayList<>();
            for (String key : flattenedJsonMap.keySet()) {
                String value = flattenedJsonMap.get(key).toString();
                String id = UUID.randomUUID().toString();
                result.add(new Attribute(id, businessId, String.format("%s-%s", key, value)));
            }
            return result;
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        
    }

    public void addInsertStatement(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, id);
        preparedStatement.setString(2, businessId);
        preparedStatement.setString(3, attributeName);
        preparedStatement.addBatch();
    }
}
