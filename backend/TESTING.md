# Backend Testing & Code Coverage Guide

## Overview
This guide explains how to run tests and generate code coverage reports for the Money Transfer backend, with special focus on the encrypted transfer feature.

## Test Structure

### Test Classes

1. **EncryptionUtilTest.java** ([security/EncryptionUtilTest.java](src/test/java/com/fidelity/moneytransfer/security/EncryptionUtilTest.java))
   - Unit tests for AES-256 encryption/decryption
   - 11 test cases covering:
     - String encryption/decryption
     - Long value encryption/decryption
     - Non-deterministic encryption (random IV)
     - Edge cases (empty strings, zero, negative numbers, large values)
     - Special characters and Unicode
     - Error handling for invalid ciphertexts

2. **TransferControllerTest.java** ([controller/TransferControllerTest.java](src/test/java/com/fidelity/moneytransfer/controller/TransferControllerTest.java))
   - Unit tests for transfer endpoints
   - Original tests: 2 (standard transfer endpoint)
   - New tests: 5 (encrypted transfer endpoint)
   - Total: 7 test cases covering:
     - Standard transfer success/validation
     - Encrypted transfer success
     - Invalid encrypted requests (validation)
     - Decryption failures
     - Missing sender account
     - Large amounts

3. **EncryptedTransferE2ETest.java** ([EncryptedTransferE2ETest.java](src/test/java/com/fidelity/moneytransfer/EncryptedTransferE2ETest.java))
   - End-to-end integration tests
   - 6 test cases covering:
     - Full encrypted transfer flow
     - Payload security verification (no plaintext exposure)
     - Large amount handling
     - Concurrent transfers
     - Non-deterministic encryption verification

### Test Coverage

**Total: 24 test cases**

```
├── Unit Tests (13)
│   ├── EncryptionUtil (11)
│   └── TransferController (2 existing)
├── Controller Tests (5 new for encryption)
│   └── TransferController encrypted endpoints
└── Integration Tests (6)
    └── End-to-end encrypted transfer flows
```

## Running Tests

### Run All Tests
```bash
cd backend
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=EncryptionUtilTest
mvn test -Dtest=TransferControllerTest
mvn test -Dtest=EncryptedTransferE2ETest
```

### Run Specific Test Method
```bash
mvn test -Dtest=EncryptionUtilTest#testStringEncryptionDecryption
mvn test -Dtest=TransferControllerTest#transferEncrypted_success
```

### Run Tests with Verbose Output
```bash
mvn test -X
```

## Code Coverage with JaCoCo

### Generate Coverage Report
```bash
mvn test jacoco:report
```

### View Coverage Report
After running the above command, open:
```
target/site/jacoco/index.html
```

In your browser to view:
- Overall coverage percentage
- Line coverage by class
- Branch coverage
- Missed lines and branches

### Coverage Targets

JaCoCo is configured to:
- Run during Maven `test` phase
- Generate report in `target/site/jacoco/`
- Track coverage for all classes in:
  - `com.fidelity.moneytransfer.security.EncryptionUtil`
  - `com.fidelity.moneytransfer.controller.TransferController`
  - `com.fidelity.moneytransfer.service.TransferService`

### Expected Coverage

After all tests pass:

```
EncryptionUtil:
  Line Coverage:   ~95%
  Branch Coverage: ~90%
  
TransferController:
  Line Coverage:   ~85%
  Branch Coverage: ~80%
  
Overall Security Package:
  Line Coverage:   ~92%
```

## Maven Configuration

JaCoCo plugin configured in `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Test Execution Flow

### 1. EncryptionUtilTest Flow
```
setUp() → Test case → assert encryption/decryption works
```

Example (testStringEncryptionDecryption):
```
"sensitive data" 
  → encrypt() 
  → random IV applied
  → AES-256 cipher
  → Base64 encoded ciphertext
  → decrypt()
  → IV extracted
  → AES-256 decipher
  → Base64 decoded
  → "sensitive data" ✓
