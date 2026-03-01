$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = 'C:\Users\Devashis Kumar\.jdks\ms-21.0.10\bin\java.exe'
$psi.Arguments = '-jar D:\MindX360\MCP\springAI-mcp\target\springAI-mcp-1.0.0.jar'
$psi.UseShellExecute = $false
$psi.RedirectStandardInput = $true
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$p = [System.Diagnostics.Process]::Start($psi)

Write-Host "Process started, PID: $($p.Id). Waiting 6s for startup..."
Start-Sleep -Seconds 6

# Step 1: Send initialize
$init = '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
Write-Host "`n=== STEP 1: initialize ==="
Write-Host "SENDING: $init"
$p.StandardInput.WriteLine($init)
$p.StandardInput.Flush()

$task1 = $p.StandardOutput.ReadLineAsync()
if ($task1.Wait(8000)) {
    Write-Host "RESPONSE: $($task1.Result)"
} else {
    Write-Host "TIMEOUT"
}

# Step 2: Send initialized notification
$notif = '{"jsonrpc":"2.0","method":"notifications/initialized"}'
Write-Host "`n=== STEP 2: initialized notification ==="
Write-Host "SENDING: $notif"
$p.StandardInput.WriteLine($notif)
$p.StandardInput.Flush()
Start-Sleep -Seconds 1

# Step 3: List tools
$toolsList = '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}'
Write-Host "`n=== STEP 3: tools/list ==="
Write-Host "SENDING: $toolsList"
$p.StandardInput.WriteLine($toolsList)
$p.StandardInput.Flush()

$task2 = $p.StandardOutput.ReadLineAsync()
if ($task2.Wait(8000)) {
    $response = $task2.Result
    Write-Host "RESPONSE LENGTH: $($response.Length) chars"
    # Pretty print tool names
    if ($response -match '"tools"') {
        Write-Host "FULL RESPONSE (truncated):"
        Write-Host $response.Substring(0, [Math]::Min(2000, $response.Length))
        if ($response.Length -gt 2000) { Write-Host "... (truncated)" }
    } else {
        Write-Host "RESPONSE: $response"
    }
} else {
    Write-Host "TIMEOUT"
}

try { $p.Kill(); Write-Host "`nProcess killed." } catch { Write-Host "`nProcess already exited." }
