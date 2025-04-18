package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.common.exception.base.BaseException;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.domain.dto.*;
import com.ruoyi.system.domain.entity.EqList;
import com.ruoyi.system.domain.vo.ResultEqListVO;
import com.ruoyi.system.mapper.EqListMapper;
import com.ruoyi.system.service.IEqListService;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: xiaodemos
 * @date: 2024-11-26 15:36
 * @description: 实现类
 */

@Service
@Slf4j
public class EqListServiceImpl extends ServiceImpl<EqListMapper, EqList> implements IEqListService {

    private final Neo4jClient neo4jClient;

    public EqListServiceImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Resource
    private EqListMapper eqListMapper;

    @Resource
    private RestTemplate restTemplate;

    /**
     * @param event 地震事件编码
     * @author: xiaodemos
     * @date: 2024/12/10 9:47
     * @description: 对批次表的数据进行逻辑删除
     * @return: 返回删除的状态
     */
    public Boolean deletedEqListData(String event) {

        LambdaQueryWrapper<EqList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EqList::getEqid, event);

        EqList built = EqList.builder()
                .eqid(event)
                .isDeleted(1)
                .build();

        int flag = eqListMapper.update(built, wrapper);

        return flag > 0 ? true : false;
    }

    /**
     * @author: xiaodemos
     * @date: 2024/12/10 20:54
     * @description: 返回所有eqlist中的数据
     * @return: 返回所有eqlist中的数据
     */
    public List<ResultEqListDTO> eqEventGetList() {

        LambdaQueryWrapper<EqList> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EqList::getIsDeleted, 0);
        wrapper.orderByDesc(EqList::getOccurrenceTime);

        List<EqList> eqLists = eqListMapper.selectList(wrapper);

        List<ResultEqListDTO> dtos = new ArrayList<>(); //创建Dto对象

        for (EqList record : eqLists) {

            Geometry geom = record.getGeom();
            double longitude = geom.getCoordinate().x;
            double latitude = geom.getCoordinate().y;

            ResultEqListDTO dto = ResultEqListDTO.builder()
                    .longitude(longitude)
                    .latitude(latitude)
                    .depth(record.getDepth())
                    .eqAddrCode(record.getEqAddrCode())
                    .source(record.getSource())
                    .eqType(record.getEqType())
                    .occurrenceTime(String.valueOf(record.getOccurrenceTime()))
                    .pac(record.getPac())
                    .earthquakeFullName(record.getEarthquakeFullName())
                    .earthquakeName(record.getEarthquakeName())
                    .eqAddr(record.getEqAddr())
                    .eqid(record.getEqid())
                    .eqqueueId(record.getEqqueueId())
                    .intensity(record.getIntensity())
                    .magnitude(record.getMagnitude())
                    .townCode(record.getTownCode())
                    .type(record.getType())
                    .build();

            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * @param dto 查询参数
     * @author: xiaodemos
     * @date: 2024/12/12 17:28
     * @description: 根据事件编码查询地震的详情信息
     * @return:
     */
    public ResultEqListDTO eqEventGetDetailsInfo(EqEventDTO dto) {

        LambdaQueryWrapper<EqList> wrapper = new LambdaQueryWrapper();

        wrapper.ge(EqList::getMagnitude, 4); //大于四级的地震
        wrapper.eq(EqList::getIsDeleted, 0);
        wrapper.like(EqList::getEqid, dto.getEqid());
        wrapper.or().like(EqList::getEqqueueId, dto.getEqqueueId());

        EqList eq = eqListMapper.selectOne(wrapper);

        Geometry geom = eq.getGeom();
        double longitude = geom.getCoordinate().x;
        double latitude = geom.getCoordinate().y;

        ResultEqListDTO listDTO = ResultEqListDTO.builder().longitude(longitude)
                .longitude(longitude)
                .latitude(latitude)
                .build();

        BeanUtils.copyBeanProp(eq, listDTO);

        return listDTO;
    }

    /**
     * @param params 上传的参数
     * @author: xiaodemos
     * @date: 2024/12/15 17:47
     * @description: 修改地震
     */
    public void updateEqList(EqList params) {

        LambdaQueryWrapper<EqList> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(EqList::getEqid, params.getEqid());

        eqListMapper.update(params, wrapper);

    }

    //获取excel上传地震事件列表
    @Override
    public List<String> getExcelUploadEqList() {
        // 查询所有的 EqList 数据getData
        // 自定义日期时间格式化器，确保显示秒
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 创建 QueryWrapper 用于排序
        LambdaQueryWrapper<EqList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EqList::getIsDeleted, 0)
                .orderByDesc(EqList::getOccurrenceTime); // 按 OccurrenceTime 字段升序排序

        List<EqList> eqLists = eqListMapper.selectList(queryWrapper);

        // 拼接 position、time、magnitude 字段
        List<String> result = new ArrayList<>();

        for (EqList eq : eqLists) {
            String eqid = eq.getEqid().toString();
            String combined = eq.getOccurrenceTime().format(formatter).toString().replace("T", " ") + " " + eq.getEarthquakeName() + "  " + "震级：" + eq.getMagnitude();
            String resultString = eqid + " - " + combined; // 使用 "-" 或其他分隔符连接
            result.add(resultString);
        }
        return result;
    }

    /**
     * @author: xiaodemos
     * @date: 2025/1/14 7:18
     * @description: 查询eqlist表中最新一条的正式地震最新时间作为同步正式地震数据的参数
     * @return: 返回正式地震的最新时间
     */
    @Override
    public String findLastNomalEventTime() {
        LambdaQueryWrapper<EqList> wrapper = Wrappers.lambdaQuery(EqList.class);
        wrapper.eq(EqList::getEqType, "Z")
                .eq(EqList::getIsDeleted, 0)
                .orderByDesc(EqList::getOccurrenceTime)
                .last("LIMIT 1");  // 限制只查询一条数据;

        EqList eqList = eqListMapper.selectOne(wrapper);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String lasttime = eqList.getOccurrenceTime().format(formatter);

        return lasttime;
    }


    /**
     * @author: xiaodemos
     * @date: 2025/2/12 13:44
     * @description: 返回最新一条手动触发的地震数据
     * @return: 返回EqList对象
     */
    public EqList findRecentSeismicData() {
        LambdaQueryWrapper<EqList> wrapper = Wrappers.lambdaQuery(EqList.class);
        wrapper.eq(EqList::getIsDeleted, 0)
                // 需要加入一个手动触发的条件
                .eq(EqList::getSource, "2")
                .orderByDesc(EqList::getOccurrenceTime)
                .last("LIMIT 1");

        EqList eqList = eqListMapper.selectOne(wrapper);

        return eqList;

    }

    private static final String PYTHON_API_URL = "http://localhost:5000/process";  // Python 服务 URL

    public void addNewEq(EqEventTriggerDTO eqEventTriggerDTO) {


        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(eqEventTriggerDTO.getLongitude(), eqEventTriggerDTO.getLatitude()));

        EqList eqList = EqList.builder()
                .eqid(eqEventTriggerDTO.getEvent())
                .earthquakeName(eqEventTriggerDTO.getEqAddr())
                .geom(point)
                .intensity("")
                .magnitude(String.valueOf(eqEventTriggerDTO.getEqMagnitude()))
                .depth(eqEventTriggerDTO.getEqDepth().toString())
                .occurrenceTime(LocalDateTime.parse(eqEventTriggerDTO.getEqTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))     //这里是上传dto时保存的地震时间
                .eqType("Z")
                .source("2")
                .pac("")
                .type("")
                .isDeleted(0)
                .build();

