var prefix = "/achievement/achievement";

$(function() {
    $.table.init({
        url: prefix + "/list",
        removeUrl: prefix + "/remove",
        modalName: "成果",
        columns: [
            {field: 'id', title: '序号', visible: false},
            {field: 'planTitle', title: '关联计划', formatter: function(v,r){return v || '-';}},
            {field: 'planType', title: '计划类型', formatter: typeFormatter},
            {field: 'description', title: '完成说明', formatter: function(v){return v ? v.substring(0,60)+'...' : '-';}},
            {field: 'status', title: '状态', formatter: statusFormatter},
            {field: 'actualQty', title: '实际数量'},
            {field: 'actualHours', title: '耗时(h)'},
            {field: 'userName', title: '提交人'},
            {field: 'submittedAt', title: '提交时间'},
            {title: '操作', align: 'center', formatter: actionFormatter}
        ]
    });
});

function typeFormatter(v) {
    var m = {DAILY:'日报',WEEKLY:'周报',MONTHLY:'月报'};
    return '<span class="label label-info">'+(m[v]||v)+'</span>';
}
function statusFormatter(v) {
    var m = {PENDING:'待填写',SUBMITTED:'已提交',APPROVING:'验收中',APPROVED:'已通过',REJECTED:'待修改'};
    var c = v==='APPROVED'?'success':v==='REJECTED'?'danger':'primary';
    return '<span class="label label-'+c+'">'+(m[v]||v)+'</span>';
}
function actionFormatter(v, row) {
    var h = '<a class="btn btn-xs btn-info" onclick="viewAchievement('+row.id+')">查看</a> ';
    if (row.status==='PENDING'||row.status==='REJECTED') {
        h += '<a class="btn btn-xs btn-primary" onclick="editAchievement('+row.id+')">编辑</a> ';
        h += '<a class="btn btn-xs btn-success" onclick="submitAchievement('+row.id+')">提交</a> ';
    }
    if (row.status==='SUBMITTED'||row.status==='APPROVING') {
        h += '<a class="btn btn-xs btn-warning" onclick="withdrawAchievement('+row.id+')">撤回</a> ';
    }
    if (row.status==='PENDING') h += '<a class="btn btn-xs btn-danger" onclick="removeAchievement('+row.id+')">删除</a>';
    return h;
}
function addAchievement() { $.modal.open("提交成果", prefix + "/add", "900px", "650px"); }
function editAchievement(id) { $.modal.open("编辑成果", prefix + "/edit/" + id, "900px", "650px"); }
function viewAchievement(id) { $.modal.open("成果详情", prefix + "/detail/" + id, "900px", "650px"); }
function submitAchievement(id) { $.modal.confirm("确认提交？",function(){$.operate.submit(prefix+"/submit/"+id);}); }
function withdrawAchievement(id) { $.modal.confirm("确认撤回？",function(){$.operate.submit(prefix+"/withdraw/"+id);}); }
function removeAchievement(id) { $.operate.remove(prefix+"/remove", id); }
function resetForm() { $('#searchForm')[0].reset(); $.table.search(); }
