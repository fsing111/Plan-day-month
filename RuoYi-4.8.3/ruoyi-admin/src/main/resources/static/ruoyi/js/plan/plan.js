var prefix = "/plan/plan";

$(function() {
    var options = {
        url: prefix + "/list",
        createUrl: prefix + "/add",
        updateUrl: prefix + "/edit/{id}",
        detailUrl: prefix + "/detail/{id}",
        removeUrl: prefix + "/remove",
        modalName: "计划",
        search: false,
        showExport: false,
        columns: [
            {field: 'id', title: '序号', visible: false},
            {field: 'title', title: '计划标题', width: 200,
                formatter: function(value, row) {
                    return '<a href="#" onclick="viewPlan(' + row.id + ')">' + value + '</a>';
                }},
            {field: 'planType', title: '类型', width: 80,
                formatter: function(value) {
                    var map = {DAILY: '日报', WEEKLY: '周报', MONTHLY: '月报'};
                    return '<span class="label label-info">' + (map[value] || value) + '</span>';
                }},
            {field: 'priority', title: '优先级', width: 80,
                formatter: function(value) {
                    var cls = value === 'HIGH' ? 'danger' : value === 'MEDIUM' ? 'warning' : 'default';
                    var label = value === 'HIGH' ? '高' : value === 'MEDIUM' ? '中' : '低';
                    return '<span class="label label-' + cls + '">' + label + '</span>';
                }},
            {field: 'status', title: '状态', width: 90,
                formatter: function(value) {
                    var map = {DRAFT:'草稿',SUBMITTED:'已提交',APPROVING:'审批中',APPROVED:'已通过',REJECTED:'待修改',ARCHIVED:'已归档',OVERDUE:'已逾期'};
                    var cls = value === 'APPROVED' ? 'success' : value === 'REJECTED' ? 'danger' : value === 'OVERDUE' ? 'danger' : 'primary';
                    return '<span class="label label-' + cls + '">' + (map[value] || value) + '</span>';
                }},
            {field: 'userName', title: '提交人', width: 80},
            {field: 'startTime', title: '开始时间', width: 140},
            {field: 'endTime', title: '截止时间', width: 140},
            {field: 'createTime', title: '创建时间', width: 140},
            {title: '操作', width: 220, align: 'center',
                formatter: function(value, row) {
                    var html = '<a class="btn btn-xs btn-info" onclick="viewPlan(' + row.id + ')">查看</a> ';
                    if (row.status === 'DRAFT' || row.status === 'REJECTED') {
                        html += '<a class="btn btn-xs btn-primary" onclick="editPlan(' + row.id + ')">编辑</a> ';
                        html += '<a class="btn btn-xs btn-success" onclick="submitPlan(' + row.id + ')">提交</a> ';
                    }
                    if (row.status === 'SUBMITTED' || row.status === 'APPROVING') {
                        html += '<a class="btn btn-xs btn-warning" onclick="withdrawPlan(' + row.id + ')">撤回</a> ';
                    }
                    if (row.status === 'APPROVED') {
                        html += '<a class="btn btn-xs btn-default" onclick="copyPlan(' + row.id + ')">复制</a> ';
                    }
                    if (row.status === 'DRAFT') {
                        html += '<a class="btn btn-xs btn-danger" onclick="removePlan(' + row.id + ')">删除</a> ';
                    }
                    return html;
                }}
        ]
    };
    $.table.init(options);
});

function searchPlan() { $.table.search(); }
function resetSearch() { $('#searchForm')[0].reset(); $.table.search(); }

function addPlan() {
    $.modal.open("新建计划", prefix + "/add");
}
function editPlan(id) {
    $.modal.open("编辑计划", prefix + "/edit/" + id);
}
function viewPlan(id) {
    $.modal.open("计划详情", prefix + "/detail/" + id, "900px", "700px");
}
function submitPlan(id) {
    $.modal.confirm("确认提交该计划？", function() {
        $.operate.submit(prefix + "/submit/" + id);
    });
}
function withdrawPlan(id) {
    $.modal.confirm("确认撤回该计划？撤回后将回到草稿状态，审批记录将被清除。", function() {
        $.operate.submit(prefix + "/withdraw/" + id);
    });
}
function copyPlan(id) {
    $.modal.confirm("确认复制该计划？", function() {
        $.operate.submit(prefix + "/copy/" + id);
    });
}
function removePlan(id) {
    $.operate.remove(prefix + "/remove", id);
}
function exportPlans() {
    window.location.href = prefix + "/export?" + $('#searchForm').serialize();
}