```

### 2. TransferControllerTest Flow (Encrypted)
```
Setup encrypted request
  → Mock AccountRepository.findByUsername()
  → Mock EncryptionUtil.decryptLong()
  → Mock EncryptionUtil.decrypt()
  → Mock TransferService.transfer()
  → POST /api/v1/transfers/encrypted
  → Controller decrypts fields
  → Controller calls service with decrypted values
  → Verify response status & content
```

### 3. E2E Test Flow
```
Frontend scenario (simulated):
  1. Get toAccountId = 2L from form
  2. Get amount = 1000 from form
  3. Encrypt both using EncryptionUtil
  4. Send encrypted values to backend
  
Backend scenario:
  5. Receive EncryptedTransferRequest
  6. Decrypt toAccountId (2L)
  7. Decrypt amount (1000)
  8. Look up sender account from JWT username
  9. Create TransferRequest with decrypted values
  10. Call TransferService.transfer()
  11. Return TransferResponse with success status
```

## Key Test Scenarios

### Encryption Unit Tests
- ✓ Round-trip encryption/decryption (string, long, BigDecimal)
- ✓ Non-deterministic encryption (different ciphertexts for same value)
- ✓ Edge cases (empty, zero, negative, MAX_VALUE)
- ✓ Unicode & special character support
- ✓ Error handling for corrupted ciphertexts

### Encrypted Transfer Controller Tests
- ✓ Success: Full decryption and transfer
- ✓ Validation: Reject malformed encrypted requests
- ✓ Error handling: Decryption failures
- ✓ Authorization: Account lookup for authenticated user
- ✓ Large amounts: Precision with BigDecimal

### E2E Integration Tests
- ✓ Complete flow: Frontend encryption → Backend decryption → Transfer
- ✓ Security: Verify plaintext doesn't leak in logs/responses
- ✓ Concurrent transfers: Multiple simultaneous encrypted transfers
- ✓ Idempotency: Duplicate transfer prevention via idempotencyKey

## Debugging Tests

### Enable Debug Output
```bash
mvn test -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```

### Run Single Test with Output
```bash
mvn test -Dtest=EncryptionUtilTest#testStringEncryptionDecryption -DfailIfNoTests=false -e
```

### Generate HTML Test Report
```bash
mvn surefire-report:report
```

View at: `target/site/surefire-report.html`

## Continuous Integration

For CI/CD pipelines (GitHub Actions, Jenkins, etc.):

```bash
# Run tests with coverage
mvn clean test jacoco:report

# Check if coverage meets minimum threshold
# (Can be configured with JaCoCo rules in pom.xml)
```

## Expected Test Output

```
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ money-transfer ---
[INFO] Running com.fidelity.moneytransfer.security.EncryptionUtilTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.345 s
[INFO] Running com.fidelity.moneytransfer.controller.TransferControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.234 s
[INFO] Running com.fidelity.moneytransfer.EncryptedTransferE2ETest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.456 s
[INFO] 
[INFO] -------------------------------------------------------
[INFO] Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
[INFO] -------------------------------------------------------
[INFO] 
[INFO] Building report at target/site/jacoco/index.html
```

## Quick Start

1. **Run all tests:**
   ```bash
   mvn test
   ```

2. **Generate coverage report:**
   ```bash
   mvn test jacoco:report
   ```

3. **View coverage:**
   ```bash
   open target/site/jacoco/index.html  # macOS
   start target/site/jacoco/index.html # Windows
   xdg-open target/site/jacoco/index.html # Linux
   ```

4. **Check encryption tests specifically:**
   ```bash
   mvn test -Dtest=EncryptionUtilTest
   ```

## Troubleshooting

### Tests fail with "Account not found"
- Mock `AccountRepository.findByUsername()` in your test setup

### Tests fail with "Decryption failed"
- Ensure `EncryptionUtil` is properly initialized with valid key
- Check that ciphertext is valid Base64 and hasn't been corrupted

### Coverage report not generated
- Verify JaCoCo plugin is in `pom.xml`
- Run `mvn clean test jacoco:report`
- Check `target/site/jacoco/` directory exists

### Tests timeout
- Increase timeout in surefire plugin config
- Check for infinite loops in encryption logic

## References

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
