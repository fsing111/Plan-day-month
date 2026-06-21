$(function() {
    initTable('planPendingTable', 'PLAN', '#planToolbar');
    initTable('achvPendingTable', 'ACHIEVEMENT', '#achvToolbar');
    $('#approvalTabs a').on('shown.bs.tab', function() {
        var type = $(this).attr('href') === '#planTab' ? 'PLAN' : 'ACHIEVEMENT';
        $('#' + (type === 'PLAN' ? 'planPendingTable' : 'achvPendingTable')).bootstrapTable('refresh');
    });
});

function initTable(tableId, targetType, toolbar) {
    $('#' + tableId).bootstrapTable({
        url: '/approval/list',
        method: 'post',
        queryParams: function(p) { p.targetType = targetType; return p; },
        toolbar: toolbar,
        striped: true, pagination: true, sidePagination: 'server', pageSize: 10,
        columns: [
            {checkbox: true},
            {field: 'id', title: 'ID', visible: false},
            {field: 'submitterName', title: '提交人'},
            {field: 'targetTitle', title: '标题', formatter: function(v,r) { return v || r.targetType + '-' + r.targetId; }},
            {field: 'targetPlanType', title: '类型', formatter: function(v) {
                var m = {DAILY:'日报',WEEKLY:'周报',MONTHLY:'月报'};
                return '<span class="label label-info">'+(m[v]||v)+'</span>';
            }},
            {field: 'approvalLevel', title: '审批级'},
            {field: 'peerApprovals', title: '同级审批状态', formatter: function(v) {
                if (!v) return '-';
                var h = '';
                $.each(v, function(i,r) {
                    var c = r.action==='APPROVE'?'success':r.action==='REJECT'?'danger':'default';
                    var l = r.action==='APPROVE'?'通过':r.action==='REJECT'?'驳回':r.action==='TRANSFER'?'转审':'待审';
                    h += '<span class="label label-'+c+'">'+r.approverName+':'+l+'</span> ';
                });
                return h;
            }},
            {field: 'createTime', title: '提交时间'},
            {title: '操作', align: 'center', formatter: function(v,r) {
                return '<button class="btn btn-xs btn-primary" onclick="openApproval('+r.id+')">审批</button>';
            }}
        ]
    });
}

function openApproval(recordId) {
    $('#approvalRecordId').val(recordId);
    $('#approvalComment').val('');
    $('#commentRequired').hide();
    $('#transferGroup').hide();
    $('#rejectBtn').prop('disabled', false);
    $('#approvalModal').modal('show');
}

function showTransfer() {
    $('#transferGroup').show();
    $.get('/system/user/list', {pageSize: 100}, function(res) {
        var h = '';
        $.each(res.rows||[], function(i,u){ h += '<option value="'+u.userId+'">'+u.userName+'</option>'; });
        $('#transferUser').html(h);
    });
}

function doAction(action) {
    var id = $('#approvalRecordId').val();
    var comment = $('#approvalComment').val();
    if (action === 'REJECT' && !comment.trim()) {
        $.modal.alertWarning('驳回必须填写审批意见（Gap-5强制要求）');
        return;
    }
    var url = action==='APPROVE'?'/approval/approve/':action==='REJECT'?'/approval/reject/':'/approval/transfer/';
    var data = {comment: comment};
    if (action==='TRANSFER') data.targetUserId = $('#transferUser').val();
    $.post(url + id, data, function(res) {
        if (res.code === 0) { $('#approvalModal').modal('hide'); $.table.refresh(); }
        else $.modal.alertError(res.msg);
    });
}

function batchApprove(targetType) {
    var tableId = targetType === 'PLAN' ? 'planPendingTable' : 'achvPendingTable';
    var rows = $('#' + tableId).bootstrapTable('getSelections');
    if (!rows.length) { $.modal.alertWarning('请先选择要批量审批的记录'); return; }
    var ids = $.map(rows, function(r) { return r.id; });
    $.modal.confirm('确认批量通过 ' + ids.length + ' 条审批？', function() {
        $.ajax({ url: '/approval/batch-approve', method: 'POST', contentType: 'application/json',
            data: JSON.stringify({recordIds: ids, comment: '批量通过'}),
            success: function(res) {
                if (res.code === 0) { $.modal.alertSuccess('成功 ' + res.data.successCount + ' 条'); $.table.refresh(); }
                else $.modal.alertError(res.msg);
            }
        });
    });
}
