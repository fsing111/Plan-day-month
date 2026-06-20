package com.plansystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plansystem.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User Mapper interface.
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
