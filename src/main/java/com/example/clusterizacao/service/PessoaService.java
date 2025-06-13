package com.example.clusterizacao.service;

import com.example.clusterizacao.model.Pessoa;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PessoaService {

    private final Map<Long, Pessoa> pessoas = new HashMap<>();
    private final Map<String, Integer> profissaoMap = new HashMap<>();
    private int profissaoIndex = 0;
    private long nextId = 1;
    private static final double LIMIAR_DISTANCIA = 30.0;

    public PessoaService() {
        adicionarPessoa(new Pessoa(null, "João", 25, 3000.0, 2, "Engenheiro", true, null));
        adicionarPessoa(new Pessoa(null, "Maria", 45, 8000.0, 3, "Professora", true, null));
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
        analisarDispersao();

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

    // Conversão de dados categóricos
    private Pessoa obterPessoaNumerica(Pessoa pessoa) {
        int profissaoCode = profissaoMap.computeIfAbsent(
                pessoa.getProfissao() == null ? "Desconhecido" : pessoa.getProfissao(),
                key -> profissaoIndex++
        );

        return new Pessoa(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getIdade(),
                pessoa.getSalario(),
                pessoa.getEscolaridade(),
                String.valueOf(profissaoCode),
                pessoa.isCentroide(),
                pessoa.getClusterId()
        );
    }

    private double calcularDistancia(Pessoa p1, Pessoa p2) {
        Pessoa np1 = obterPessoaNumerica(p1);
        Pessoa np2 = obterPessoaNumerica(p2);

        double idade = np1.getIdade() - np2.getIdade();
        double salario = np1.getSalario() - np2.getSalario();
        double escolaridade = np1.getEscolaridade() - np2.getEscolaridade();
        double profissao = Double.parseDouble(np1.getProfissao()) - Double.parseDouble(np2.getProfissao());

        return Math.sqrt(idade * idade + salario * salario + escolaridade * escolaridade + profissao * profissao);
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

        return new Pessoa(null, "Centróide Virtual", (int) mediaIdade, mediaSalario, (int) mediaEscolaridade, "Desconhecido", true, clusterId);
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

        Set<Long> clusterIds = pessoas.values().stream()
                .filter(Pessoa::isCentroide)
                .map(Pessoa::getId)
                .collect(Collectors.toSet());

        for (Long clusterId : clusterIds) {
            atualizarCentroide(clusterId);
        }
    }

    public void analisarDispersao() {
        List<Pessoa> candidatos = new ArrayList<>();

        for (Pessoa pessoa : pessoas.values()) {
            if (!pessoa.isCentroide()) {
                Pessoa centroideAtual = pessoas.get(pessoa.getClusterId());
                double distAtual = calcularDistancia(pessoa, centroideAtual);

                Pessoa centroideMaisProximo = encontrarCentroideMaisProximo(pessoa);
                double distMaisProx = calcularDistancia(pessoa, centroideMaisProximo);

                if (distAtual > LIMIAR_DISTANCIA && centroideMaisProximo != centroideAtual) {
                    candidatos.add(pessoa);
                }
            }
        }

        for (Pessoa p : candidatos) {
            Pessoa novoCentroide = new Pessoa(nextId++, p.getNome() + " Cluster", p.getIdade(),
                    p.getSalario(), p.getEscolaridade(), p.getProfissao(), true, null);
            novoCentroide.setClusterId(novoCentroide.getId());

            pessoas.put(novoCentroide.getId(), novoCentroide);
            p.setClusterId(novoCentroide.getId());
        }

        reorganizarClusters();
    }
}
