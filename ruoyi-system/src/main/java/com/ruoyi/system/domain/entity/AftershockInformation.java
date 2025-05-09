package com.ruoyi.system.domain.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
    * 震后生成-地震震情信息-余震信息（用户上传数据）
    */
@Data
@TableName(value = "aftershock_information")
public class AftershockInformation {
    /**
     * 序号，自增主键
     */
    @TableId(value = "uuid", type = IdType.NONE)
    private String uuid;

    /**
     * 地震标识，标识地震事件的唯一标识符
     */
    @TableField(value = "earthquake_identifier")
    private String earthquakeIdentifier;

    /**
     * 地震名称，地震的描述性名称
     */
    @TableField(value = "earthquake_name") //对应数据库表名
    @ExcelProperty({"震情伤亡-震情灾情统计表", "地震名称"})//用于导入导出
    @ColumnWidth(30) //导出默认列宽30
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private String earthquakeName;

    /**
     * 地震时间，地震发生的具体时间
     */
    @TableField(value = "earthquake_time")
    @ExcelProperty(value = {"震情伤亡-震情灾情统计表", "地震时间"})
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private LocalDateTime earthquakeTime;

    /**
     * 震级，地震 Richter 震级
     */
    @ExcelProperty({"震情伤亡-震情灾情统计表", "震级"})
    @TableField(value = "magnitude")
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private String magnitude;

    /**
     * 震区，受影响的地区名称
     */
    @TableField(value = "affected_area")
    @ExcelProperty(value = {"震情伤亡-震情灾情统计表", "震区（县/区）"})
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private String affectedArea;


    /**
     * 填报截至时间，报告提交的最终期限
     */
    @TableField(value = "submission_deadline")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ExcelProperty(value = {"震情伤亡-震情灾情统计表", "统计截止时间"})
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private LocalDateTime submissionDeadline;


    /**
     * 余震次数累计，所有余震的次数总和
     */
    @TableField(value = "total_aftershocks")
    @ExcelProperty({"震情伤亡-震情灾情统计表", "余震次数累计"})
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private Integer totalAftershocks;

    /**
     * 3.0-3.9级余震次数，该级别范围内的余震次数
     */
    @TableField(value = "magnitude_3_3_9")
    @ExcelProperty({"震情伤亡-震情灾情统计表", "3.0-3.9级"})
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private Integer magnitude339;

    /**
     * 4.0-4.9级余震次数，该级别范围内的余震次数
     */
    @TableField(value = "magnitude_4_4_9")
    @ExcelProperty({"震情伤亡-震情灾情统计表", "4.0-4.9级"})
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private Integer magnitude449;

    /**
     * 5.0-5.9级余震次数，该级别范围内的余震次数
     */
    @TableField(value = "magnitude_5_5_9")
    @ExcelProperty({"震情伤亡-震情灾情统计表", "5.0-5.9级"})
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private Integer magnitude559;

    @TableField(value = "magnitude_6")
    @ExcelProperty({"震情伤亡-震情灾情统计表", "6.0级以上"})
    @ColumnWidth(30)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT) // 设置为左对齐
    private Integer magnitude6;

    /**
     * 系统插入时间，记录被系统创建的时间
     */
    @TableField(value = "system_insert_time")
    @ExcelIgnore
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime systemInsertTime;
    public LocalDateTime getSystemInsertTime() {
        return systemInsertTime != null ? systemInsertTime.truncatedTo(ChronoUnit.SECONDS) : null;
    }




    /**
     * 余震分布，描述余震的空间分布情况
     */
    @TableField(value = "aftershock_distribution")
    @ExcelIgnore
    private String aftershockDistribution;

    /**
     * 地震强度，余震的强度级别
     */
    @TableField(value = "earthquake_intensity")
    @ExcelIgnore
    private Double earthquakeIntensity;

    /**
     * 持续时间，余震持续的时间段
     */
    @TableField(value = "duration")
    @ExcelIgnore
    private String duration;

    /**
     * 影响范围，余震影响的地理范围面积
     */
    @TableField(value = "affected_range")
    @ExcelIgnore
    private String affectedRange;

    /**
     * 与主震的关系，描述余震与主震的关联性
     */
    @TableField(value = "relationship_with_mainshock")
    @ExcelIgnore
    private String relationshipWithMainshock;
}
