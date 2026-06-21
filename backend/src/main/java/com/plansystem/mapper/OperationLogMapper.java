package com.plansystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plansystem.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
