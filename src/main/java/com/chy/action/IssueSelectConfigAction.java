package com.chy.action;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction;
import com.atlassian.jira.web.action.admin.customfields.EditCustomFieldDefaults;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chy on 19/9/18.
 */
@WebSudoRequired
@Scanned
public class IssueSelectConfigAction extends AbstractEditConfigurationItemAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueSelectConfigAction.class);

    private String issueType;
    private Options options;
    @ComponentImport
    private OptionsManager optionsManager;
    @ComponentImport
    private ManagedConfigurationItemService managedConfigurationItemService;
    @ComponentImport
    private ConstantsManager constantsManager;

    private Map customFieldValuesHolder = new HashMap();

    @Inject
    public IssueSelectConfigAction(ManagedConfigurationItemService managedConfigurationItemService,
                                   OptionsManager optionsManager, ConstantsManager constantsManager) {
        super(managedConfigurationItemService);
        this.optionsManager = optionsManager;
        this.constantsManager = constantsManager;
    }

    @Override
    public String doDefault() throws Exception {

        LOGGER.debug("IssueSelectConfigAction.doDefault");

        //初始化IssueType
        options = this.optionsManager.getOptions(this.getFieldConfig());
        LOGGER.debug("options is {}", options);
        Option option = null;
        if(!options.isEmpty() && (option = options.get(0)) != null){
            this.issueType = option.getValue();
        }

        EditCustomFieldDefaults.populateDefaults(this.getFieldConfig(), this.customFieldValuesHolder);

        return super.doDefault();
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception {
        LOGGER.debug("IssueSelectConfigAction.doExecute");
        LOGGER.debug("issueType is {}", this.issueType);
        LOGGER.debug("fieldConfig is {}", this.getFieldConfig().getId());
        options = this.optionsManager.getOptions(this.getFieldConfig());
        LOGGER.debug("options is {}", options);
        Option option = null;
        if(options.isEmpty() || (option = options.get(0)) == null){
            options.clear();
            options.addOption(null, issueType);
        } else {
            option = options.get(0);
            options.setValue(option, issueType);
        }

        StringBuilder redirectUrl = (new StringBuilder("IssueSelectConfigAction!default.jspa?fieldConfigId=")).append(this.getFieldConfigId());

        return this.getRedirect(redirectUrl.toString());

    }

    public String getIssueType() {

        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public List<IssueConstant> getIssueTypes(){
        return constantsManager.getConstantsByIds(ConstantsManager.CONSTANT_TYPE.ISSUE_TYPE, constantsManager.getAllIssueTypeIds());
    }

    public Long getCustomFieldId(){
        return this.getCustomField().getIdAsLong();
    }
}
