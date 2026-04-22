param(
    [string]$EnvFile = "ai.env"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$envPath = Join-Path $scriptDir $EnvFile
if (Test-Path $envPath) {
    Write-Host "[run-with-ai] Loading environment variables from $EnvFile"
    foreach ($line in Get-Content -Path $envPath) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith("#")) {
            continue
        }

        $parts = $trimmed.Split("=", 2)
        if ($parts.Count -ne 2) {
            continue
        }

        $key = $parts[0].Trim()
        $value = $parts[1].Trim()
        if ([string]::IsNullOrWhiteSpace($key)) {
            continue
        }

        [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
    }
} else {
    Write-Host "[run-with-ai] $EnvFile not found, continue without AI env file."
}

if (-not (Test-Path ".mvn\wrapper\maven-wrapper.jar")) {
    throw "Missing .mvn\wrapper\maven-wrapper.jar. Run: mvn -N wrapper:wrapper"
}

if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    throw "JAVA_HOME is not set. Please set JAVA_HOME first."
}

$javaExe = Join-Path $env:JAVA_HOME "bin\java.exe"
if (-not (Test-Path $javaExe)) {
    throw "JAVA_HOME seems invalid: $javaExe not found."
}

& $javaExe `
    -classpath ".mvn\wrapper\maven-wrapper.jar" `
    "-Dmaven.multiModuleProjectDirectory=$PWD" `
    org.apache.maven.wrapper.MavenWrapperMain clean package cargo:run
