package com.chy.customfields.searcher;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.FreeTextCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.searchers.transformer.TextQueryValidator;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.lucene.parsing.LuceneQueryParserFactory;
import org.apache.lucene.queryParser.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by chy on 19/9/17.
 */
public class IssueSelectCFSearchInputTransformer extends FreeTextCustomFieldSearchInputTransformer {

    private static final Logger logger = LoggerFactory.getLogger(IssueSelectCFSearchInputTransformer.class);

    private final TextQueryValidator textQueryValidator;

    public IssueSelectCFSearchInputTransformer(CustomField field, ClauseNames clauseNames, String urlParameterName, CustomFieldInputHelper customFieldInputHelper) {
        super(field, clauseNames, urlParameterName, customFieldInputHelper);
        textQueryValidator = new TextQueryValidator();
    }


    //修复异常 Internal error attempting to validate the search term
    @Override
    public void validateParams(ApplicationUser user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, I18nHelper i18nHelper, ErrorCollection errors) {

        CustomField field = getCustomField();
        if (fieldValuesHolder.containsKey(field.getId())) {
            CustomFieldParams customFieldParams = (CustomFieldParams)fieldValuesHolder.get(field.getId());

            Object paramValueObject = field.getCustomFieldType().getValueFromCustomFieldParams(customFieldParams);
            logger.debug(" validate value {}", paramValueObject);
            if(paramValueObject != null){
                String paramValue = ((Issue)paramValueObject).getKey();
                if (paramValue != null) {
                    QueryParser queryParser = ((LuceneQueryParserFactory)ComponentAccessor.getComponent(LuceneQueryParserFactory.class)).createParserFor(field.getId());
                    MessageSet validationResult = this.textQueryValidator.validate(queryParser, paramValue, field.getFieldName(), (String)null, true, i18nHelper);
                    Iterator var9 = validationResult.getErrorMessages().iterator();

                    while(var9.hasNext()) {
                        String errorMessage = (String)var9.next();
                        errors.addError(field.getId(), errorMessage);
                    }
                }
            }

        }
    }
}
