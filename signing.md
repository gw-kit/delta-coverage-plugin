# Signing

To publish a library to Maven Central, it is required to sign the artifacts with a GPG key.

1. Generate a new GPG key
```bash
gpg --full-generate-key
```
> RSA sign only 
> size: 3072
> expiration: 4y (4 years)

Consider the entered password as `<YOUR_PASSPHRASE>`

2. List all keys and get the key id
```bash
gpg --list-keys --keyid-format short
```
> Copy the key id (8 characters) from the output
> consider next as <YOUR_KEY_ID>

3. Publish the key
```bash
gpg --keyserver hkps://keys.openpgp.org --send-keys <YOUR_KEY_ID>
```

4. Export the private key for signing
```bash
# ASCII armored
gpg --armor --export-secret-key <YOUR_KEY_ID>
# Consider next as SIGNING_KEY
```

5. Add the data to `~/.gradle/gradle.properties`
```properties
signing.keyId=<YOUR_KEY_ID>
signing.password=<YOUR_PASSPHRASE>
# Set <SIGNING_KEY>:
signing.secretKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n\
\n\
next-key-lines\n\
...\n\
-----END PGP PRIVATE KEY BLOCK-----
```

6. Setup Gradle signing plugin
```kts
plugins {
    signing
}

signing {
    val keyId = "<YOUR_KEY_ID>" // read from property 'signing.keyId'
    val secretKey = "<SIGNING_KEY>" // read from property 'signing.secretKey'
    val signingPassword = "<YOUR_PASSPHRASE>" // read from property 'signing.password'
    seInMemoryPgpKeys(keyId, secretKey, signingPassword)
}
```
