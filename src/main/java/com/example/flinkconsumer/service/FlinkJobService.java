package com.example.flinkconsumer.service;

import com.example.flinkconsumer.job.DataToOpensearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FlinkJobService {

    @Autowired
    private DataToOpensearch dataToOpensearch;

    public void runFlinkJob() throws Exception {
        dataToOpensearch.main(new String[]{});
    }
}
