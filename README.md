# Blockchain Real Estate Ledger

A property escrow system connecting a Spring Boot REST API to a Solidity smart contract, with the standard
buy/sell workflow (list, earnest deposit, inspection, funding, finalize or cancel) enforced on-chain.

## Live

- Frontend: https://real-estate-blockchain-git-main-saishyamdonthas-projects.vercel.app
- Backend: https://realestate-blockchain.onrender.com (Render free tier — cold starts after inactivity, first
  request can take up to a minute)
- Contract deployed on Sepolia testnet

## Architecture
Frontend (Next.js, Vercel)
|
Backend (Spring Boot, Render)
|-- PropertyLedgerController -> web3j -> PropertyLedger.sol (Sepolia)
|-- UserController -> Postgres (Neon)
`-- LoginController -> Postgres (Neon)

`PropertyLedgerController` writes property state directly to the smart contract via a web3j-generated Java
wrapper. `UserController` tracks per-user property ownership in Postgres, keyed separately from the on-chain
`propertyId`. These are not currently reconciled by a shared service layer — see Limitations.

## API

**Auth** — `/api/auth`
| Method | Path        |
|--------|-------------|
| POST   | `/register` |
| POST   | `/login`    |
| POST   | `/logout`   |

**Property ledger (on-chain)** — `/ledger`
| Method | Path                         | Description                    |
|--------|------------------------------|---------------------------------|
| POST   | `/register`                  | Register a new property         |
| GET    | `/next-id`                   | Get the next available property ID |
| POST   | `/{propertyId}/list`         | List a property for sale        |
| POST   | `/{propertyId}/earnest`      | Submit earnest deposit          |
| POST   | `/{propertyId}/inspection`   | Record inspection outcome       |
| POST   | `/{propertyId}/fund`         | Fund the escrow                 |
| POST   | `/{propertyId}/finalize`     | Finalize the sale                |
| POST   | `/{propertyId}/cancel`       | Cancel the sale                  |
| GET    | `/{propertyId}`              | Get property state              |
| GET    | `/{propertyId}/owner`        | Get current owner                |

**User properties (off-chain)** — `/api/user`
| Method | Path                                               | Description                  |
|--------|-----------------------------------------------------|-------------------------------|
| POST   | `/add/property/{userId}/{uniqueId}`                  | Add a property to a user      |
| GET    | `/{userId}/properties`                               | List a user's properties      |
| GET    | `/properties/for-sale`                               | List all properties for sale  |
| PUT    | `/property/sell/{propertyId}/{userUniqueId}`         | Mark a property for sale      |
| PUT    | `/property/transfer/{propertyId}/{sellerId}/{buyerId}` | Transfer ownership           |
| DELETE | `/{userId}/delete/property/{propertyId}`             | Remove a property from a user |

## Run locally
git clone https://github.com/saishyamdontha/RealEstate_Blockchain.git
cd RealEstate_Blockchain

set BLOCKCHAIN_RPC, BLOCKCHAIN_PRIVATE_KEY, BLOCKCHAIN_CONTRACT as environment variables

./mvnw spring-boot:run



Requires a running Ethereum node (local Ganache/Hardhat, or a Sepolia RPC endpoint) and the `PropertyLedger`
contract deployed to it.

## Limitations

- **Auth is not yet enforced on the ledger and user endpoints.** `LoginController` provides register/login/logout,
  but `PropertyLedgerController` and `UserController` do not currently check a session or token before executing
  state-changing calls (`finalize`, `cancel`, `fund`, `sell`, `transfer`). Anyone who knows a `propertyId` can call
  these directly. This is a known gap, not yet fixed.
- On-chain property state (`PropertyLedgerController`) and off-chain per-user property tracking
  (`UserController`) are not reconciled by a shared service layer; they can drift out of sync.
- Deployed to Sepolia testnet only — not audited, not intended to hold real funds.
- Early commits contained a local devnet private key (Ganache/Hardhat default ports) in `application.properties`,
  since moved to an environment variable. The exposed key was never funded on any public network.

## CI and Security Pipeline

![CI](https://github.com/saishyamdontha/RealEstate_Blockchain/actions/workflows/ci.yml/badge.svg)
![Security](https://github.com/saishyamdontha/RealEstate_Blockchain/actions/workflows/security.yml/badge.svg)

Every push to `main` runs two GitHub Actions workflows.

| Workflow | Job | Tool | What it checks |
|---|---|---|---|
| CI | Build and test | Maven, JUnit | Compiles the backend and runs all 7 tests |
| Security | Secret scan | Gitleaks | Full Git history for committed keys and passwords |
| Security | Dependency scan | Trivy | Known HIGH and CRITICAL CVEs in Maven dependencies |
| Security | Static analysis | Semgrep | Insecure patterns in the Java source |

Tests run with a `test` profile (`src/test/resources/application-test.properties`) that uses an
in-memory H2 database and a dummy key, so the pipeline needs no production secrets.

The dependency and static analysis jobs currently run in report-only mode. A blocking severity
gate is planned once the baseline findings are triaged.
