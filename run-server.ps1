param(
    [int]$Port = 7777
)

$root = $PSScriptRoot
& "$root\build.ps1"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
Set-Location $root
& java -cp "$root/out/classes;$root/lib/*" network.server.ServerMain $Port
