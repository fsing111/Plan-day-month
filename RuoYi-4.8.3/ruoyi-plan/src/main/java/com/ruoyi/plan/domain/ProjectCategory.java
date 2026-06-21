package com.ruoyi.plan.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目分类对象 project_category
 *
 * @author ruoyi
 */
public class ProjectCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long deptId;
    private Integer sortOrder;

    /** 非数据库字段：部门名称 */
    private String deptName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
}
