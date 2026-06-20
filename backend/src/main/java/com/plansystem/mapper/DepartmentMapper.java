package com.plansystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plansystem.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * Department mapper.
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
