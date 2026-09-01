$root = $PSScriptRoot
& "$root\build.ps1"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
Set-Location $root
& java -cp "$root/out/classes;$root/lib/*" ir.ac.pvz.DesktopLauncher
