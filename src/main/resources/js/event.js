console.log(" load event.js ")
//add filter
JIRA.bind(JIRA.Events.INLINE_EDIT_STARTED, function (e, context, reason) {
    filterSelectEventHandler();
})

JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
    filterSelectEventHandler();
})

JIRA.bind(JIRA.Events.NEW_PAGE_ADDED, function (e, context, reason) {
    filterSelectEventHandler();
})

function filterSelectEventHandler() {
    AJS.$("select.filter-single-select").change(function (val1, val2, val3) {
        var selectVal  = AJS.$(this).find("option:selected").html().trim();
        console.log("single-filter-select change"+selectVal);

        var multiSelect = AJS.$(this).siblings(".jira-multi-select");
        var items = multiSelect.find(".representation ul li");
        if(items && items.length > 0){
            AJS.$.each(items, function (index, item) {
                if($(item).find("span.value-text").html() != selectVal){
                    $(item).find("em.item-delete").click();
                }
            })
        }
    })
}
