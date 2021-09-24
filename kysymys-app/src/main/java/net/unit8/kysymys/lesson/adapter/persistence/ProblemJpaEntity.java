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
    @Column(name = "id", length = 21)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "repository_url", nullable = false)
    private String repositoryUrl;

    @Column(name = "branch", nullable = false)
    private String branch;

    @Column(name = "readme_path")
    private String readmePath;

    @Column(name = "runner")
    private String runner;
}
