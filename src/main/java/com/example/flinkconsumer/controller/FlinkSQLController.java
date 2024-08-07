package com.example.flinkconsumer.controller;

import com.example.flinkconsumer.service.FlinkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Boolean> dataToOpensearchCall(String topic) throws Exception {
        service.runDataToOpensearchJob(topic);
        return ResponseEntity.ok(true);
    }
}
