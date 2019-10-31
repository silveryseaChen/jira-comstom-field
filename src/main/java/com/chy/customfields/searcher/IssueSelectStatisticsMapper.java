package com.chy.customfields.searcher;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * Created by chy on 19/10/30.
 * 在 仪表板中可选
 */
public class IssueSelectStatisticsMapper implements StatisticsMapper<Issue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueSelectStatisticsMapper.class);

    private CustomField customField;
    private IssueManager issueManager;

    public IssueSelectStatisticsMapper(CustomField customField, IssueManager issueManager) {
        this.customField = customField;
        this.issueManager = issueManager;
    }

    @Override
    public boolean isValidValue(Issue o) {
        return true;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue() {
        return false;
    }

    @Override
    public SearchRequest getSearchUrlSuffix(Issue o, SearchRequest searchRequest) {
        return null;
    }

    @Override
    public String getDocumentConstant() {
        return this.customField.getId();
    }

    @Override
    public Issue getValueFromLuceneField(String documentValue) {
        LOGGER.debug( " static mapper document value is {}", documentValue );
        Issue issue = issueManager.getIssueObject(documentValue.toUpperCase());
        LOGGER.debug( " static mapper issue is {}", issue );
        return issue;
    }

    @Override
    public Comparator<Issue> getComparator() {
        return (o1, o2) -> {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return 1;
                } else if (o2 == null) {
                    return -1;
                } else {
                    return o1.getId().compareTo(o2.getId());
                }
            };
    }
}
