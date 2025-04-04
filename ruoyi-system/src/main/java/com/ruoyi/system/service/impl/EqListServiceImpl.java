package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.system.domain.bto.QueryParams;
import com.ruoyi.system.domain.dto.EqEventDTO;
import com.ruoyi.system.domain.dto.ResultEqListDTO;
import com.ruoyi.system.domain.entity.EarthquakeList;
import com.ruoyi.system.domain.entity.EqList;
import com.ruoyi.system.mapper.EqListMapper;
import com.ruoyi.system.service.IEqListService;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: xiaodemos
 * @date: 2024-11-26 15:36
 * @description: 实现类
 */

@Service
@Slf4j
public class EqListServiceImpl extends ServiceImpl<EqListMapper, EqList> implements IEqListService {

    @Resource
    private EqListMapper eqListMapper;

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


}
