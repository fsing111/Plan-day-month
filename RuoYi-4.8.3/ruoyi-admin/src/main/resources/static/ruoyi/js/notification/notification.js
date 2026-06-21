$(function() {
    $.table.init({
        url: '/notification/notification/list',
        modalName: '通知',
        columns: [
            {field: 'id', visible: false},
            {field: 'title', title: '标题', formatter: function(v,r) {
                var icon = r.isRead ? 'fa-envelope-open-o' : 'fa-envelope text-warning';
                return '<i class="fa '+icon+'"></i> ' + v;
            }},
            {field: 'content', title: '内容', formatter: function(v){ return v ? v.substring(0,80) : '-'; }},
            {field: 'type', title: '类型'},
            {field: 'createTime', title: '时间'},
            {title: '操作', align:'center', formatter: function(v,r) {
                var h = '';
                if (!r.isRead) h += '<button class="btn btn-xs btn-info" onclick="markRead('+r.id+')">标为已读</button> ';
                if (r.relatedId) h += '<button class="btn btn-xs btn-default" onclick="viewRelated(\''+r.relatedType+'\','+r.relatedId+')">查看</button> ';
                h += '<button class="btn btn-xs btn-danger" onclick="removeNotify('+r.id+')">删除</button>';
                return h;
            }}
        ]
    });
    updateUnreadCount();
});

function markRead(id) { $.post('/notification/notification/read/'+id, function(res){ if(res.code===0) $.table.refresh(); }); }
function markAllRead() { $.post('/notification/notification/read-all', function(res){ if(res.code===0) { $.table.refresh(); updateUnreadCount(); } }); }
function removeNotify(id) { $.operate.remove('/notification/notification/remove', id); }
function viewRelated(type, id) {
    var url = type==='PLAN' ? '/plan/plan/detail/' : '/achievement/achievement/detail/';
    $.modal.open("详情", url+id, "900px", "650px");
}
function updateUnreadCount() {
    $.get('/notification/notification/unread-count', function(res) {
        var count = res.data || 0;
        $('#notificationBadge').text(count).toggle(count > 0);
    });
}
