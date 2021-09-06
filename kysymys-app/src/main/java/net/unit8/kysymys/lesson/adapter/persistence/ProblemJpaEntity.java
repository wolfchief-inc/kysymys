package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "problem")
@Table(name ="problems")
@Data
public class ProblemJpaEntity {
    @Id
    private String id;

    @Column(name = "repository_url")
    private String repositoryUrl;

    @Column(name = "branch")
    private String branch;

    @Column(name = "runner")
    private String runner;
}
