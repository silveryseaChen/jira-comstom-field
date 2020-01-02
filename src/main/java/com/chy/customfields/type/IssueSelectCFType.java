package com.chy.customfields.type;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.impl.AbstractSingleFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by chy on 19/9/7.
 */
@Scanned
public class IssueSelectCFType extends AbstractSingleFieldType<Issue> {

    @ComponentImport
    private IssueManager issueManager;
    @ComponentImport
    private JiraAuthenticationContext jiraAuthenticationContext;
    @ComponentImport
    private CustomFieldValuePersister customFieldValuePersister;
    @ComponentImport
    private GenericConfigManager genericConfigManager;
    @ComponentImport
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    @ComponentImport
    private ProjectManager projectManager;
    @ComponentImport
    private OptionsManager optionsManager;

    @Inject
    public IssueSelectCFType(CustomFieldValuePersister customFieldValuePersister,
                             GenericConfigManager genericConfigManager,
                             IssueManager issueManager,
                             JiraAuthenticationContext jiraAuthenticationContext,
                             FieldConfigSchemeManager fieldConfigSchemeManager,
                             ProjectManager projectManager,
                             OptionsManager optionsManager) {
        super(customFieldValuePersister, genericConfigManager);
        this.issueManager = issueManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.projectManager = projectManager;
        this.optionsManager = optionsManager;
    }

    /**
     * 设置 组件 选项
     * @return
     */
    @Nonnull
    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        List<FieldConfigItemType> list = super.getConfigurationItemTypes();
        list.add(new IssueFieldConfigItemType(issueManager, jiraAuthenticationContext,
                this.fieldConfigSchemeManager, this.projectManager, this.optionsManager));
        return list;
    }

    /**
     * 保存的数据库类型
     * @return
     */
    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    /**
     * 对象转换为存储类型
     * 以 issue key 来存储
     * @param issue
     * @return
     */
    @Nullable
    @Override
    protected Object getDbValueFromObject(Issue issue) {
        return getStringFromSingularObject(issue);
    }

    /**
     * 存储类型转换为对象
     * @param o
     * @return
     * @throws FieldValidationException
     */
    @Nullable
    @Override
    protected Issue getObjectFromDbValue(@Nonnull Object o) throws FieldValidationException {
        if(null != o) {
            return getSingularObjectFromString(o.toString());
        }
        return null;
    }

    /**
     * 对象转化为String
     * 以issue key 作为标识
     * @param issue
     * @return
     */
    @Override
    public String getStringFromSingularObject(Issue issue) {
        return issue == null? null: issue.getKey();
    }

    /**
     * String  转换为 对象
     * @param s
     * @return
     * @throws FieldValidationException
     */
    @Override
    public Issue getSingularObjectFromString(String s) throws FieldValidationException {
        return issueManager.getIssueObject(s);
    }


}
