package com.chy.customfields.multisearcher;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.MultiSelectCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldContextValueGeneratingClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.MultiSelectCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.context.*;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.ValidatingDecoratorQueryFactory;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.validator.DefaultOperatorUsageValidator;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.jql.validator.SelectCustomFieldValidator;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.jql.values.CustomFieldOptionsClauseValuesGenerator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.*;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.chy.customfields.searcher.IssueSelectCFSearchInputTransformer;

import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by chy on 19/9/10.
 */
@Scanned
public class MultiIssueSelectCFSearcher extends AbstractInitializationCustomFieldSearcher  implements CustomFieldSearcher,CustomFieldStattable {

    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    @ComponentImport
    private JqlOperandResolver jqlOperandResolver;
    @ComponentImport
    FieldVisibilityManager fieldVisibilityManager;
    @ComponentImport
    private CustomFieldInputHelper customFieldInputHelper;
    @ComponentImport
    private IssueManager issueManager;
    @ComponentImport
    private ProjectService projectService;
    @ComponentImport
    private I18nHelper.BeanFactory beanFactory;
    @ComponentImport
    private CustomFieldManager customFieldManager;
    @ComponentImport
    private SearchHandlerManager searchHandlerManager;
    @ComponentImport
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    @ComponentImport
    private PermissionManager permissionManager;
    @ComponentImport
    private SelectConverter selectConverter;

    @Inject
    public MultiIssueSelectCFSearcher(JqlOperandResolver jqlOperandResolver,
                                      FieldVisibilityManager fieldVisibilityManager,
                                      CustomFieldInputHelper customFieldInputHelper,
                                      IssueManager issueManager, ProjectService projectService,
                                      I18nHelper.BeanFactory beanFactory, CustomFieldManager customFieldManager,
                                      SearchHandlerManager searchHandlerManager, FieldConfigSchemeManager fieldConfigSchemeManager,
                                      PermissionManager permissionManager, SelectConverter selectConverter){
        this.jqlOperandResolver = jqlOperandResolver;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.customFieldInputHelper = customFieldInputHelper;
        this.issueManager = issueManager;
        this.projectService = projectService;
        this.beanFactory = beanFactory;
        this.customFieldManager = customFieldManager;
        this.searchHandlerManager = searchHandlerManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.permissionManager = permissionManager;
        this.selectConverter = selectConverter;
    }

    @Override
    public void init(CustomField customField) {

        FieldIndexer indexer = new MultiIssueCustomFieldIndexer(fieldVisibilityManager, customField);
        searcherInformation = new CustomFieldSearcherInformation(customField.getId(),
                customField.getNameKey(), Collections.singletonList(indexer), new AtomicReference(customField));

        searchRenderer = new MultiIssueSearcherRenderer(customField.getClauseNames(), getDescriptor(), customField,
                new MultiSelectCustomFieldValueProvider(), fieldVisibilityManager, this.projectService, this.issueManager);
        searchInputTransformer = new MultiIssueSelectCFSearchInputTransformer(this.searcherInformation.getId(),
                customField.getClauseNames(), customField, jqlOperandResolver, this.customFieldInputHelper);

        ComponentFactory componentFactory = JiraComponentFactory.getInstance();
        ClauseValidator clauseValidator = new IssueSelectClauseValidator(issueManager, jqlOperandResolver, beanFactory);

        //name resolver
        IssueResolver resolver = new IssueResolver(issueManager);
        //get query
        ClauseQueryFactory queryFactory = new SelectIssueClauseQueryFactory(customField, jqlOperandResolver, resolver);
        //validate sql
        OperatorUsageValidator usageValidator = new DefaultOperatorUsageValidator(jqlOperandResolver, beanFactory);
        //封装 queryFactory 并加上验证
        queryFactory = new ValidatingDecoratorQueryFactory(usageValidator, queryFactory);
        // clause context
        MultiClauseDecoratorContextFactory.Factory factory = new MultiClauseDecoratorContextFactory.Factory(usageValidator,jqlOperandResolver, ContextSetUtil.getInstance() );
        ClauseContextFactory clauseContextFactory = componentFactory.createObject(SelectCustomFieldClauseContextFactory.class, customField);

        ClauseValuesGenerator clauseValuesGenerator = new IssueClauseValuesGenerator();

        this.customFieldSearcherClauseHandler = new SimpleCustomFieldContextValueGeneratingClauseHandler(
                clauseValidator,
                queryFactory,
                factory.create(clauseContextFactory, false),
                clauseValuesGenerator,
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.ISSUE);
    }

    @Override
    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        return this.customFieldSearcherClauseHandler;
    }

    @Override
    public SearcherInformation<CustomField> getSearchInformation() {
        return this.searcherInformation;
    }

    @Override
    public SearchInputTransformer getSearchInputTransformer() {
        return this.searchInputTransformer;
    }

    /**
     * 搜索渲染 搜索框的样式
     * @return
     */
    @Override
    public SearchRenderer getSearchRenderer() {
        return this.searchRenderer;
    }

    @Override
    public StatisticsMapper getStatisticsMapper(CustomField customField) {
        return new SelectStatisticsMapper(customField, selectConverter, ComponentAccessor.getJiraAuthenticationContext(), this.customFieldInputHelper);
    }
}
