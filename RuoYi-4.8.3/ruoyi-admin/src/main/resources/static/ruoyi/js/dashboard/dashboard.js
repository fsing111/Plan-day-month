$(function() {
    loadDashboard();
});

function loadDashboard() {
    $.get("/plan/plan/list", {pageSize: 1}, function(res) {
        $('#todayPlans').text(res.total || 0);
    });
    $.get("/approval/list", {pageSize: 1}, function(res) {
        $('#pendingApprovals').text(res.total || 0);
    });
    $.get("/plan/plan/list", {status: 'REJECTED', pageSize: 1}, function(res) {
        $('#rejectedCount').text(res.total || 0);
    });
    // 最近通知
    $.get("/notification/notification/list", {pageSize: 5}, function(res) {
        var html = '';
        if (res.rows && res.rows.length > 0) {
            $.each(res.rows, function(i, n) {
                var icon = n.isRead ? 'fa-envelope-open-o' : 'fa-envelope';
                var cls = n.isRead ? '' : 'text-warning';
                html += '<p class="'+cls+'"><i class="fa '+icon+'"></i> '+n.title+' <small>'+n.createTime+'</small></p>';
            });
        } else { html = '<p class="text-muted">暂无通知</p>'; }
        $('#recentNotifications').html(html);
    });
    // 趋势图
    var chart = echarts.init(document.getElementById('trendChart'));
    chart.setOption({
        tooltip: {trigger:'axis'},
        xAxis: {type:'category',data:['周一','周二','周三','周四','周五','周六','周日']},
        yAxis: {type:'value',max:100},
        series: [{data:[],type:'line',smooth:true,areaStyle:{}}]
    });
}
