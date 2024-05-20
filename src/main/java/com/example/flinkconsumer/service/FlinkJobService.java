package com.example.flinkconsumer.service;

import com.example.flinkconsumer.job.DataToOpensearch;
import com.example.flinkconsumer.job.FlinkSQLOpensearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FlinkJobService {

    @Autowired
    private DataToOpensearch dataToOpensearch;

    @Autowired
    private FlinkSQLOpensearch flinkSQLOpensearch;

    public void runDataToOpensearchJob() throws Exception {
        dataToOpensearch.main(new String[]{});
    }

    public void runFlinkSQLOpensearchJob() throws Exception {
        flinkSQLOpensearch.main(new String[]{});
    }
}
