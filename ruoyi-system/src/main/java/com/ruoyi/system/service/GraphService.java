package com.ruoyi.system.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface GraphService {

    List<Map<String, Object>> getGraphData(List<Map<String, String>> keywordList);
}
