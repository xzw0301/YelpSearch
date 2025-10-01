package edu.scu.xzw;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

public class AttributeTest {
    
    @Test
    public void testFlatten() throws Exception {
        String json = "{\"business_id\":\"123\", \"attributes\":{\"k1\":{\"k2\":\"v2\"}}}";
        ObjectMapper objectMapper = new ObjectMapper();

        List<Attribute> attributes = Attribute.from(objectMapper.readTree(json));
        System.out.println(attributes);

    }
}
