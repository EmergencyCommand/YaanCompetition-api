package com.ruoyi.web.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.entity.OrthophotoImage;
import com.ruoyi.system.service.RemotesensingImageService;
import com.ruoyi.system.domain.entity.RemotesensingImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/remotesensing")
public class RemotesensingImageController {

    @Resource
    private RemotesensingImageService remotesensingImageService;

    //遥感影像---搜索
    @GetMapping("/queryRI")
    public AjaxResult queryRI(@RequestParam(value = "inputData", required = false) String inputData) {
        LambdaQueryWrapper<RemotesensingImage> wrapper = new LambdaQueryWrapper<>();
        if (inputData != null && !inputData.trim().isEmpty()) {
            wrapper
                    .like(RemotesensingImage::getName, inputData)
                    .or()
                    .like(RemotesensingImage::getPath, inputData)
                    .or()
                    .apply("TO_CHAR(create_time, 'YYYY-MM-DD HH24:MI:SS') LIKE {0}", "%" + inputData + "%")
                    .or()
                    .apply("TO_CHAR(shooting_time, 'YYYY-MM-DD HH24:MI:SS') LIKE {0}", "%" + inputData + "%");
        }
        List<RemotesensingImage> resultList = remotesensingImageService.list(wrapper);
        return AjaxResult.success(resultList);
    }

    //遥感影像---增
    @PostMapping("/addRI")
    public AjaxResult addRI(@RequestBody RemotesensingImage remotesensingImage){  //使 JSON 数据自动映射到 RemotesensingImage 对象的字段中
        System.out.println("从前端传过来的数据:"+remotesensingImage);
        try {
            remotesensingImage.generateUuidIfNotPresent();
            return AjaxResult.success(remotesensingImageService.save(remotesensingImage));
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
            return AjaxResult.error("保存失败: " + e.getMessage());
        }

    }

    //遥感影像---删
    @DeleteMapping("/removeRI")
    public AjaxResult removeRI(@RequestParam(value = "uuid") String uuid) {
        return AjaxResult.success(remotesensingImageService.removeById(uuid));
    }

    //遥感影像---改
    @PostMapping("/updaRI")
    public AjaxResult updaRI(@RequestBody RemotesensingImage remotesensingImage) {
        return AjaxResult.success(remotesensingImageService.updateById(remotesensingImage));
    }

    //遥感影像---查
    @PostMapping("/searchRI")
    public AjaxResult searchRI(){

        LambdaQueryWrapper<RemotesensingImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(RemotesensingImage::getName);
        return AjaxResult.success(remotesensingImageService.list(wrapper));
        
    }

    // 遥感影像---筛选
    @PostMapping("/filterRI")
    public AjaxResult filterRI(@RequestBody RemotesensingImage remotesensingImage) {
        LambdaQueryWrapper<RemotesensingImage> wrapper = new LambdaQueryWrapper<>();
        // 名称字段筛选
        if (remotesensingImage.getName() != null && !remotesensingImage.getName().trim().isEmpty()) {
            wrapper.like(RemotesensingImage::getName, remotesensingImage.getName());
        }

        // 路径字段筛选
        if (remotesensingImage.getPath() != null && !remotesensingImage.getPath().trim().isEmpty()) {
            wrapper.like(RemotesensingImage::getPath, remotesensingImage.getPath());
        }
        // 处理时间字段
        if (remotesensingImage.getCreateTime() != null) {
            // 将 LocalDateTime 视为 UTC 时间并转换为本地时区
            ZonedDateTime utcTime = remotesensingImage.getCreateTime().atZone(ZoneId.of("UTC"));
            ZonedDateTime localTime = utcTime.withZoneSameInstant(ZoneId.systemDefault());

            // 格式化为数据库中匹配的格式字符串
            String formattedTime = localTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 将格式化后的时间应用到查询中
            wrapper.apply("TO_CHAR(create_time, 'YYYY-MM-DD HH24:MI:SS') LIKE {0}", "%" + formattedTime + "%");
        }

        // 处理 `shootingTime` 字段  ge 表示大于或等于，用于范围查询，特别是处理日期或数值字段。
        if (remotesensingImage.getShootingTime() != null) {
            // 将 LocalDateTime 视为 UTC 时间并转换为本地时区
            ZonedDateTime utcTime = remotesensingImage.getShootingTime().atZone(ZoneId.of("UTC"));
            ZonedDateTime localTime = utcTime.withZoneSameInstant(ZoneId.systemDefault());

            // 格式化为数据库中匹配的格式字符串
            String formattedTime = localTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 将格式化后的时间应用到查询中
            wrapper.apply("TO_CHAR(shooting_time, 'YYYY-MM-DD HH24:MI:SS') LIKE {0}", "%" + formattedTime + "%");
        }

        List<RemotesensingImage> resultList = remotesensingImageService.list(wrapper);
        return AjaxResult.success(resultList);
    }

}






