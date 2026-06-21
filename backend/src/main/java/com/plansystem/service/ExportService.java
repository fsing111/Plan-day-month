package com.plansystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plansystem.entity.Achievement;
import com.plansystem.entity.Plan;
import com.plansystem.mapper.AchievementMapper;
import com.plansystem.mapper.PlanMapper;
import com.plansystem.utils.DateUtils;
import com.plansystem.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Excel export service using Apache POI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final PlanMapper planMapper;
    private final AchievementMapper achievementMapper;
    private final UserService userService;

    public byte[] exportPlans(String planType, String status, String startDate, String endDate) {
        Long userId = UserContext.getUserId();
        List<Long> visibleUserIds = userService.getVisibleUserIds(userId, UserContext.getRole());

        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        if (visibleUserIds != null) wrapper.in(Plan::getUserId, visibleUserIds);
        if (planType != null) wrapper.eq(Plan::getPlanType, planType);
        if (status != null) wrapper.eq(Plan::getStatus, status);
        if (startDate != null) wrapper.ge(Plan::getStartTime, startDate + " 00:00:00");
        if (endDate != null) wrapper.le(Plan::getStartTime, endDate + " 23:59:59");
        wrapper.orderByDesc(Plan::getCreatedAt);

        List<Plan> plans = planMapper.selectList(wrapper);
        var userNameMap = userService.getUserNameMap(
                plans.stream().map(Plan::getUserId).distinct().toList());

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("计划列表");
            Row header = sheet.createRow(0);
            String[] columns = {"标题", "类型", "优先级", "状态", "开始时间", "截止时间", "提交人", "量化指标"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Plan p : plans) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getTitle());
                row.createCell(1).setCellValue(p.getPlanType());
                row.createCell(2).setCellValue(p.getPriority());
                row.createCell(3).setCellValue(p.getStatus());
                row.createCell(4).setCellValue(DateUtils.format(p.getStartTime()));
                row.createCell(5).setCellValue(DateUtils.format(p.getEndTime()));
                row.createCell(6).setCellValue(userNameMap.getOrDefault(p.getUserId(), "未知"));
                row.createCell(7).setCellValue(p.getQuantTarget() != null ? p.getQuantTarget() : "");
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Excel export failed", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    public byte[] exportAchievements(String status) {
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Achievement::getStatus, status);
        wrapper.orderByDesc(Achievement::getCreatedAt);

        List<Achievement> achievements = achievementMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("成果列表");
            Row header = sheet.createRow(0);
            String[] columns = {"完成说明", "实际数量", "实际耗时", "状态", "遇到问题", "提交时间"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Achievement a : achievements) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getDescription() != null ? a.getDescription() : "");
                row.createCell(1).setCellValue(a.getActualQty() != null ? a.getActualQty() : "");
                row.createCell(2).setCellValue(a.getActualHours() != null ? a.getActualHours().toString() : "");
                row.createCell(3).setCellValue(a.getStatus());
                row.createCell(4).setCellValue(a.getIssues() != null ? a.getIssues() : "");
                row.createCell(5).setCellValue(DateUtils.format(a.getSubmittedAt()));
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Excel export failed", e);
            throw new RuntimeException("导出失败", e);
        }
    }
}
