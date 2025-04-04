package com.ruoyi.system.domain.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author: xiaodemos
 * @date: 2024-11-23 18:00
 * @description: 获取地震灾情报告DTO
 */

@Data
@Builder
public class EqEventGetReportDTO {

    private String event;
    private String eqqueueId;

    @JsonCreator
    public EqEventGetReportDTO(@JsonProperty("event") String event, @JsonProperty("eqqueueId") String eqqueueId) {
        this.event = event;
        this.eqqueueId = eqqueueId;
    }

    public JSONObject toJSONObject() {
        // Create a JSONObject using FastJSON
        JSONObject jsonObject = new JSONObject();

        // Put values into the JSON object
        jsonObject.put("event", event);
        jsonObject.put("eqqueueId", eqqueueId);

        return jsonObject;
    }


}
