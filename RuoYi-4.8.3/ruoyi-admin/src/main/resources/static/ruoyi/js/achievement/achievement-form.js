$(function() {
    $('.summernote').summernote({height:200, lang:'zh-CN'});
    loadApprovedPlans();
});

function loadApprovedPlans() {
    $.get("/plan/plan/list", {status:'APPROVED',pageSize:100}, function(res) {
        if (res.code===0 && res.rows) {
            var h = '';
            $.each(res.rows, function(i,p) {
                h += '<option value="'+p.id+'">['+(p.planType==='DAILY'?'日报':p.planType==='WEEKLY'?'周报':'月报')+'] '+p.title+'</option>';
            });
            $('#planSelect').html(h || '<option value="">暂无已通过的计划</option>');
        }
    });
}
