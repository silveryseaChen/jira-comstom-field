package com.chy.customfields.multisearcher;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.query.*;
import com.atlassian.query.clause.TerminalClause;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chy on 19/9/30.
 */
public class SelectIssueClauseQueryFactory implements ClauseQueryFactory {

    private final ClauseQueryFactory delegateClauseQueryFactory;

    public SelectIssueClauseQueryFactory(CustomField customField, JqlOperandResolver operandResolver, IssueResolver resolver) {
        CustomFieldIndexInfoResolver indexInfoResolver = new CustomFieldIndexInfoResolver(resolver);
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList();
        operatorFactories.add(new EqualityQueryFactory(indexInfoResolver));
        this.delegateClauseQueryFactory = new GenericClauseQueryFactory(customField.getId(), operatorFactories, operandResolver);
    }

    @Nonnull
    @Override
    public QueryFactoryResult getQuery(@Nonnull QueryCreationContext queryCreationContext, @Nonnull TerminalClause terminalClause) {
        return this.delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }
}
