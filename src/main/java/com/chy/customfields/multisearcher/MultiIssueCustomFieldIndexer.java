package com.chy.customfields.multisearcher;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by chy on 19/9/10.
 */
public class MultiIssueCustomFieldIndexer extends AbstractCustomFieldIndexer {

    private static final Logger logger = LoggerFactory.getLogger(MultiIssueCustomFieldIndexer.class);

    public MultiIssueCustomFieldIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField customField) {
        super(fieldVisibilityManager, customField);
    }

    @Override
    public void addDocumentFieldsSearchable(Document document, Issue issue) {
        addDoc(document, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    @Override
    public void addDocumentFieldsNotSearchable(Document document, Issue issue) {
        addDoc(document, issue, Field.Index.NO);
    }

    private void addDoc(Document document, Issue issue, Field.Index index) {

        Object value = customField.getValue(issue);

        if(value != null){
            if(value instanceof Collection){
                Collection<Issue> issues = (Collection)value;
                if(!issues.isEmpty()){
                    issues.forEach(issue1 -> {
                        String strVal = customField.getCustomFieldType().getStringFromSingularObject(value);
                        logger.debug(" index {} value {} from issue {}", getDocumentFieldId(), strVal, issue.getKey());
                        document.add(new Field(getDocumentFieldId(), strVal, Field.Store.YES, index));
                    });
                }
            } else {
                String strVal = customField.getCustomFieldType().getStringFromSingularObject(value);
                logger.debug(" index {} value {} from issue {}", getDocumentFieldId(), strVal, issue.getKey());
                document.add(new Field(getDocumentFieldId(), strVal, Field.Store.YES, index));
            }

        }

    }
}
