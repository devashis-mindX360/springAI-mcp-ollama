$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = 'C:\Users\Devashis Kumar\.jdks\ms-21.0.10\bin\java.exe'
$psi.Arguments = '-jar D:\MindX360\MCP\springAI-mcp\target\springAI-mcp-1.0.0.jar'
$psi.UseShellExecute = $false
$psi.RedirectStandardInput = $true
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$p = [System.Diagnostics.Process]::Start($psi)

Write-Host "Process started, PID: $($p.Id). Waiting 5s for startup..."
Start-Sleep -Seconds 5

$msg = '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
Write-Host "Sending: $msg"
$p.StandardInput.WriteLine($msg)
$p.StandardInput.Flush()

Write-Host "Waiting for response..."
$task = $p.StandardOutput.ReadLineAsync()
if ($task.Wait(8000)) {
    Write-Host "RESPONSE: $($task.Result)"
} else {
    Write-Host "TIMEOUT - no response received in 8 seconds"
}

try { $p.Kill(); Write-Host "Process killed." } catch { Write-Host "Process already exited." }
