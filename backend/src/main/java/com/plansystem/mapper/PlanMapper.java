package com.plansystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plansystem.entity.Plan;
import org.apache.ibatis.annotations.Delete;
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
            + "AND deleted_at IS NULL "
            + "ORDER BY start_time ASC")
    List<Plan> findForCalendar(@Param("userIds") String userIds,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate);

    /**
     * Physical delete (bypasses @TableLogic).
     */
    @Delete("DELETE FROM plan WHERE id = #{id}")
    void physicalDelete(@Param("id") Long id);
}
