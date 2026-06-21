$(function() {
    var id = $.common.getQueryVariable("planId") || window.location.pathname.split('/').pop();
    if (id) loadPlanDetail(id);
});

function loadPlanDetail(id) {
    $.get("/plan/plan/detail/" + id, function(res) {
        if (res.code !== 0 || !res.data) return;
        var p = res.data;
        var typeMap = {DAILY:'<span class="label label-info">日报</span>', WEEKLY:'<span class="label label-primary">周报</span>', MONTHLY:'<span class="label label-success">月报</span>'};
        var priorityMap = {HIGH:'<span class="label label-danger">高</span>',MEDIUM:'<span class="label label-warning">中</span>',LOW:'<span class="label label-default">低</span>'};
        var statusMap = {DRAFT:'草稿',SUBMITTED:'已提交',APPROVING:'审批中',APPROVED:'已通过',REJECTED:'待修改',ARCHIVED:'已归档',OVERDUE:'已逾期'};
        $('#planTitle').text(p.title);
        $('#planType').html(typeMap[p.planType] || p.planType);
        $('#planPriority').html(priorityMap[p.priority] || p.priority);
        $('#planStatus').text(statusMap[p.status] || p.status);
        $('#planUser').text(p.userName || '-');
        $('#planDept').text(p.deptName || '-');
        $('#planCategory').text(p.categoryName || '-');
        $('#planQuant').text(p.quantTarget || '-');
        $('#planStart').text(p.startTime);
        $('#planEnd').text(p.endTime);
        $('#planDesc').html(p.description || '<p class="text-muted">暂无详细描述</p>');

        // 审批时间线
        if (p.status !== 'DRAFT') {
            loadApprovalTimeline('PLAN', id);
        } else {
            $('#approvalTimelineBox').hide();
        }

        // 关联成果
        if (p.hasAchievement && p.achievementId) {
            $('#achievementLinkBox').show();
            $('#achievementLink').html('<a href="#" onclick="viewAchievement(' + p.achievementId + ')" class="btn btn-primary btn-sm">查看成果详情</a>');
        }
    });
}

function loadApprovalTimeline(targetType, targetId) {
    $.get("/approval/timeline/" + targetType + "/" + targetId, function(res) {
        if (res.code !== 0 || !res.data || !res.data.length) {
            $('#approvalTimeline').html('<p class="text-muted">暂无审批记录</p>');
            return;
        }
        // 按级别分组
        var grouped = {};
        $.each(res.data, function(i, r) {
            if (!grouped[r.approvalLevel]) grouped[r.approvalLevel] = [];
            grouped[r.approvalLevel].push(r);
        });
        var html = '<ul class="timeline">';
        $.each(grouped, function(level, records) {
            html += '<li><i class="fa fa-circle"></i><span class="text-muted">第' + level + '级审批</span>';
            $.each(records, function(i, r) {
                var icon = r.action === 'APPROVE' ? 'fa-check text-success' : r.action === 'REJECT' ? 'fa-times text-danger' : r.action === 'TRANSFER' ? 'fa-share text-warning' : 'fa-clock-o text-muted';
                html += '<p><i class="fa ' + icon + '"></i> ' + r.approverName;
                if (r.action) html += ' - ' + (r.action === 'APPROVE' ? '通过' : r.action === 'REJECT' ? '驳回' : '转审');
                if (r.comment) html += '：' + r.comment;
                if (r.approvedAt) html += ' <small>' + r.approvedAt + '</small>';
                html += '</p>';
            });
            html += '</li>';
        });
        html += '</ul>';
        $('#approvalTimeline').html(html);
    });
}

function viewAchievement(id) {
    $.modal.open("成果详情", "/achievement/achievement/detail/" + id, "900px", "650px");
}
