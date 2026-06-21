$(function() {
    $.table.init({
        url: '/plan/template/list', modalName: '模板',
        columns: [
            {field:'id',visible:false},
            {field:'name',title:'模板名称'},
            {field:'planType',title:'类型',formatter:function(v){var m={DAILY:'日报',WEEKLY:'周报',MONTHLY:'月报'};return '<span class="label label-info">'+(m[v]||v)+'</span>';}},
            {field:'title',title:'标题'},
            {field:'createTime',title:'创建时间'},
            {title:'操作',align:'center',formatter:function(v,r){
                return '<button class="btn btn-xs btn-success" onclick="useTemplate('+r.id+')">使用</button> '+
                       '<button class="btn btn-xs btn-danger" onclick="removeTemplate('+r.id+')">删除</button>';
            }}
        ]
    });
});
function addTemplate() { $.modal.open("新建模板", "/plan/template/add"); }
function useTemplate(id) { $.operate.addTab("新建计划", "/plan/plan/add?templateId=" + id); }
function removeTemplate(id) { $.operate.remove('/plan/template/remove', id); }
