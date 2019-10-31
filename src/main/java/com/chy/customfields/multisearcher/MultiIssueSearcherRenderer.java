package com.chy.customfields.multisearcher;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.SearchContextRenderHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webwork.action.Action;

import java.util.*;

/**
 * Created by chy on 19/9/29.
 */
public class MultiIssueSearcherRenderer extends CustomFieldRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiIssueSearcherRenderer.class);

    private ProjectService projectService;
    private IssueManager issueManager;
    private CustomFieldValueProvider customFieldValueProvider;

    public MultiIssueSearcherRenderer(ClauseNames clauseNames, CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor,
                                      CustomField field, CustomFieldValueProvider customFieldValueProvider,
                                      FieldVisibilityManager fieldVisibilityManager,
                                      ProjectService projectService, IssueManager issueManager) {
        super(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
        this.projectService = projectService;
        this.issueManager = issueManager;
        this.customFieldValueProvider = customFieldValueProvider;
    }

    @Override
    public String getEditHtml(ApplicationUser user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action) {

        Collection<Long> issueIds = new ArrayList<>();
        ServiceOutcome<List<Project>> allProjects = this.projectService.getAllProjects(user);
        if (allProjects.isValid()){
            allProjects.getReturnedValue().forEach(project -> {
                try {
                    issueIds.addAll(issueManager.getIssueIdsForProject(project.getId()));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            });
        }

        HashMap<String, Object> velocityParams = new HashMap();
        velocityParams.put("fieldkey", getField().getCustomFieldType().getKey());
        velocityParams.put("allOptions", issueManager.getIssueObjects(issueIds));
        SearchContextRenderHelper.addSearchContextParams(searchContext, velocityParams);
        return this.getEditHtml(searchContext, fieldValuesHolder, displayParameters, action, velocityParams);
    }

    @Override
    public String getViewHtml(ApplicationUser user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters, Action action) {

        LOGGER.debug(" get view html {} {}", searchContext, fieldValuesHolder);

        Object o = customFieldValueProvider.getStringValue(this.getField(), fieldValuesHolder);

        LOGGER.debug(" field values {} ", o);
        List<Issue> issues = new ArrayList<>();
        if( o != null && o instanceof Collection){
            ((Collection)o).forEach(val -> {
                MutableIssue issue = issueManager.getIssueByCurrentKey(val.toString());
                issues.add(issue);
            });
        } else {
            MutableIssue issue = issueManager.getIssueByCurrentKey(o.toString());
            issues.add(issue);
        }

        HashMap<String, Object> velocityParams = new HashMap();
        velocityParams.put("fieldkey", getField().getCustomFieldType().getKey());
        velocityParams.put("selectedOptions", issues);
        SearchContextRenderHelper.addSearchContextParams(searchContext, velocityParams);
        return this.getViewHtml(searchContext, fieldValuesHolder, displayParameters, action, velocityParams);


    }
}
