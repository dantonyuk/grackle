package com.github.hyla.grackle.web;

import com.github.hyla.grackle.query.AuthorQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AuthorQuery query;

    @GetMapping
    public String remoteCall() {
        query.nameIs("test");
        return "OK";
    }
}
