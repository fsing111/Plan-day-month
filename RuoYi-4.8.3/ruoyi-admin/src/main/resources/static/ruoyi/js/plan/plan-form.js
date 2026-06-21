var prefix = "/plan/plan";

$(function() {
    // 初始化 Summernote 富文本编辑器
    $('.summernote').summernote({
        height: 250,
        lang: 'zh-CN',
        toolbar: [
            ['style', ['style']],
            ['font', ['bold', 'italic', 'underline', 'clear']],
            ['color', ['color']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['table', ['table']],
            ['insert', ['link', 'picture']],
            ['view', ['fullscreen', 'codeview']]
        ]
    });

    // 加载分类下拉
    $.get("/plan/category/all", function(res) {
        if (res.code === 0) {
            var html = '<option value="">请选择</option>';
            $.each(res.data, function(i, c) {
                html += '<option value="' + c.id + '">' + c.name + '</option>';
            });
            $('#categorySelect').html(html);
        }
    });

    // 计划类型切换时加载可引用计划
    $('select[name="planType"]').on('change', function() {
        var planType = $(this).val();
        if (planType === 'DAILY') {
            $('#refPlanSelect').closest('.form-group').hide();
        } else {
            $('#refPlanSelect').closest('.form-group').show();
            // 如果有 planId（编辑模式），加载汇总选项
            loadRollupOptions(planType);
        }
    });

    // 编辑模式：预填数据
    var planId = getQueryParam('planId');
    if (planId) {
        $('#planId').val(planId);
        $.get(prefix + "/detail/" + planId, function(res) {
            if (res.code === 0 && res.data) {
                fillForm(res.data);
            }
        });
    }
});

function loadRollupOptions(planType) {
    var planId = $('#planId').val() || 0;
    $.get(prefix + "/rollup-options", {planId: planId, planType: planType}, function(res) {
        if (res.code === 0 && res.data) {
            var html = '';
            $.each(res.data, function(i, p) {
                html += '<option value="' + p.id + '">[' + (p.planType === 'DAILY' ? '日报' : '周报') + '] ' + p.title + '</option>';
            });
            $('#refPlanSelect').html(html || '<option value="">暂无可用引用</option>');
        }
    });
}

function fillForm(plan) {
    $('select[name="planType"]').val(plan.planType);
    $('select[name="priority"]').val(plan.priority);
    $('input[name="title"]').val(plan.title);
    $('input[name="startTime"]').val(plan.startTime);
    $('input[name="endTime"]').val(plan.endTime);
    if (plan.categoryId) $('select[name="categoryId"]').val(plan.categoryId);
    if (plan.quantTarget) $('input[name="quantTarget"]').val(plan.quantTarget);
    if (plan.description) $('.summernote').summernote('code', plan.description);
    if (plan.planType !== 'DAILY') loadRollupOptions(plan.planType);
}

function getQueryParam(name) {
    var url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"), results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
