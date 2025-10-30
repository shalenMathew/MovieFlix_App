# Build Requirements

## Java/JDK Version
This project requires **Java 17** (JDK 17).

## Automatic Toolchain Provisioning
The project is configured with **Gradle Toolchain Auto-Provisioning** using the Foojay Resolver plugin. This means:

- ✅ **You don't need to manually install Java 17** on your machine
- ✅ Gradle will automatically download the correct JDK version when building
- ✅ Works across different operating systems (Windows, macOS, Linux)
- ✅ Ensures consistent build environment for all contributors

### How It Works
When you run any Gradle build command, Gradle will:
1. Detect that Java 17 is required
2. Check if it's already available locally
3. If not found, automatically download it from the Foojay repository
4. Use the downloaded JDK for compilation

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### First Build
The first build may take longer as Gradle downloads the JDK. Subsequent builds will be faster as the JDK is cached locally.

### Manual Java Installation (Optional)
If you prefer to use a manually installed Java 17, ensure it's properly configured in your system PATH and JAVA_HOME environment variable.

### Supported Java Vendors
The toolchain will work with any Java 17 implementation:
- Oracle JDK 17
- OpenJDK 17
- Azul Zulu 17
- Amazon Corretto 17
- Eclipse Temurin 17

## Configuration Files
- **settings.gradle.kts** - Contains toolchain resolver configuration
- **app/build.gradle.kts** - Specifies Java 17 requirement (`jvmToolchain(17)`)

## Troubleshooting

### Build Fails with Toolchain Error
If you still get toolchain errors:
1. Delete `.gradle` folder in project root
2. Run: `./gradlew --stop`
3. Run: `./gradlew clean build --refresh-dependencies`

### Manually Specify JDK Path
If needed, you can specify a JDK path in `gradle.properties`:
```properties
org.gradle.java.home=/path/to/your/jdk-17
```

### Check Gradle Version
Ensure you're using Gradle 7.6 or higher:
```bash
./gradlew --version
```

## For Contributors
No special setup needed! Just clone and build. Gradle handles the JDK automatically.
