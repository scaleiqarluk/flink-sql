package com.example.flinkconsumer.service;

import com.example.flinkconsumer.job.DataToOpensearch;
import com.example.flinkconsumer.job.FlinkSQLOpensearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class FlinkJobService {

    @Autowired
    private DataToOpensearch dataToOpensearch;

    @Autowired
    private FlinkSQLOpensearch flinkSQLOpensearch;

    @Async
    public void runDataToOpensearchJob(String topic) throws Exception {
        try {
            dataToOpensearch.jobSubmitter(topic);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    public void runFlinkSQLOpensearchJob() throws Exception {
        flinkSQLOpensearch.main(new String[]{});
    }
}
