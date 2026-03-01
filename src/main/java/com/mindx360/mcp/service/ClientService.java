package com.mindx360.mcp.service;

import com.mindx360.mcp.entity.Client;
import com.mindx360.mcp.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository repo;

    public List<Client> getAllClients() {
        return repo.findAll();
    }
}