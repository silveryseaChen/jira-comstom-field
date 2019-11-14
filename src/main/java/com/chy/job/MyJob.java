package com.chy.job;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.*;
import com.atlassian.scheduler.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;

/**
 * jira job
 * Created by chy on 19/11/14.
 */
@Component
public class MyJob implements JobRunner,DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyJob.class);

    private static final JobRunnerKey JOB_KEY = JobRunnerKey.of(MyJob.class.getName());
    private static final JobId JOB_ID = JobId.of(MyJob.class.getName());

    @ComponentImport
    private UserManager userManager;
    @ComponentImport
    private SchedulerService schedulerService;
    @Autowired
    private MyService myService;

    @Inject
    public MyJob(UserManager userManager,
                 SchedulerService schedulerService){

        this.userManager = userManager;
        this.schedulerService = schedulerService;

        registerJob();

    }

    private void registerJob(){

        schedulerService.registerJobRunner(JOB_KEY, this);
        JobConfig jobConfig = JobConfig.forJobRunnerKey(JOB_KEY)
                .withRunMode(RunMode.RUN_LOCALLY)
                .withSchedule(Schedule.forCronExpression("0 * * * * ?"));
        try {
            schedulerService.scheduleJob(JOB_ID, jobConfig);
        } catch (SchedulerServiceException e) {
            LOGGER.error("my job schedule error", e);
        }
    }

    @Nullable
    @Override
    public JobRunnerResponse runJob(JobRunnerRequest request) {

        Collection<ApplicationUser> users = userManager.getUsers();
        users.stream().forEach(u -> LOGGER.info(u.getName()));

        myService.execute();

        return JobRunnerResponse.success();
    }

    @Override
    public void destroy() throws Exception {
        schedulerService.unscheduleJob(JOB_ID);
        schedulerService.unregisterJobRunner(JOB_KEY);
    }

}
