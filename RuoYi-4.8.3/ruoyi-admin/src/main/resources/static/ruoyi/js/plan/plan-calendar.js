var calendar;
var currentPlanType = '';

$(function() {
    var calendarEl = document.getElementById('calendar');
    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'zh-cn',
        headerToolbar: { left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek' },
        events: fetchEvents,
        eventClick: function(info) {
            $.modal.open("计划详情", "/plan/plan/detail/" + info.event.id, "800px", "600px");
        }
    });
    calendar.render();
});

function fetchEvents(info, successCallback) {
    var start = info.startStr.substring(0, 7);
    var parts = start.split('-');
    $.get("/plan/plan/calendar-data", { year: parts[0], month: parts[1], planType: currentPlanType }, function(res) {
        var events = [];
        if (res.code === 0 && res.data) {
            $.each(res.data, function(date, plans) {
                $.each(plans, function(i, p) {
                    var color = p.status === 'APPROVED' ? '#1ab394' : p.status === 'APPROVING' ? '#f8ac59' : p.status === 'REJECTED' ? '#ed5565' : '#c4c4c4';
                    events.push({ id: p.id, title: p.title, start: date, color: color, textColor: '#fff' });
                });
            });
        }
        successCallback(events);
    });
}

function filterType(type) {
    currentPlanType = type;
    calendar.refetchEvents();
}
