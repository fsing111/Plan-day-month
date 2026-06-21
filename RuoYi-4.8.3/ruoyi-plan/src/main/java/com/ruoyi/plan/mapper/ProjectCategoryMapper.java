package com.ruoyi.plan.mapper;

import java.util.List;

import com.ruoyi.plan.domain.ProjectCategory;

/**
 * 项目分类Mapper
 */
public interface ProjectCategoryMapper {

    List<ProjectCategory> selectProjectCategoryList(ProjectCategory category);

    ProjectCategory selectProjectCategoryById(Long id);

    int insertProjectCategory(ProjectCategory category);

    int updateProjectCategory(ProjectCategory category);

    int deleteProjectCategoryById(Long id);
}
