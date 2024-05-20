package com.example.flinkconsumer.controller;

import com.example.flinkconsumer.service.FlinkJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlinkSQLController {

    @Autowired
    private FlinkJobService service;

    @GetMapping("/sql")
    public void sqlCall() throws Exception {
        service.runFlinkSQLOpensearchJob();
    }
}
