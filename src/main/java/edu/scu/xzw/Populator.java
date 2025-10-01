package edu.scu.xzw;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

interface Populator {
    void populate(List<JsonNode> nodes) throws SQLException;
}
