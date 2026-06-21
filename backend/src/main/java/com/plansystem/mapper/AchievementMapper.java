package com.plansystem.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plansystem.entity.Achievement;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface AchievementMapper extends BaseMapper<Achievement> {

    /**
     * Physical delete (bypasses @TableLogic).
     */
    @Delete("DELETE FROM achievement WHERE id = #{id}")
    void physicalDelete(@Param("id") Long id);
}