//        eqListMapper.insert(eqList);


        String eqName = eqEventTriggerDTO.getEqAddr() + eqEventTriggerDTO.getEqMagnitude() + "级地震";
        String eqid = eqList.getEqid();
//
//        // 八大分类节点
//        List<String> categories = List.of(
//                "地震震情信息", "地震灾情信息", "应急指挥协调信息",
//                "应急决策信息", "应急处置信息", "态势标绘",
//                "灾害现场动态信息", "社会反应动态信息"
//        );
//
//        // 1. 创建 eqName 节点
//        String createEqNameNode = String.format(
//                "MERGE (eq:Event {name: '%s', eqid: '%s'})", eqName, eqid
//        );
//        neo4jClient.query(createEqNameNode).run();
//
//        // 2. 创建八大分类节点，并连接“震后生成”
//        for (String cat : categories) {
//            String createCatNode = String.format(
//                    "MERGE (c:Category {name: '%s', eqid: '%s'})", cat, eqid
//            );
//            neo4jClient.query(createCatNode).run();
//
//            String linkAfterToCat = String.format(
//                    "MATCH (eq:Event {name: '%s', eqid: '%s'}), (c:Category {name: '%s', eqid: '%s'}) " +
//                            "MERGE (eq)-[:包含 {eqid: '%s'}]->(c)", eqName, eqid, cat, eqid, eqid
//            );
//            neo4jClient.query(linkAfterToCat).run();
//        }
//
//        System.out.println("✅ 图谱构建完成：" + eqName);


        String result = pythonServiceClient.sendDataToPython(eqName, eqid);

        System.out.println(result);
    }

    @Async
    @Override
    @Transactional  // 确保两者操作在同一个事务中
    public void trigger(TriggerDTO triggerDTO) {

        // 抛出异常
        if (triggerDTO == null) {
            throw new BaseException("地震参数有误");
        }

        // 获取授权
        String authUrl = "http://localhost:8080/api/open/auth";
        // 设置请求头
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.set("Content-Type", "application/json");
        // 设置请求体
        JSONObject authBody = new JSONObject();
        authBody.put("username", "admin");
        authBody.put("password", "admin123");
        HttpEntity<JSONObject> authEntity = new HttpEntity<>(authBody, authHeaders);
        // 发送请求
        ResponseEntity<String> authResponse = restTemplate.exchange(authUrl, HttpMethod.POST, authEntity, String.class);

        // 抛出异常
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            throw new BaseException("获取灾损评估接口授权失败");
        }

        // 获取token
        String authResponseBody = authResponse.getBody();
        JSONObject data = JSONObject.parseObject(authResponseBody);
        String token = data.getString("token");

        // 请求 trigger 接口
        String triggerUrl = "http://localhost:8080/api/open/eq/trigger";
        HttpHeaders baseHeaders = new HttpHeaders();
        baseHeaders.set("Content-Type", "application/json");
        baseHeaders.set("Authorization", "Bearer " + token);

        // 设置请求体
        JSONObject triggerBody = new JSONObject();
        triggerBody.put("eqName", triggerDTO.getEqName());
        triggerBody.put("eqAddr",  triggerDTO.getEqAddr());
        triggerBody.put("eqTime", triggerDTO.getEqTime());
        triggerBody.put("longitude", triggerDTO.getLongitude());
        triggerBody.put("latitude", triggerDTO.getLatitude());
        triggerBody.put("eqDepth", triggerDTO.getEqDepth());
        triggerBody.put("magnitude", triggerDTO.getMagnitude());
        triggerBody.put("eqType", triggerDTO.getEqType());

        HttpEntity<JSONObject> triggerEntity = new HttpEntity<>(triggerBody, baseHeaders);
        // 发送请求
        ResponseEntity<String> triggerResponse = restTemplate.exchange(triggerUrl, HttpMethod.POST, triggerEntity, String.class);
        String triggerResponseBody = triggerResponse.getBody();
        log.info("触发地震：{}",triggerResponseBody);

        // 获取从库最新的一条数据
        String currentlyUrl = "http://localhost:8080/api/open/eq/currently";
        HttpEntity<JSONObject> currentlyEntity = new HttpEntity<>(baseHeaders);
        // 发送请求
        ResponseEntity<String> currentlyResponse = restTemplate.exchange(currentlyUrl, HttpMethod.GET, currentlyEntity, String.class);
        String currentlyResponseBody = currentlyResponse.getBody();
        // 解析json
        EqListResultDTO eqListResultDTO = parseJson(currentlyResponseBody, EqListResultDTO.class);
        ResultEqListVO eqListVO = eqListResultDTO.getData();
        log.info("最新地震：{}",eqListVO);

        // 同步数据到主库
        asyncMaster(eqListVO);
    }

    public void asyncMaster(ResultEqListVO eqListVO){

        try{

            GeometryFactory geometryFactory = new GeometryFactory();
            Point point = geometryFactory.createPoint(new Coordinate(eqListVO.getLongitude(), eqListVO.getLatitude()));

            EqList eqList = EqList.builder()
                    .eqid(eqListVO.getEqId())
                    .earthquakeName(eqListVO.getEqAddr())
                    .geom(point)
                    .intensity(eqListVO.getIntensity().toString())
                    .magnitude(String.valueOf(eqListVO.getMagnitude()))
                    .depth(eqListVO.getEqDepth().toString())
                    .occurrenceTime(LocalDateTime.parse(eqListVO.getEqTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .eqType(eqListVO.getEqType())
                    .source("2")
                    .pac("")
                    .type("")
                    .isDeleted(0)
                    .build();

            eqListMapper.insert(eqList);
            log.info("✅ 同步到主表成功");

            String eqName = eqListVO.getEqAddr() + eqListVO.getMagnitude() + "级地震";
            String eqid = eqList.getEqid();

            String result = pythonServiceClient.sendDataToPython(eqName, eqid);

            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("同步到主表失败");
            throw new BaseException("同步到主表失败");
        }
    }

    public static class pythonServiceClient {

        private static final String PYTHON_API_URL = "http://localhost:5000/process";  // Python 服务 URL

        public static String sendDataToPython(String eqName, String eqid) {
            // 构建请求体，发送数据到 Python 服务
            String requestData = String.format("{\"eqName\": \"%s\", \"eqid\": \"%s\"}", eqName, eqid);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);  // 设置内容类型为 JSON

            HttpEntity<String> entity = new HttpEntity<>(requestData, headers);  // 包装请求体和头部信息

            // 创建 RestTemplate 发送 HTTP 请求
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(PYTHON_API_URL, HttpMethod.POST, entity, String.class);

            // 获取并返回 Python 服务的响应
            return response.getBody();
        }
    }

    /**
     * 通用的 JSON 解析方法
     * @param jsonBody 需要反序列化的 JSON 字符串
     * @param clazz      目标类型的 Class 对象
     * @param <T>        目标类型的类型参数
     * @return 反序列化后的对象，如果解析失败则返回 null
     */
    public static  <T> T parseJson(String jsonBody, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonBody, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
