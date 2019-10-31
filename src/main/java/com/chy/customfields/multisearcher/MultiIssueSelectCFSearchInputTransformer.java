package com.chy.customfields.multisearcher;

import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.searchers.transformer.AbstractCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.MultiSelectCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import java.util.*;

/**
 *
 * 修改只能选择一个值的问题
 *
 * Created by chy on 19/10/26.
 */
public class MultiIssueSelectCFSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer implements SearchInputTransformer {

    private ClauseNames clauseNames;
    private JqlOperandResolver jqlOperandResolver;

    public MultiIssueSelectCFSearchInputTransformer(String urlParameterName, ClauseNames clauseNames, CustomField field,
                                                    JqlOperandResolver jqlOperandResolver, CustomFieldInputHelper customFieldInputHelper) {
        super( field, urlParameterName, customFieldInputHelper);
        this.clauseNames = clauseNames;
        this.jqlOperandResolver = jqlOperandResolver;
    }

    @Override
    protected Clause getClauseFromParams(ApplicationUser user, CustomFieldParams params) {
        Collection<String> searchValues = params.getValuesForNullKey();
        searchValues.removeAll(CollectionBuilder.newBuilder(new String[]{"-1", ""}).asCollection());
        if (!searchValues.isEmpty()) {
            if (searchValues.size() > 0) {
                return new TerminalClauseImpl(this.getClauseName(user, this.clauseNames), (String[])searchValues.toArray(new String[searchValues.size()]));
            }
        }

        return null;
    }

    protected CustomFieldParams getParamsFromSearchRequest(ApplicationUser user, Query query, SearchContext searchContext) {
        if (query != null && query.getWhereClause() != null) {
            SimpleNavigatorCollectorVisitor visitor = new SimpleNavigatorCollectorVisitor(this.clauseNames.getJqlFieldNames());
            query.getWhereClause().accept(visitor);
            if (visitor.isValid() && visitor.getClauses().size() == 1) {
                TerminalClause clause = visitor.getClauses().get(0);
                if (this.isValidOperatorForFitness(clause.getOperator())) {
                    List<QueryLiteral> literals = this.jqlOperandResolver.getValues(user, clause.getOperand(), clause);
                    if (literals != null && !literals.contains(new QueryLiteral())) {
                        Set<String> valuesAsStrings = new HashSet();
                        Iterator var8 = literals.iterator();

                        while(var8.hasNext()) {
                            QueryLiteral literal = (QueryLiteral)var8.next();
                            valuesAsStrings.add(literal.getStringValue());
                        }

                        if (valuesAsStrings.isEmpty()) {
                            return null;
                        }

                        return new CustomFieldParamsImpl(this.getCustomField(), valuesAsStrings);
                    }
                }
            }
        }

        return null;
    }

    protected boolean isValidOperatorForFitness(Operator operator) {
        return operator == Operator.EQUALS || operator == Operator.IS || operator == Operator.IN;
    }

    public boolean doRelevantClausesFitFilterForm(ApplicationUser user, Query query, SearchContext searchContext) {
        return this.getParamsFromSearchRequest(user, query, searchContext) != null;
    }

    public void validateParams(ApplicationUser user, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, I18nHelper i18nHelper, ErrorCollection errors) {
    }

}
