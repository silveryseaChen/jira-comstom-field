package com.chy.customfields.type;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.GenericImmutableOptions;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chy on 19/9/7.
 */
public class IssueFieldConfigItemType implements FieldConfigItemType {

    private static final Logger log = LoggerFactory.getLogger(IssueFieldConfigItemType.class);

    private IssueManager issueManager;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    private ProjectManager projectManager;
    private OptionsManager optionsManager;

    public IssueFieldConfigItemType(IssueManager issueManager, JiraAuthenticationContext jiraAuthenticationContext,
                                    FieldConfigSchemeManager fieldConfigSchemeManager, ProjectManager projectManager,
                                    OptionsManager optionsManager) {
        this.issueManager = issueManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.projectManager = projectManager;
        this.optionsManager = optionsManager;
    }

    @Override
    public String getDisplayName() {
        return "IssueSelectCFType";
    }

    @Override
    public String getDisplayNameKey() {
        return "issue.type";
    }

    @Override
    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem) {
        return fieldConfig.getCustomField().getCustomFieldType().getDescriptor().getDefaultViewHtml(fieldConfig, fieldLayoutItem);
    }

    @Override
    public String getObjectKey() {
        return "options";
    }

    @Override
    public Object getConfigurationObject(Issue issue, FieldConfig fieldConfig) {

        List<Issue> issues = new ArrayList<>();

        log.debug(" current issue {}", issue);
        try {
            Collection<Long> projectIds = CustomFieldUtils.getProjectIdsFromIssueOrFieldConfig(issue, fieldConfig,
                    this.fieldConfigSchemeManager, this.projectManager);
            for (Long pId: projectIds) {
                issues.addAll(issueManager.getIssueObjects(issueManager.getIssueIdsForProject(pId)));
            }
            log.debug(" issue items {} ", issues);

            //根据配置的类型判断
            Options options = optionsManager.getOptions(fieldConfig);
            Option option = null;
            if(options != null && !options.isEmpty() && (option = options.get(0)) != null){
                String issueType = option.getValue();
                issues = issues.stream().filter(i -> issueType.equals(i.getIssueTypeId())).collect(Collectors.toList());
            }
        } catch (GenericEntityException e) {
            log.error("IssueSelectCFType get option items error", e);
        }

        return new GenericImmutableOptions(issues, fieldConfig);
    }

    @Override
    public String getBaseEditUrl() {
        return "IssueSelectConfigAction!default.jspa";
    }
}
