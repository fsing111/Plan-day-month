$(function(){loadTeamStats();});
function loadTeamStats(){
    $.get('/statistics/team',function(res){
        if(res.code!==0||!res.data) return;
        var d = res.data;
        $('#teamMembers').text(d.totalMembers||0);
        $('#teamPlans').text(d.totalPlans||0);
        $('#teamRate').text((d.avgCompletionRate||0)+'%');
        if(d.unsubmittedNames && d.unsubmittedNames.length>0) {
            var h = '';
            $.each(d.unsubmittedNames,function(i,n){h+='<span class="label label-danger m-r-xs">'+n+'</span> ';});
            $('#unsubmitted').html(h);
        } else { $('#unsubmitted').html('<p class="text-success">全部已提交</p>'); }
        if(d.memberStats && d.memberStats.length>0) {
            var names=[], rates=[];
            $.each(d.memberStats,function(i,m){names.push(m.userName);rates.push(m.completionRate||0);});
            var mc = echarts.init(document.getElementById('memberChart'));
            mc.setOption({tooltip:{},xAxis:{type:'value',max:100},yAxis:{type:'category',data:names.reverse()},
                series:[{type:'bar',data:rates.reverse(),itemStyle:{color:'#1ab394'},label:{show:true,formatter:'{c}%'}}]});
        }
    });
}
