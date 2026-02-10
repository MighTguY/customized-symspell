# GitHub Actions Setup Guide

This guide will help you set up GitHub Actions for automated builds and Maven Central deployment.

## Prerequisites

1. **Maven Central (Sonatype OSSRH) Account**
   - Sign up at https://issues.sonatype.org/
   - Request access for your groupId (io.github.mightguy)
   - Note your Sonatype username and password

2. **GPG Key for Signing**
   - You need a GPG key to sign your artifacts for Maven Central

## Required GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions → New repository secret

Add the following secrets:

### 1. SONATYPE_USERNAME
- Your Sonatype OSSRH username
- Example: `your-sonatype-username`

### 2. SONATYPE_PASSWORD
- Your Sonatype OSSRH password (or user token - recommended)
- To create a user token: https://oss.sonatype.org/ → Profile → User Token

### 3. GPG_PRIVATE_KEY
Export your GPG private key in ASCII format:

```bash
# List your keys
gpg --list-secret-keys --keyid-format LONG

# Export the private key (replace KEY_ID with your key ID)
gpg --export-secret-keys --armor KEY_ID

# Copy the entire output including:
# -----BEGIN PGP PRIVATE KEY BLOCK-----
# ...
# -----END PGP PRIVATE KEY BLOCK-----
```

Paste the entire exported key (including BEGIN and END lines) into the GitHub secret.

### 4. GPG_PASSPHRASE
- The passphrase for your GPG key
- If your key doesn't have a passphrase, use an empty string

### 5. CODECOV_TOKEN (Optional)
- Sign up at https://codecov.io/
- Add your repository
- Copy the upload token
- This is optional - the build will work without it, but you won't get coverage reports

## Creating a GPG Key (if you don't have one)

```bash
# Generate a new GPG key
gpg --full-generate-key

# Choose:
# - RSA and RSA
# - 4096 bits
# - No expiration (or set your preference)
# - Your name and email (must match your Maven Central account)

# Upload to key server
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

## Workflow Behavior

### On Pull Requests
- Builds the project
- Runs all tests
- Generates coverage reports
- Does NOT deploy

### On Push to develop branch
- Builds the project
- Runs all tests
- Generates coverage reports
- Does NOT deploy

### On Push to master branch
- Builds the project
- Runs all tests
- Calculates a new version (format: X.Y.YYYYMMDDHHMMSS)
- Deploys to Maven Central
- Creates a Git tag
- Creates a GitHub Release with JAR artifacts
- Updates POM to next SNAPSHOT version

## Upgrading from Java 8

The workflow uses Java 11. If you want to stick with Java 8 for compatibility:

Edit `.github/workflows/ci.yml` and change:
```yaml
java-version: '11'
```
to:
```yaml
java-version: '8'
```

However, Java 11 is recommended as Java 8 is EOL.

## Migrating from Travis CI

The old `.travis.yml` file can be removed after confirming GitHub Actions works:

```bash
git rm .travis.yml
git commit -m "Remove Travis CI configuration, migrated to GitHub Actions"
```

## Testing the Workflow

1. Create a feature branch
2. Make a small change
3. Push and create a Pull Request
4. Check the "Actions" tab in GitHub to see the build status

## Troubleshooting

### GPG Signing Fails
- Verify GPG_PRIVATE_KEY secret contains the full key including headers
- Verify GPG_PASSPHRASE is correct
- Make sure the key is uploaded to a public key server

### Maven Central Deployment Fails
- Check SONATYPE_USERNAME and SONATYPE_PASSWORD are correct
- Verify your groupId (io.github.mightguy) is approved in Sonatype
- Check your POM has the required metadata (developers, licenses, scm)

### Version Conflicts
- If deployment fails due to version already exists, the workflow will skip to avoid errors
- You may need to manually fix version numbers

## Support

- GitHub Actions docs: https://docs.github.com/en/actions
- Maven Central guide: https://central.sonatype.org/publish/publish-guide/
- Sonatype OSSRH guide: https://central.sonatype.org/publish/publish-guide/
