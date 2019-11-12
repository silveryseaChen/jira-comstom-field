console.log(" load event.js ")

JIRA.bind(JIRA.Events.INLINE_EDIT_STARTED, function (e, context, reason) {
    if(AJS.$(".filter-single-select").length > 0){
        AJS.$(".filter-single-select").auiSelect2();
        AJS.$(".filter-single-select").css("max-width", "none")
    }
})

JIRA.bind(JIRA.Events.NEW_PAGE_ADDED, function (e, context, reason) {
    if(AJS.$(".filter-single-select").length > 0){
        AJS.$(".filter-single-select").auiSelect2();
        AJS.$(".filter-single-select").css("max-width", "500px")
    }
})