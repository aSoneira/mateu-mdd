package io.mateu.mdd.tester.app.erp;

import lombok.MateuMDDEntity;

import javax.validation.constraints.NotBlank;

@MateuMDDEntity
public class Grupo {

    @NotBlank
    private String name;

}
