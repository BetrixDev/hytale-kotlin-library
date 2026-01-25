<#
.SYNOPSIS
    Assembles JAR, creates git tag, and publishes a GitHub release.

.PARAMETER Version
    Optional version override. If not specified, reads from gradle.properties.

.PARAMETER DryRun
    If specified, shows what would be done without making changes.

.EXAMPLE
    .\release.ps1
    .\release.ps1 -Version "0.2.0"
    .\release.ps1 -DryRun
#>

param(
    [string]$Version,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host "`n==> $Message" -ForegroundColor Cyan
}

function Get-VersionFromProperties {
    $content = Get-Content "gradle.properties" -Raw
    if ($content -match 'version=(.+)') {
        return $Matches[1].Trim()
    }
    throw "Could not read version from gradle.properties"
}

function Update-VersionInProperties {
    param([string]$NewVersion)
    $content = Get-Content "gradle.properties" -Raw
    $content = $content -replace 'version=.+', "version=$NewVersion"
    Set-Content "gradle.properties" -Value $content.TrimEnd()
}

function Get-PreviousTag {
    $tags = git tag --sort=-v:refname 2>$null
    if ($tags) {
        return ($tags | Select-Object -First 1)
    }
    return $null
}

function Get-CommitsSinceTag {
    param([string]$Tag)
    
    if ($Tag) {
        $commits = git log "$Tag..HEAD" --pretty=format:"- %s (%h)" --no-merges 2>$null
    } else {
        $commits = git log --pretty=format:"- %s (%h)" --no-merges 2>$null
    }
    
    if ($commits) {
        return $commits -join "`n"
    }
    return $null
}

# Ensure we're in the project root
if (-not (Test-Path "build.gradle.kts")) {
    throw "Must be run from project root (where build.gradle.kts is located)"
}

# Ensure git is clean
$gitStatus = git status --porcelain
if ($gitStatus -and -not $DryRun) {
    throw "Working directory is not clean. Commit or stash changes first."
}

# Determine version
if (-not $Version) {
    $Version = Get-VersionFromProperties
}
$tagName = "v$Version"

Write-Step "Preparing release $tagName"

# Check if tag already exists
$existingTag = git tag -l $tagName
if ($existingTag) {
    throw "Tag $tagName already exists. Use a different version."
}

# Get previous tag and commits
$previousTag = Get-PreviousTag
$commitList = Get-CommitsSinceTag -Tag $previousTag

if ($DryRun) {
    Write-Host "[DRY RUN] Would perform the following:" -ForegroundColor Yellow
    Write-Host "  - Build JAR with version $Version"
    Write-Host "  - Create git tag: $tagName"
    Write-Host "  - Push tag to origin"
    Write-Host "  - Create GitHub release with JAR artifacts"
    if ($previousTag) {
        Write-Host "  - Previous tag: $previousTag"
    } else {
        Write-Host "  - No previous tag found (initial release)"
    }
    if ($commitList) {
        Write-Host "`n  Commits to include:" -ForegroundColor Yellow
        Write-Host $commitList
    }
    exit 0
}

# Build the JAR
Write-Step "Building JAR..."
./gradlew clean build

if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed"
}

# Verify JAR was created
$jarPath = "build/libs/hytale-kotlin-library-$Version.jar"
$sourcesJarPath = "build/libs/hytale-kotlin-library-$Version-sources.jar"
$javadocJarPath = "build/libs/hytale-kotlin-library-$Version-javadoc.jar"

if (-not (Test-Path $jarPath)) {
    # Try to find the actual jar name
    $jars = Get-ChildItem "build/libs/*.jar" -Exclude "*-sources.jar", "*-javadoc.jar"
    if ($jars) {
        $jarPath = $jars[0].FullName
        Write-Host "Found JAR at: $jarPath" -ForegroundColor Yellow
    } else {
        throw "JAR file not found at $jarPath"
    }
}

Write-Step "Creating git tag $tagName..."
git tag -a $tagName -m "Release $Version"

Write-Step "Pushing tag to origin..."
git push origin $tagName

# Create GitHub release using gh CLI
Write-Step "Creating GitHub release..."

# Build changelog section
$changelogSection = ""
if ($commitList) {
    $changelogSection = @"

### What's Changed
$commitList
"@
    if ($previousTag) {
        $changelogSection += "`n`n**Full Changelog**: https://github.com/BetrixDev/hytale-kotlin-library/compare/$previousTag...$tagName"
    }
}

# Build release notes (use single quotes to avoid backtick issues)
$releaseNotes = @"
## Hytale.kt $Version
$changelogSection

### Installation via JitPack

Add the JitPack repository and dependency to your build.gradle.kts:

``````kotlin
repositories {
    mavenCentral()
    maven('https://jitpack.io')
}

dependencies {
    implementation('com.github.BetrixDev:hytale-kotlin-library:$tagName')
}
``````

Or with Gradle Groovy DSL:

``````groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.BetrixDev:hytale-kotlin-library:$tagName'
}
``````

Or with Maven:

``````xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.BetrixDev</groupId>
    <artifactId>hytale-kotlin-library</artifactId>
    <version>$tagName</version>
</dependency>
``````

### Assets
- hytale-kotlin-library-$Version.jar - Main library
- hytale-kotlin-library-$Version-sources.jar - Source code
- hytale-kotlin-library-$Version-javadoc.jar - Documentation
"@

# Collect all JARs to upload
$assets = @()
if (Test-Path $jarPath) { $assets += $jarPath }
if (Test-Path $sourcesJarPath) { $assets += $sourcesJarPath }
if (Test-Path $javadocJarPath) { $assets += $javadocJarPath }

# Use array splatting to pass arguments properly
$ghArgs = @(
    "release", "create", $tagName,
    "--title", $tagName,
    "--notes", $releaseNotes
)
$ghArgs += $assets

Write-Host "Creating release with $($assets.Count) assets..." -ForegroundColor Gray
& gh @ghArgs

if ($LASTEXITCODE -ne 0) {
    throw "Failed to create GitHub release"
}

Write-Step "Release $tagName created successfully!"
Write-Host "`nView release at: " -NoNewline
gh release view $tagName --web