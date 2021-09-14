package net.unit8.kysymys.web;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentForm implements Serializable {
    private String description;
}
