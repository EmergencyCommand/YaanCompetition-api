package com.ruoyi.web.controller.system;


import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/graph")
public class GraphController {

    private final Neo4jClient neo4jClient;

    public GraphController(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @GetMapping("/getGraph")
    public List getFullGraph() {
        String query = "MATCH (n)-[r]->(m) RETURN n, r, m";
        return new ArrayList<>(neo4jClient.query(query)
                .fetchAs(Map.class)
                .mappedBy((typeSystem, record) -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("source", record.get("n").asNode().asMap());
                    Relationship rel = record.get("r").asRelationship();
                    Map<String, Object> relMap = new HashMap<>(rel.asMap());
                    relMap.put("type", rel.type()); // 可选：添加关系类型
                    result.put("target", record.get("m").asNode().asMap());
                    result.put("value", relMap);
                    return result;
                })
                .all());
    }

    @GetMapping("/getGraphBy")
    public List getGraphBy(@RequestParam("eqid") String eqid) {
        // 查询特定 eqId 的最大知识图谱
        String query = "MATCH (n {eqid: '" +eqid+ "'})-[r]->(m) RETURN n, r, m";

        return new ArrayList<>(neo4jClient.query(query)
                .fetchAs(Map.class)
                .mappedBy((typeSystem, record) -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("source", record.get("n").asNode().asMap());
                    Relationship rel = record.get("r").asRelationship();
                    Map<String, Object> relMap = new HashMap<>(rel.asMap());
                    relMap.put("type", rel.type());
                    result.put("target", record.get("m").asNode().asMap());
                    result.put("value", relMap);
                    return result;
                })
                .all());

    }

}



