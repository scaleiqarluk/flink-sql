package com.example.flinkconsumer.controller;

import com.example.flinkconsumer.service.FlinkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opensearch/api/")
public class FlinkSQLController {

    @Autowired
    private FlinkJobService service;

    @GetMapping("/sqlQuery")
    public void sqlQueryCall() throws Exception {
        service.runFlinkSQLOpensearchJob();
    }

    @GetMapping("/dataToOpensearch")
    public void dataToOpensearchCall() throws Exception {
//        service.runDataToOpensearchJob();
    }
}
