package io.mateu.showcase.tester.model.useCases.showcase;

import javax.persistence.GeneratedValue;
import lombok.MateuMDDEntity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;

@MateuMDDEntity
public class UnoAUno {

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty
    private String nombre;

}
