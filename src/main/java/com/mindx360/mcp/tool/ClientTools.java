package com.mindx360.mcp.tool;

import com.mindx360.mcp.entity.Client;
import com.mindx360.mcp.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ClientTools {

    private final ClientService service;

    @Tool(description="Get all clients")
    public List<Client> getAllClients(){
        return service.getAllClients();
    }
}