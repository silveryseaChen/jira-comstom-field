package com.chy.customfields.multisearcher;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.jql.resolver.NameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by chy on 19/9/30.
 * 根据名称获取 对象
 * 根据 id 获取对象
 *
 */
public class IssueResolver implements NameResolver<Issue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueResolver.class);

    private IssueManager issueManager;

    public IssueResolver(IssueManager issueManager) {
        this.issueManager = issueManager;
    }

    @Override
    public List<String> getIdsFromName(String s) {
        LOGGER.debug( "get ids from name {}", s );
        return Collections.singletonList(s);
    }

    @Override
    public boolean nameExists(String s) {
        LOGGER.debug( "nameExists {}", s );
        return false;
    }

    @Override
    public boolean idExists(Long aLong) {
        LOGGER.debug( "idExists {}", aLong );

        return get(aLong) != null;
    }

    @Override
    public Issue get(Long aLong) {
        LOGGER.debug( "get {}", aLong );
        return issueManager.getIssueObject(aLong);
    }

    @Nonnull
    @Override
    public Collection<Issue> getAll() {
        LOGGER.debug( "get all {}");
        return null;
    }
}
