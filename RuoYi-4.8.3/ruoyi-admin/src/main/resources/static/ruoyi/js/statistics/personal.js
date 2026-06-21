$(function(){loadStats();});
function loadStats(){
    $.get('/statistics/personal',function(res){
        if(res.code!==0||!res.data) return;
        var d = res.data;
        $('#totalPlans').text(d.totalPlans||0);
        $('#approvedPlans').text(d.approvedPlans||0);
        $('#completionRate').text((d.completionRate||0)+'%');
        $('#onTimeRate').text((d.onTimeRate||0)+'%');
        // 状态分布饼图
        var sc = echarts.init(document.getElementById('statusChart'));
        sc.setOption({tooltip:{trigger:'item'},series:[{type:'pie',radius:['40%','70%'],data:[
            {value:d.approvedPlans||0,name:'已通过'},{value:d.rejectedPlans||0,name:'待修改'},
            {value:d.draftPlans||0,name:'草稿'},{value:d.otherPlans||0,name:'其他'}
        ]}]});
        // 按类型柱状图
        var tc = echarts.init(document.getElementById('typeChart'));
        tc.setOption({tooltip:{},xAxis:{data:['日报','周报','月报']},yAxis:{},series:[
            {name:'已通过',type:'bar',data:[d.dailyApproved||0,d.weeklyApproved||0,d.monthlyApproved||0],itemStyle:{color:'#1ab394'}},
            {name:'总数',type:'bar',data:[d.dailyTotal||0,d.weeklyTotal||0,d.monthlyTotal||0],itemStyle:{color:'#c4c4c4'}}
        ]});
    });
}
