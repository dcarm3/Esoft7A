package com.example.clusterizacao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pessoa {
    private Long id;
    private String nome;
    private int idade;
    private double salario;
    private int escolaridade; // 1=fundamental, 2=médio, 3=superior...
    private boolean isCentroide;
}
