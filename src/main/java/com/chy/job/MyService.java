package com.chy.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by chy on 19/11/14.
 */
@Service
public class MyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyService.class);

    public void execute(){
        LOGGER.info("================execute service method===================");
    }

}
