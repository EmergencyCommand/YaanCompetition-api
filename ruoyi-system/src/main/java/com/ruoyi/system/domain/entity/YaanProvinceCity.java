package com.ruoyi.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;

/**
    * 全省县区经纬度
    */
@Data
@TableName(value = "yaan_province_city")
public class YaanProvinceCity {
    @TableField(value = "province_city_id")
    private String provinceCityId;

    @TableField(value = "province_city_name")
    private String provinceCityName;

    @TableField(value = "full_province_city_name")
    private String fullProvinceCityName;

    @TableField(value = "geom")
    private Geometry geom;
}
