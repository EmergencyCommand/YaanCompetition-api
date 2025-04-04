package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.entity.AftershockInformation;
import com.ruoyi.system.domain.entity.CasualtyReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AftershockInformationMapper extends BaseMapper<AftershockInformation> {
    /**
     * 获取最新的余震数据
     *
     * @param eqid
     * @return
     */
    @Select("SELECT yas.magnitude_3_3_9, yas.magnitude_4_4_9, yas.magnitude_5_5_9, yas.submission_deadline " +
            "FROM public.aftershock_information yas " +
            "WHERE yas.earthquake_identifier = #{eqid} " +
            "ORDER BY yas.submission_deadline DESC " +
            "LIMIT 1")
    Map<String, Object> getLatestAftershockData(String eqid);

//    @Select("SELECT yas.magnitude_3_3_9, yas.magnitude_4_4_9, yas.magnitude_5_5_9, yas.affected_area, yas.earthquake_time, yas.total_aftershocks  " +
//            "FROM public.aftershock_information yas " +
//            "WHERE yas.earthquake_identifier = #{eqid} " +
//            "ORDER BY yas.system_insert_time DESC ")
//    List<Map<String, Object>> getTotal(String eqid);


    @Select("SELECT cr.affected_area, " +
            "SUM(cr.magnitude_3_3_9) AS magnitude_3_3_9, " +
            "SUM(cr.magnitude_4_4_9) AS magnitude_4_4_9, " +
            "SUM(cr.magnitude_5_5_9) AS magnitude_5_5_9, " +
            "SUM(cr.magnitude_6) AS magnitude_6, " +
            "SUM(cr.total_aftershocks) AS total_aftershocks, " +
            "cr.system_insert_time, " +
            "cr.submission_deadline " +
            "FROM public.aftershock_information cr " +
            "JOIN ( " +
            "    SELECT affected_area, MAX(submission_deadline) AS latest_submission_deadline, " +
            "           MAX(system_insert_time) AS latest_system_insert_time " +
            "    FROM public.aftershock_information " +
            "    WHERE earthquake_identifier = #{eqid} " +
            "    GROUP BY affected_area " +
            ") sub ON cr.affected_area = sub.affected_area " +
            "AND cr.submission_deadline = sub.latest_submission_deadline " +
            "AND cr.system_insert_time = sub.latest_system_insert_time " +
            "WHERE cr.earthquake_identifier = #{eqid} " +
            "GROUP BY cr.affected_area, cr.system_insert_time, cr.submission_deadline")
    List<Map<String, Object>> getTotal(String eqid);


    @Select("SELECT yas.magnitude_3_3_9, yas.magnitude_4_4_9, yas.magnitude_5_5_9, yas.affected_area, yas.earthquake_time, yas.total_aftershocks " +
            "FROM public.aftershock_information yas " +
            "JOIN ( " +
            "    SELECT affected_area, MAX(submission_deadline) AS latest_submission_deadline " +
            "    FROM public.aftershock_information " +
            "    WHERE earthquake_identifier = #{eqid} " +
            "    GROUP BY affected_area " +
            ") sub ON yas.affected_area = sub.affected_area " +
            "AND yas.submission_deadline = sub.latest_submission_deadline " +
            "WHERE yas.earthquake_identifier = #{eqid} " +
            "ORDER BY yas.affected_area ")
    List<Map<String, Object>> getAfterShockInformation(String eqid);


    @Select("SELECT yas.* " +
            "FROM aftershock_information yas " +
            "JOIN LATERAL (" +
            "    SELECT affected_area, " +
            "           submission_deadline, " +
            "           system_insert_time, " +
            "           ROW_NUMBER() OVER (" +
            "               PARTITION BY affected_area " +
            "               ORDER BY " +
            "                   ABS(EXTRACT(EPOCH FROM (submission_deadline - #{time}::timestamp))) ASC, " +
            "                   ABS(EXTRACT(EPOCH FROM (system_insert_time - #{time}::timestamp))) ASC" +
            "           ) AS rn " +
            "    FROM aftershock_information " +
            "    WHERE earthquake_identifier = #{eqid} " +
            "    AND affected_area = yas.affected_area " +
            ") sub ON yas.affected_area = sub.affected_area " +
            "AND yas.submission_deadline = sub.submission_deadline " +
            "AND yas.system_insert_time = sub.system_insert_time " +
            "WHERE yas.earthquake_identifier = #{eqid} " +
            "AND sub.rn = 1 " +
            "ORDER BY yas.affected_area")

    List<Map<String, Object>> fromAftershock(@Param("eqid") String eqid, @Param("time") LocalDateTime time);
}