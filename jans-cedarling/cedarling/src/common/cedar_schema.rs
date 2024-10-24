/*
 * This software is available under the Apache-2.0 license.
 * See https://www.apache.org/licenses/LICENSE-2.0.txt for full text.
 *
 * Copyright (c) 2024, Gluu, Inc.
 */

pub(crate) use cedar_json::CedarSchemaJson;

pub(crate) mod cedar_json;

/// Box that holds the [`cedar_policy::Schema`] and
/// JSON representation that is used to create entities from the schema in the policy store.
#[derive(Debug, Clone)]
#[allow(dead_code)]
pub(crate) struct CedarSchema {
    pub schema: cedar_policy::Schema,
    pub json: cedar_json::CedarSchemaJson,
}

impl<'de> serde::Deserialize<'de> for CedarSchema {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        deserialize::parse_cedar_schema(deserializer)
    }
}

mod deserialize {
    use super::*;
    use base64::prelude::*;

    #[derive(Debug, thiserror::Error)]
    pub enum ParseCedarSchemaSetMessage {
        #[error("unable to decode cedar policy schema base64")]
        Base64,
        #[error("unable to unmarshal cedar policy schema json to the structure")]
        CedarSchemaJsonFormat,
        #[error("unable to parse cedar policy schema json")]
        Parse,
    }

    /// A custom deserializer for Cedar's Schema.
    //
    // is used to deserialize field `schema` in `PolicyStore` from base64 and get [`cedar_policy::Schema`]
    pub(crate) fn parse_cedar_schema<'de, D>(deserializer: D) -> Result<CedarSchema, D::Error>
    where
        D: serde::Deserializer<'de>,
    {
        let source = <String as serde::Deserialize>::deserialize(deserializer)?;
        let decoded: Vec<u8> = BASE64_STANDARD.decode(source.as_str()).map_err(|err| {
            serde::de::Error::custom(format!("{}: {}", ParseCedarSchemaSetMessage::Base64, err,))
        })?;

        // parse cedar policy schema to the our structure
        let cedar_policy_json: CedarSchemaJson = serde_json::from_reader(decoded.as_slice())
            .map_err(|err| {
                serde::de::Error::custom(format!(
                    "{}: {}",
                    ParseCedarSchemaSetMessage::CedarSchemaJsonFormat,
                    err
                ))
            })?;

        // parse cedar policy schema to the `cedar_policy::Schema`
        let cedar_policy_schema = cedar_policy::Schema::from_json_file(decoded.as_slice())
            .map_err(|err| {
                serde::de::Error::custom(format!("{}: {}", ParseCedarSchemaSetMessage::Parse, err))
            })?;

        Ok(CedarSchema {
            schema: cedar_policy_schema,
            json: cedar_policy_json,
        })
    }

    #[cfg(test)]
    mod tests {
        use super::*;
        use crate::common::policy_store::PolicyStoreMap;

        #[test]
        fn test_read_ok() {
            static POLICY_STORE_RAW: &str =
                include_str!("../../../test_files/policy-store_ok.json");

            let policy_result = serde_json::from_str::<PolicyStoreMap>(POLICY_STORE_RAW);
            assert!(policy_result.is_ok());
        }

        #[test]
        fn test_read_base64_error() {
            static POLICY_STORE_RAW: &str =
                include_str!("../../../test_files/policy-store_schema_err_base64.json");

            let policy_result = serde_json::from_str::<PolicyStoreMap>(POLICY_STORE_RAW);
            assert!(policy_result
                .unwrap_err()
                .to_string()
                .contains(&ParseCedarSchemaSetMessage::Base64.to_string()));
        }

        #[test]
        fn test_read_json_error() {
            static POLICY_STORE_RAW: &str =
                include_str!("../../../test_files/policy-store_schema_err_json.json");

            let policy_result = serde_json::from_str::<PolicyStoreMap>(POLICY_STORE_RAW);
            assert!(policy_result
                .unwrap_err()
                .to_string()
                .contains(&ParseCedarSchemaSetMessage::CedarSchemaJsonFormat.to_string()));
        }

        #[test]
        fn test_parse_cedar_error() {
            static POLICY_STORE_RAW: &str =
                include_str!("../../../test_files/policy-store_schema_err_cedar_mistake.json");

            let policy_result = serde_json::from_str::<PolicyStoreMap>(POLICY_STORE_RAW);
            let err_msg = policy_result.unwrap_err().to_string();
            assert_eq!(
                err_msg,
                "unable to parse cedar policy schema json: failed to resolve type: User_TypeNotExist at line 35 column 1"
            );
        }
    }
}
