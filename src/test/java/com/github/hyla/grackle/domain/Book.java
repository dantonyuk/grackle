package com.github.hyla.grackle.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Book {

    @Id
    private Long id;
    @Column
    private String title;
    @Column
    private int rating;
    @Column
    private String articleNumber;
    @ManyToOne
    private Author author;
}
