package io.mateu.mdd.tester.model.useCases.hotel;

import io.mateu.mdd.core.annotations.UseCheckboxes;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @ManyToOne
    private Hotel hotel;


    private String name;


    @ManyToMany
    @UseCheckboxes
    private Set<Contract> contracts = new HashSet<>();


    @Override
    public boolean equals(Object obj) {
        return this == obj || id == ((Offer)obj).getId();
    }
}
