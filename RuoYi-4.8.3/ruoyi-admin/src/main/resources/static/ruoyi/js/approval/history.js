$(function() {
    $.table.init({
        url: '/approval/history/list',
        modalName: '审批历史',
        columns: [
            {field: 'id', visible: false},
            {field: 'targetType', title: '类型', formatter: function(v){return v==='PLAN'?'<span class="label label-info">计划</span>':'<span class="label label-success">成果</span>';}},
            {field: 'submitterName', title: '提交人'},
            {field: 'targetTitle', title: '标题'},
            {field: 'action', title: '结果', formatter: function(v){
                var m={APPROVE:'<span class="label label-success">通过</span>',REJECT:'<span class="label label-danger">驳回</span>',TRANSFER:'<span class="label label-warning">转审</span>'};
                return m[v]||v;
            }},
            {field: 'comment', title: '意见'},
            {field: 'approverName', title: '审批人'},
            {field: 'approvedAt', title: '审批时间'}
        ]
    });
});
