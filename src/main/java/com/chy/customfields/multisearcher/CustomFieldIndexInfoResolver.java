package com.chy.customfields.multisearcher;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chy on 19/9/30.
 */
public class CustomFieldIndexInfoResolver implements IndexInfoResolver<CustomField> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldIndexInfoResolver.class);

    private final IssueResolver resolver;

    public CustomFieldIndexInfoResolver(IssueResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public List<String> getIndexedValues(String s) {
        LOGGER.debug(" get indexed values {} ", s);
        return this.resolver.getIdsFromName(s);
    }

    @Override
    public List<String> getIndexedValues(Long aLong) {
        LOGGER.debug(" get indexed values {} ", aLong);
        Issue issue = this.resolver.get(aLong);
        List<String> list = new ArrayList<>();
        list.add(issue.getKey());

        return list;
    }

    @Override
    public String getIndexedValue(CustomField customField) {
        LOGGER.debug(" get indexed value {} ", customField);
        return customField.toString().toLowerCase();
    }
}
