# SECURITY RUNBOOK

#### KEK Keystore (PKCS#12) Management

---

## 1. Scope and intent

This runbook governs the lifecycle of the **Key Encryption Key (KEK)** used to protect **Data Encryption Keys (DEKs)**.

This keystore:

- Stores **only symmetric AES-256 KEKs**
- Is required to decrypt any stored document
- Is a **Tier-0 security asset**

Loss or compromise of this keystore results in **irreversible data loss** or **system-wide exposure**.

---

## 2. Cryptographic policy (non-negotiable)

- Algorithm: **AES-256**
- Mode: **AES-GCM (via DEKs only)**
- KEK usage: **encrypt / decrypt DEKs only**
- Keystore format: **PKCS#12**
- One **active KEK** at any time
- Old KEKs retained until migration is complete

---

## 3. Prerequisites

- Java 11+ (`keytool`)
- Secure workstation (offline preferred)
- Secrets vault or password manager
- Change ticket / approval for KEK operations

---

## 4. Keystore creation (initial provisioning)

### 4.1 Generate keystore password

- Minimum 32 characters
- Randomly generated
- Stored in:
    - Vault / KMS / Secret Manager
- **Never written to disk or source code**

Example:

~~~
KEK_STORE_PASSWORD = <random-32+>
~~~

### 4.2 Create PKCS#12 keystore and initial KEK

```shell
keytool -genseckey \
  -alias kek-YYYY-MM \
  -keyalg AES \
  -keysize 256 \
  -keystore kek.p12 \
  -storetype PKCS12 \
  -storepass <KEK_STORE_PASSWORD> \
  -keypass <KEK_STORE_PASSWORD>
```

Example alias:

~~~
kek-2026-01
~~~

Result:

- File `kek.p12`
- One AES-256 KEK

### 4.3 Verify keystore contents

```shell
keytool -list -v \
  -keystore kek.p12 \
  -storetype PKCS12 \
  -storepass <KEK_STORE_PASSWORD>
```

Verify:

- Entry type: `SecretKeyEntry`
- Algorithm: AES
- Key size: 256 bits

---

## 5. Secure keystore storage

### 5.1 Filesystem location

Recommended:

```shell
/opt/app/secrets/kek.p12
```

Permissions:

```shell
chown appuser:appuser kek.p12
chmod 400 kek.p12
```

Rules:

- Read-only
- No group/world access
- Separate from application binaries

### 5.2 Application configuration

```yaml
crypto:
  kek:
    keystore-type: PKCS12
    location: file:/opt/app/secrets/kek.p12
    password: ${KEK_STORE_PASSWORD}
    alias: kek-2026-01
```

Password is injected via environment variable or secrets manager.

## 6. Normal operations

### 6.1 Runtime behavior

- Application loads keystore at startup
- Reads **active KEK only**
- Uses KEK to:
    - Encrypt new DEKs
    - Decrypt stored DEKs
- KEK is never logged or serialized

---

## 7. Scheduled KEK rotation (planned)

**Frequency:** every 3–6 months (or per policy)

### 7.1 Generate new KEK

```shell
keytool -genseckey \
  -alias kek-YYYY-MM \
  -keyalg AES \
  -keysize 256 \
  -keystore kek.p12 \
  -storetype PKCS12 \
  -storepass <KEK_STORE_PASSWORD>
```

This **adds** a new KEK.

### 7.2 Activate new KEK

Update application configuration:

```yaml
crypto:
  kek:
    alias: kek-2026-04
```

Restart application. New uploads now use the new KEK.

### 7.3 Run KEK rotation batch job

Purpose:

- Re-encrypt **all DEKs**
- Files remain encrypted with same DEKs

Steps:

1. Trigger `KEK Rotation Job`
2. Monitor completion
3. Verify all attachments reference new alias

### 7.4 Retire old KEK

Only after verification:

```shell
keytool -delete \
  -alias kek-2026-01 \
  -keystore kek.p12 \
  -storetype PKCS12 \
  -storepass <KEK_STORE_PASSWORD>
```

---

## 8. Emergency KEK rotation (compromise)

### Trigger conditions

* Keystore leaked
* Password exposed
* Unauthorized access detected

### Procedure

1. Generate **new KEK immediately**
2. Activate it in configuration
3. Restart application
4. Run:
    - KEK rotation job
    - DEK rotation job
5. If required, run **full file re-encryption**
6. Revoke compromised KEK
7. Rotate keystore password
8. Audit logs and access

---

## 9. Full re-encryption scenario

Required if:

- KEK and DEKs are suspected compromised

Steps:

1. New KEK
2. New DEKs
3. Re-encrypt every file
4. Validate integrity
5. Retire all old keys

This is I/O-intensive and must be scheduled.

---

## 10. Backup and disaster recovery

### 10.1 Backup requirements

- Encrypted backup of `kek.p12`
- Stored separately from data
- Versioned
- Offline copy

### 10.2 Recovery rules

If `kek.p12` is lost:

- All encrypted data is **permanently unrecoverable**
- No brute-force or workaround exists

Treat keystore like:

> a database master encryption key

---

## 11. Auditing and monitoring

Recommended:

- Log KEK alias used for each encryption
- Monitor failed DEK decrypt attempts
- Alert on keystore load failure
- Track the last successful KEK health check

---

## 12. Forbidden actions

- Using KEK to encrypt files directly
- Sharing keystore across environments
- Committing keystore to version control
- Reusing old KEKs
- Disabling KEK health checks

---

## 13. Recommended hardening (future)

- HSM or cloud KMS for KEK
- Dual-control rotation approval
- Short-lived KEKs
- Tamper-evident audit logs

---

## 14. Summary

This runbook ensures:

- Safe KEK lifecycle
- Predictable rotation
- Fast incident response
- Compliance with AES-256-GCM design
- Operational reliability

---
