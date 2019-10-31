package com.chy.customfields.searcher;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleAllTextCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.FreeTextClauseQueryFactory;
import com.atlassian.jira.jql.validator.FreeTextFieldValidator;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by chy on 19/9/10.
 */
@Scanned
public class IssueSelectCFSearcher extends AbstractInitializationCustomFieldSearcher  implements CustomFieldStattable {

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

    @Inject
    public IssueSelectCFSearcher(JqlOperandResolver jqlOperandResolver,
                                 FieldVisibilityManager fieldVisibilityManager,
                                 CustomFieldInputHelper customFieldInputHelper,
                                 IssueManager issueManager){
        this.jqlOperandResolver = jqlOperandResolver;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.customFieldInputHelper = customFieldInputHelper;
        this.issueManager = issueManager;
    }

    @Override
    public void init(CustomField customField) {

        FieldIndexer indexer = new IssueCustomFieldIndexer(fieldVisibilityManager, customField);
        searcherInformation = new CustomFieldSearcherInformation(customField.getId(),
                customField.getNameKey(), Collections.singletonList(indexer),
                new AtomicReference(customField));

        searchRenderer = new CustomFieldRenderer(customField.getClauseNames(), getDescriptor(), customField,
                new SingleValueCustomFieldValueProvider(), fieldVisibilityManager);
        searchInputTransformer = new IssueSelectCFSearchInputTransformer(customField, customField.getClauseNames(), customField.getId(), customFieldInputHelper);

        customFieldSearcherClauseHandler = new SimpleAllTextCustomFieldSearcherClauseHandler(
                new FreeTextFieldValidator(customField.getId(), jqlOperandResolver),
                new FreeTextClauseQueryFactory(jqlOperandResolver, customField.getId()),
                        OperatorClasses.TEXT_OPERATORS,
                        JiraDataTypes.TEXT);

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

    @Override
    public SearchRenderer getSearchRenderer() {
        return this.searchRenderer;
    }

    @Override
    public StatisticsMapper getStatisticsMapper(CustomField customField) {
        return new IssueSelectStatisticsMapper(customField, issueManager);
    }
}
