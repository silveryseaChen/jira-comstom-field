package com.chy.customfields.multisearcher;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.validator.SupportedOperatorsValidator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * jql 验证类
 * 解决：The option 'VALID-1' for field '问题选择' does not exist
 * Created by chy on 19/10/26.
 */
public class IssueSelectClauseValidator implements ClauseValidator {

    private SupportedOperatorsValidator supportedOperatorsValidator;
    private I18nHelper.BeanFactory beanFactory;
    private JqlOperandResolver jqlOperandResolver;
    private IssueManager issueManager;

    public IssueSelectClauseValidator(IssueManager issueManager, JqlOperandResolver jqlOperandResolver, I18nHelper.BeanFactory beanFactory) {
        this.supportedOperatorsValidator = new SupportedOperatorsValidator(new Collection[]{OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY});
        this.beanFactory = beanFactory;
        this.jqlOperandResolver = jqlOperandResolver;
        this.issueManager = issueManager;
    }


    @Nonnull
    @Override
    public MessageSet validate(ApplicationUser applicationUser, @Nonnull TerminalClause terminalClause) {

        MessageSet messageSet = this.supportedOperatorsValidator.validate(applicationUser, terminalClause);
        return !messageSet.hasAnyMessages() ? validateValues(applicationUser, terminalClause) : messageSet;
    }

    private MessageSet validateValues(ApplicationUser searcher, TerminalClause terminalClause) {
        I18nHelper i18n = this.beanFactory.getInstance(searcher);
        MessageSet messageSet = new MessageSetImpl();
        Operand operand = terminalClause.getOperand();
        List<QueryLiteral> literals = this.jqlOperandResolver.getValues(searcher, operand, terminalClause);
        if (literals != null && !literals.isEmpty()) {
            Iterator var7 = literals.iterator();

            while(var7.hasNext()) {
                QueryLiteral literal = (QueryLiteral)var7.next();
                if (!literal.isEmpty()) {
                    MutableIssue issue = issueManager.getIssueByCurrentKey(literal.getStringValue());
                    if (issue == null) {
                        if (this.jqlOperandResolver.isFunctionOperand(literal.getSourceOperand())) {
                            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.select.option.does.not.exist.function", literal.getSourceOperand().getName(), terminalClause.getName()));
                        } else {
                            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.select.option.does.not.exist", literal.asString(), terminalClause.getName()));
                        }
                    }
                }
            }

            return messageSet;
        } else {
            return messageSet;
        }
    }

}
