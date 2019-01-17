## QrCodeScanner

###How to import?

1. Root level

```groovy
allprojects {
    repositories {
    ...
    maven {
        url "https://jitpack.io"
        credentials { username 'jp_7r2folubajc8v60rj90nrhsc83' }
    }
    ...
```

2. App level

```groovy
dependencies {
    // Scanner (view)
    implementation "org.bitbucket.NikitaGordiaNoisy.qrcodescanner:qrscanner:1.1.0"
    
    // Bitmap generator
    implementation "org.bitbucket.NikitaGordiaNoisy.qrcodescanner:qrgenerator:1.1.0"
}
```