package com.example.clusterizacao.service;

import com.example.clusterizacao.model.Pessoa;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PessoaService {
    private final Map<Long, Pessoa> pessoas = new HashMap<>();
    private long nextId = 1;

    public PessoaService() {
        adicionarPessoa(new Pessoa(null, "João", 25, 3000.0, 2, true));
        adicionarPessoa(new Pessoa(null, "Maria", 45, 8000.0, 3, true));
    }

    public Pessoa adicionarPessoa(Pessoa pessoa) {
        pessoa.setId(nextId++);
        pessoas.put(pessoa.getId(), pessoa);
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
}
