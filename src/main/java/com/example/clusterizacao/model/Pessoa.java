package com.example.clusterizacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pessoa {
    private Long id;
    private String nome;
    private int idade;
    private double salario;
    private int escolaridade;
    private String profissao;
    private boolean centroide;
    private Long clusterId;
}
