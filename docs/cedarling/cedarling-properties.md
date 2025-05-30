---
tags:
  - administration
  - lock
  - authorization / authz
  - Cedar
  - Cedarling
  - properties
---

# Cedarling Properties

These Bootstrap Properties control default application level behavior.

* **`CEDARLING_APPLICATION_NAME`** : Human friendly identifier for this application
* **`CEDARLING_POLICY_STORE_URI`** : Location of policy store JSON, used if policy store is not local.
* **`CEDARLING_POLICY_STORE_ID`** : The identifier of the policy store in case there is more then one policy_store_id in the policy store.
* **`CEDARLING_USER_AUTHZ`** : When `enabled`, Cedar engine authorization is queried for a User principal.
* **`CEDARLING_WORKLOAD_AUTHZ`** : When `enabled`, Cedar engine authorization is queried for a Workload principal.
* **`CEDARLING_PRINCIPAL_BOOLEAN_OPERATION`** : property specifies what boolean operation to use for the `USER` and `WORKLOAD` when making authz (authorization) decisions. [See here](#user-workload-boolean-operation).
* **`CEDARLING_MAPPING_USER`** : Name of Cedar User schema entity if we don't want to use default. When specified Cedarling try build defined entity (from schema) as user instead of default `User` entity defined in `cedar` schema. Works in namespace defined in the policy store.
* **`CEDARLING_MAPPING_WORKLOAD`** : Name of Cedar Workload schema entity
* **`CEDARLING_MAPPING_ROLE`** : Name of Cedar Role schema entity
* **`CEDARLING_UNSIGNED_ROLE_ID_SRC`** : The attribute that will be used to create the Role entity when using the unsigned interface. Defaults to `"role"`.

**The following bootstrap properties are needed to configure log behavior:**

* **`CEDARLING_LOG_TYPE`** : `off`, `memory`, `std_out`
* **`CEDARLING_LOG_LEVEL`** : System Log Level [See here](./cedarling-logs.md). Default to `WARN`
* **`CEDARLING_DECISION_LOG_USER_CLAIMS`** : List of claims to map from user entity, such as ["sub", "email", "username", ...]
* **`CEDARLING_DECISION_LOG_WORKLOAD_CLAIMS`** : List of claims to map from user entity, such as ["client_id", "rp_id", ...]
* **`CEDARLING_DECISION_LOG_DEFAULT_JWT_ID`** : Token claims that will be used for decision logging. Default is "jti", but perhaps some other claim is needed.
* **`CEDARLING_LOG_TTL`** : in case of `memory` store, TTL (time to live) of log entities in seconds.
* **`CEDARLING_LOG_MAX_ITEMS`** : Maximum number of log entities that can be stored using Memory logger. If used `0` value means no limit. And If missed or None, default value is applied.
* **`CEDARLING_LOG_MAX_ITEM_SIZE`** : Maximum size of a single log entity in bytes using Memory logger. If used `0` value means no limit. And If missed or None, default value is applied.

**The following bootstrap properties are needed to configure JWT and cryptographic behavior:**

* **`CEDARLING_LOCAL_JWKS`** : JWKS file with public keys
* **`CEDARLING_POLICY_STORE_LOCAL`** : JSON object as string with policy store. You can use [this](https://jsontostring.com/) converter.
* **`CEDARLING_POLICY_STORE_LOCAL_FN`** : Local file with JSON object with policy store
* **`CEDARLING_JWT_SIG_VALIDATION`** : `enabled` | `disabled` -- Whether to check the signature  of all JWT tokens. This requires an `iss` is present.
* **`CEDARLING_JWT_STATUS_VALIDATION`** : `enabled` | `disabled` -- Whether to check the status of the JWT. On startup, the Cedarling should fetch and retreive the latest Status List JWT from the `.well-known/openid-configuration` via the `status_list_endpoint` claim and cache it. See the [IETF Draft](https://datatracker.ietf.org/doc/draft-ietf-oauth-status-list/) for more info.
* **`CEDARLING_JWT_SIGNATURE_ALGORITHMS_SUPPORTED`** : Only tokens signed with these algorithms are acceptable to the Cedarling.
* **`CEDARLING_ID_TOKEN_TRUST_MODE`** :  `Strict` | `None`. Varying levels of validations based on the preference of the developer.
`Strict` mode requires (1) id_token `aud` matches the access_token `client_id`; (2) if a Userinfo token is present, the `sub` matches the id_token, and that the `aud` matches the access token client_id.

**The following bootstrap properties are only needed for enterprise deployments.**

* **`CEDARLING_LOCK`** : `enabled` | `disabled`. If `enabled`, the Cedarling will connect to the Lock Server for policies, and subscribe for SSE events.
* **`CEDARLING_LOCK_SERVER_CONFIGURATION_URI`** : Required if `LOCK` == `enabled`. URI where Cedarling can get JSON file with all required metadata about the Lock Server, i.e. `.well-known/lock-master-configuration`.
* **`CEDARLING_LOCK_DYNAMIC_CONFIGURATION`** : `enabled` | `disabled`, controls whether Cedarling should listen for SSE config updates.
* **`CEDARLING_LOCK_SSA_JWT`** : SSA for DCR in a Lock Server deployment. The Cedarling will validate this SSA JWT prior to DCR.
* **`CEDARLING_LOCK_LOG_INTERVAL`** : How often to send log messages to Lock Server (0 to turn off trasmission).
* **`CEDARLING_LOCK_HEALTH_INTERVAL`** : How often to send health messages to Lock Server (0 to turn off transmission).
* **`CEDARLING_LOCK_TELEMETRY_INTERVAL`** : How often to send telemetry messages to Lock Server (0 to turn off transmission).
* **`CEDARLING_LOCK_LISTEN_SSE`** :  `enabled` | `disabled`: controls whether Cedarling should listen for updates from the Lock Server.

## Required properties for startup

* **`CEDARLING_APPLICATION_NAME`**

To enable usage of principals at least one of the following keys must be provided:

* **`CEDARLING_WORKLOAD_AUTHZ`**
* **`CEDARLING_USER_AUTHZ`**

To load policy store one of the following keys must be provided:

* **`CEDARLING_POLICY_STORE_LOCAL`**
* **`CEDARLING_POLICY_STORE_URI`**
* **`CEDARLING_POLICY_STORE_LOCAL_FN`**

All other fields are optional and can be omitted. If a field is not provided, Cedarling will use the default value specified in the property definition.
