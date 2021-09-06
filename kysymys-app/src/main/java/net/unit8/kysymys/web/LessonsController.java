package net.unit8.kysymys.web;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/lesson")
public class LessonsController {
    public String list() {
        return "lesson/list";
    }
}
