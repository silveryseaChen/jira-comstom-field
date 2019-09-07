package com.chy.customfields.type;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.option.GenericImmutableOptions;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by chy on 19/9/7.
 */
public class IssueFieldConfigItemType implements FieldConfigItemType {

    private static final Logger log = LoggerFactory.getLogger(IssueFieldConfigItemType.class);

    private IssueManager issueManager;
    private JiraAuthenticationContext jiraAuthenticationContext;

    public IssueFieldConfigItemType(IssueManager issueManager, JiraAuthenticationContext jiraAuthenticationContext) {
        this.issueManager = issueManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public String getDisplayName() {
        return "IssueSelectCFType";
    }

    @Override
    public String getDisplayNameKey() {
        return "IssueSelectCFType.key";
    }

    @Override
    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem) {
        return this.jiraAuthenticationContext.getI18nHelper().getText("IssueSelectCFType.name");
    }

    @Override
    public String getObjectKey() {
        return "options";
    }

    @Override
    public Object getConfigurationObject(Issue issue, FieldConfig fieldConfig) {

        List<Issue> issues = Collections.emptyList();
        if (issue != null && issue.getProjectId() != null) {
            try {
                issues = issueManager.getIssueObjects(issueManager.getIssueIdsForProject(issue.getProjectId()));
            } catch (GenericEntityException e) {
                log.error("IssueSelectCFType get option items error", e);
            }
        }

        return new GenericImmutableOptions(issues, fieldConfig);
    }

    @Override
    public String getBaseEditUrl() {
        return null;
    }
}
