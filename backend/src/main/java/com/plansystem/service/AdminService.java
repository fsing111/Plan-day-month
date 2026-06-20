package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plansystem.dto.PageResult;
import com.plansystem.entity.*;
import com.plansystem.exception.BusinessException;
import com.plansystem.exception.ErrorCode;
import com.plansystem.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final ApprovalChainMapper chainMapper;
    private final PasswordEncoder passwordEncoder;
    private final com.plansystem.mapper.ProjectCategoryMapper categoryMapper;

    // User management
    public PageResult<User> getUserList(int page, int pageSize, String keyword) {
        IPage<User> p = new Page<>(page, Math.min(pageSize, 100));
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) w.like(User::getUsername, keyword).or().like(User::getRealName, keyword);
        w.orderByDesc(User::getCreatedAt);
        return PageResult.of(userMapper.selectPage(p, w));
    }

    @Transactional
    public User createUser(User user) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername())) > 0)
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public void updateUser(Long id, User user) {
        User existing = userMapper.selectById(id);
        if (existing == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        existing.setRealName(user.getRealName());
        existing.setRole(user.getRole());
        existing.setDeptId(user.getDeptId());
        existing.setLeaderId(user.getLeaderId());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        userMapper.updateById(existing);
    }

    @Transactional
    public void toggleUser(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        u.setEnabled(u.getEnabled() == 1 ? 0 : 1);
        userMapper.updateById(u);
    }

    @Transactional
    public void resetPassword(Long id) {
        User u = userMapper.selectById(id);
        u.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(u);
    }

    // Department management
    public List<Department> getDepartments() { return departmentMapper.selectList(null); }

    @Transactional
    public Department createDepartment(Department dept) {
        departmentMapper.insert(dept);
        return dept;
    }

    @Transactional
    public void updateDepartment(Long id, Department dept) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        existing.setName(dept.getName());
        existing.setParentId(dept.getParentId());
        departmentMapper.updateById(existing);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeptId, id)) > 0)
            throw new BusinessException(ErrorCode.DEPARTMENT_HAS_USERS);
        departmentMapper.deleteById(id);
    }

    // Approval chain management
    public List<ApprovalChain> getChains() { return chainMapper.selectList(null); }

    @Transactional
    public ApprovalChain createChain(ApprovalChain chain) {
        chainMapper.insert(chain);
        return chain;
    }

    @Transactional
    public void updateChain(Long id, ApprovalChain chain) {
        chain.setId(id);
        chainMapper.updateById(chain);
    }

    @Transactional
    public void deleteChain(Long id) { chainMapper.deleteById(id); }
}
