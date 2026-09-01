$root = $PSScriptRoot
$sep = [char]92
$sources = Get-ChildItem "$root\src" -Recurse -Filter *.java |
        ForEach-Object { '"' + $_.FullName.Replace($sep, '/') + '"' }
New-Item -ItemType Directory -Force -Path "$root\out" | Out-Null
[System.IO.File]::WriteAllLines("$root\out\sources.txt", $sources)
& javac -encoding UTF-8 -cp "$root/lib/*" -d "$root/out/classes" "@$root/out/sources.txt"
if ($LASTEXITCODE -ne 0) {
    Write-Host "compilation failed" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "compiled $($sources.Count) sources" -ForegroundColor Green
