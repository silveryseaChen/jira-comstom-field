package com.chy.customfields.multisearcher;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 返回匹配的结果
 * 解决查不到结果问题
 * Created by chy on 19/10/26.
 */
public class IssueClauseValuesGenerator implements ClauseValuesGenerator {

    @Override
    public Results getPossibleValues(ApplicationUser applicationUser, String jqlClauseName, String valuePrefix, int maxNumResults) {

        Set<String> values = new LinkedHashSet();
        List<Result> results = Lists.newArrayListWithCapacity(values.size());
        results.add(new Result(valuePrefix));
        return new Results(results);
    }
}
