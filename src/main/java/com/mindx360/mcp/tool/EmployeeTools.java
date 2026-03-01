package com.mindx360.mcp.tool;

import com.mindx360.mcp.entity.Employee;
import com.mindx360.mcp.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Spring AI MCP Tool class – exposes HR business operations to Claude Desktop.
 *
 * <h2>How Spring AI handles this class:</h2>
 * <ol>
 *   <li>The {@link org.springframework.ai.tool.MethodToolCallbackProvider} bean
 *       (declared in {@code McpConfig}) wraps every {@code @Tool}-annotated
 *       method in a {@code ToolCallback}.</li>
 *   <li>Spring AI MCP Server auto-configures a {@code McpSyncServer} and
 *       registers all {@code ToolCallback} instances from every
 *       {@code ToolCallbackProvider} bean found in the context.</li>
 *   <li>On startup the server sends a {@code tools/list} response to Claude
 *       containing the method names, descriptions, and auto-generated
 *       JSON Schemas derived from the method signatures.</li>
 *   <li>When Claude decides to invoke a tool it sends a {@code tools/call}
 *       message; Spring AI deserialises the arguments and delegates to
 *       the matching method – all transparently.</li>
 * </ol>
 *
 * <p><strong>No manual JSON-RPC, STDIO, or schema code is written here.</strong>
 */
@Component
@RequiredArgsConstructor
public class EmployeeTools {

    private final EmployeeService service;

    @Tool(description="Get all employees")
    public List<Employee> getAllEmployees(){
        return service.getAllEmployees();
    }

    @Tool(description="Get employee by ID")
    public Employee getEmployeeById(
            @ToolParam(description="Employee ID") Integer id){
        return service.getEmployeeById(id);
    }

    @Tool(description="Get employees by department")
    public List<Employee> getEmployeesByDepartment(
            @ToolParam(description="Department name") String dept){
        return service.getEmployeesByDepartment(dept);
    }

    @Tool(description="Search employees by name")
    public List<Employee> searchEmployees(
            @ToolParam(description="Partial name") String keyword){
        return service.searchEmployees(keyword);
    }

    @Tool(description="Get employees by status Active/Inactive")
    public List<Employee> getEmployeesByStatus(
            @ToolParam(description="Status") String status){
        return service.getEmployeesByStatus(status);
    }

    @Tool(description="Get total employee count")
    public long getTotalEmployeeCount(){
        return service.getEmployeeCount();
    }
}
