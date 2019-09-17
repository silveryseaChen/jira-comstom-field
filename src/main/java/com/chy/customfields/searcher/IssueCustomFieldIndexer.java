package com.chy.customfields.searcher;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chy on 19/9/10.
 */
public class IssueCustomFieldIndexer extends AbstractCustomFieldIndexer {

    private static final Logger logger = LoggerFactory.getLogger(IssueCustomFieldIndexer.class);

    public IssueCustomFieldIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField customField) {
        super(fieldVisibilityManager, customField);
    }

    @Override
    public void addDocumentFieldsSearchable(Document document, Issue issue) {
        addDoc(document, issue, Field.Index.ANALYZED);
    }

    @Override
    public void addDocumentFieldsNotSearchable(Document document, Issue issue) {
        addDoc(document, issue, Field.Index.NO);
    }

    private void addDoc(Document document, Issue issue, Field.Index index) {

        Object value = customField.getValue(issue);

        if(value != null){
            String strVal = customField.getCustomFieldType().getStringFromSingularObject(value);
            logger.debug(" index {} value {} from issue {}", getDocumentFieldId(), strVal, issue.getKey());
            document.add(new Field(getDocumentFieldId(), strVal, Field.Store.YES, index));
        }

    }
}
