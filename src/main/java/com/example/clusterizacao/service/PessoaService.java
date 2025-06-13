package com.example.clusterizacao.service;

import com.example.clusterizacao.model.Pessoa;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PessoaService {

    private final Map<Long, Pessoa> pessoas = new HashMap<>();
    private long nextId = 1;

    public PessoaService() {
        adicionarPessoa(new Pessoa(null, "João", 25, 3000.0, 2, true, null));
        adicionarPessoa(new Pessoa(null, "Maria", 45, 8000.0, 3, true, null));
    }

    public Pessoa adicionarPessoa(Pessoa pessoa) {
        pessoa.setId(nextId++);

        if (!pessoa.isCentroide()) {
            Pessoa centroideProximo = encontrarCentroideMaisProximo(pessoa);
            if (centroideProximo != null) {
                pessoa.setClusterId(centroideProximo.getId());
            }
        } else {
            pessoa.setClusterId(pessoa.getId());
        }

        pessoas.put(pessoa.getId(), pessoa);

        atualizarCentroide(pessoa.getClusterId());

        reorganizarClusters();

        return pessoa;
    }

    public Pessoa atualizarPessoa(Long id, Pessoa novaPessoa) {
        if (pessoas.containsKey(id)) {
            novaPessoa.setId(id);
            pessoas.put(id, novaPessoa);
            return novaPessoa;
        }
        return null;
    }

    public boolean removerPessoa(Long id) {
        return pessoas.remove(id) != null;
    }

    public List<Pessoa> listarPessoas() {
        return new ArrayList<>(pessoas.values());
    }

    public Pessoa obterPorId(Long id) {
        return pessoas.get(id);
    }

    private double calcularDistancia(Pessoa p1, Pessoa p2) {
        double idade = p1.getIdade() - p2.getIdade();
        double salario = p1.getSalario() - p2.getSalario();
        double escolaridade = p1.getEscolaridade() - p2.getEscolaridade();
        return Math.sqrt(idade * idade + salario * salario + escolaridade * escolaridade);
    }

    private Pessoa encontrarCentroideMaisProximo(Pessoa pessoa) {
        return pessoas.values().stream()
                .filter(Pessoa::isCentroide)
                .min(Comparator.comparingDouble(c -> calcularDistancia(pessoa, c)))
                .orElse(null);
    }

    public Pessoa calcularCentroideVirtual(Long clusterId) {
        List<Pessoa> membros = pessoas.values().stream()
                .filter(p -> clusterId.equals(p.getClusterId()) && !p.isCentroide())
                .toList();

        if (membros.isEmpty()) return null;

        double mediaIdade = membros.stream().mapToInt(Pessoa::getIdade).average().orElse(0);
        double mediaSalario = membros.stream().mapToDouble(Pessoa::getSalario).average().orElse(0);
        double mediaEscolaridade = membros.stream().mapToInt(Pessoa::getEscolaridade).average().orElse(0);

        return new Pessoa(
                null,
                "Centróide Virtual",
                (int) mediaIdade,
                mediaSalario,
                (int) mediaEscolaridade,
                true,
                clusterId
        );
    }

    public void atualizarCentroide(Long clusterId) {
        Pessoa novoCentroide = calcularCentroideVirtual(clusterId);
        if (novoCentroide == null) return;

        pessoas.values().removeIf(p -> p.isCentroide() && p.getId().equals(clusterId));

        novoCentroide.setId(clusterId);
        pessoas.put(clusterId, novoCentroide);
    }

    public void reorganizarClusters() {
        for (Pessoa pessoa : pessoas.values()) {
            if (!pessoa.isCentroide()) {
                Pessoa novoCentroide = encontrarCentroideMaisProximo(pessoa);
                if (novoCentroide != null) {
                    pessoa.setClusterId(novoCentroide.getId());
                }
            }
        }
    }

    public List<Pessoa> listarCentroideVirtuais() {
        Set<Long> clusterIds = pessoas.values().stream()
                .filter(Pessoa::isCentroide)
                .map(Pessoa::getId)
                .collect(Collectors.toSet());

        return clusterIds.stream()
                .map(this::calcularCentroideVirtual)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
