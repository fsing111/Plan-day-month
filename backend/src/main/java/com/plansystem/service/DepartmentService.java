package com.plansystem.service;

import com.plansystem.entity.Department;
import com.plansystem.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Department service.
 */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;

    /**
     * Get all departments.
     */
    public List<Department> getAll() {
        return departmentMapper.selectList(null);
    }

    /**
     * Get department by ID.
     */
    public Department getById(Long id) {
        return departmentMapper.selectById(id);
    }

    /**
     * Get department name by ID.
     */
    public String getDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        Department dept = departmentMapper.selectById(deptId);
        return dept != null ? dept.getName() : null;
    }
}
