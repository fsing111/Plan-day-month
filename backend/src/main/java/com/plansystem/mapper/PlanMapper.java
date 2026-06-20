package com.plansystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plansystem.entity.Plan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Plan mapper.
 */
@Mapper
public interface PlanMapper extends BaseMapper<Plan> {

    /**
     * Find plans for calendar view in a date range for given users.
     */
    @Select("SELECT * FROM plan WHERE user_id IN (${userIds}) "
            + "AND start_time >= #{startDate} AND start_time < #{endDate} "
            + "ORDER BY start_time ASC")
    List<Plan> findForCalendar(@Param("userIds") String userIds,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate);
}
