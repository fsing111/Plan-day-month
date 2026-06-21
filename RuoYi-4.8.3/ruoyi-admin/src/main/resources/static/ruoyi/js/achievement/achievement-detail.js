$(function() {
    var id = $.common.getQueryVariable("achievementId") || window.location.pathname.split('/').pop();
    if (id) loadDetail(id);
});

function loadDetail(id) {
    $.get("/achievement/achievement/detail/" + id, function(res) {
        if (res.code!==0||!res.data) return;
        var a = res.data;
        var statusMap = {PENDING:'待填写',SUBMITTED:'已提交',APPROVING:'验收中',APPROVED:'已通过',REJECTED:'待修改'};
        $('#achvStatus').text(statusMap[a.status]||a.status);
        $('#achvQty').text(a.actualQty||'-');
        $('#achvHours').text(a.actualHours? a.actualHours+'h' : '-');
        $('#achvSubmitted').text(a.submittedAt||'-');
        $('#achvIssues').text(a.issues||'无');
        $('#achvRemark').text(a.remark||'无');
        $('#achvDesc').html(a.description||'-');
        // 关联计划
        if (a.planIds && a.planIds.length>0) {
            var plans = a.planIds.join(', ');
            $('#achvPlans').text('关联计划ID: ' + plans + ' | ' + (a.planTitle||''));
        } else {
            $('#achvPlans').text(a.planTitle||'-');
        }
        // 附件
        if (a.attachments && a.attachments.length>0) {
            var fhtml = '';
            $.each(a.attachments, function(i,f) {
                fhtml += '<p><i class="fa fa-paperclip"></i> <a href="/file/download/'+f.id+'">'+f.fileName+'</a> ('+(f.fileSize/1024).toFixed(1)+'KB)</p>';
            });
            $('#achvFiles').html(fhtml);
        } else { $('#achvFiles').html('<p class="text-muted">无附件</p>'); }
        // 计划vs实际对比
        var vsHtml = '<table class="table"><tr><th></th><th>计划</th><th>实际</th></tr>';
        vsHtml += '<tr><td>指标</td><td>'+(a.planQuantTarget||'-')+'</td><td>'+(a.actualQty||'-')+'</td></tr>';
        vsHtml += '<tr><td>耗时</td><td>-</td><td>'+(a.actualHours?a.actualHours+'h':'-')+'</td></tr></table>';
        if (a.comparisonStatus) {
            var cmpMap = {MATCH:'<span class="label label-success">已达成</span>',PARTIAL:'<span class="label label-warning">部分达成</span>',EXCEED:'<span class="label label-info">超额完成</span>',NOT_MATCH:'<span class="label label-danger">未达成</span>'};
            vsHtml += '<p>'+ (cmpMap[a.comparisonStatus]||a.comparisonStatus) +'</p>';
        }
        $('#vsPanel').html(vsHtml);
        // 审批时间线
        if (a.status !== 'PENDING') {
            $.get("/approval/timeline/ACHIEVEMENT/" + id, function(r2) {
                var html = '<ul class="timeline">';
                $.each(r2.data||[], function(i,r){
                    var icon = r.action==='APPROVE'?'fa-check text-success':r.action==='REJECT'?'fa-times text-danger':'fa-clock-o';
                    html += '<li><i class="fa '+icon+'"></i> '+r.approverName+' - '+(r.action||'待审批');
                    if (r.comment) html += '：'+r.comment;
                    if (r.approvedAt) html += ' <small>'+r.approvedAt+'</small>';
                    html += '</li>';
                });
                $('#achvTimeline').html(html+'</ul>');
            });
        } else { $('#achvTimeline').html('<p class="text-muted">尚未提交，无审批记录</p>'); }
    });
}
