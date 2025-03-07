/*
 * Janssen Project software is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.fido2.service.verifier;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import io.jans.fido2.model.attestation.AttestationErrorResponseType;
import io.jans.fido2.model.error.ErrorResponseFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import io.jans.fido2.exception.Fido2RuntimeException;
import io.jans.util.security.SecurityProviderUtility;

import org.slf4j.Logger;

/**
 * @author Yuriy Movchan
 * @version May 08, 2020
 */
@ApplicationScoped
public class SignatureVerifier {

    @Inject
    private Logger log;

    @Inject
    private ErrorResponseFactory errorResponseFactory;

    public void verifySignature(byte[] signature, byte[] signatureBase, PublicKey publicKey, int signatureAlgorithm) {
        try {
            Signature signatureChecker = getSignatureChecker(signatureAlgorithm);
            signatureChecker.initVerify(publicKey);
            signatureChecker.update(signatureBase);
            if (!signatureChecker.verify(signature)) {
            	log.error("Unable to verify signature");
                throw errorResponseFactory.badRequestException(AttestationErrorResponseType.INVALID_CERTIFICATE, "Unable to verify signature");
            }
        } catch (IllegalArgumentException | InvalidKeyException | SignatureException e) {
            log.error("Can't verify the signature ", e);
            throw errorResponseFactory.badRequestException(AttestationErrorResponseType.INVALID_CERTIFICATE, "Can't verify the signature");
        }
    }

    public Signature getSignatureChecker(int signatureAlgorithm) {
        Provider provider = SecurityProviderUtility.getBCProvider();
        log.debug("Signature checker : "+ signatureAlgorithm );

        // https://www.iana.org/assignments/cose/cose.xhtml#algorithms
        try {

            switch (signatureAlgorithm) {
                case -7: {
                    Signature signatureChecker = Signature.getInstance("SHA256withECDSA", provider);
                    return signatureChecker;
                }

                case -8: {
                    Signature signatureChecker = Signature.getInstance("Ed25519");
                    return signatureChecker;
                }

                case -35: {
                    Signature signatureChecker = Signature.getInstance("SHA384withECDSA", provider);
                    return signatureChecker;
                }

                case -36: {
                    Signature signatureChecker = Signature.getInstance("SHA512withECDSA", provider);
                    return signatureChecker;
                }

                case -37: {
                    Signature signatureChecker = Signature.getInstance("SHA256withRSA/PSS", provider);
                    signatureChecker.setParameter(new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1));
                    return signatureChecker;
                }
                case -38: {
                    Signature signatureChecker = Signature.getInstance("SHA384withRSA/PSS", provider);
                    signatureChecker.setParameter(new PSSParameterSpec("SHA-384", "MGF1", new MGF1ParameterSpec("SHA-384"), 32, 1));
                    return signatureChecker;
                }

                case -39: {
                    Signature signatureChecker = Signature.getInstance("SHA512withRSA/PSS", provider);
                    signatureChecker.setParameter(new PSSParameterSpec("SHA-512", "MGF1", new MGF1ParameterSpec("SHA-512"), 32, 1));
                    return signatureChecker;
                }
                case -257: {
                    Signature signatureChecker = Signature.getInstance("SHA256withRSA");
                    return signatureChecker;
                }
                case -258: {
                    Signature signatureChecker = Signature.getInstance("SHA384withRSA", provider);
                    return signatureChecker;
                }
                case -259: {
                    Signature signatureChecker = Signature.getInstance("SHA512withRSA", provider);
                    return signatureChecker;
                }
                case -65535: {
                    Signature signatureChecker = Signature.getInstance("SHA1withRSA");
                    return signatureChecker;
                }

                default: {
                    throw new Fido2RuntimeException("Unknown mapping");
                }

            }

        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            throw new Fido2RuntimeException("Problem with crypto");
        }
    }

    public MessageDigest getDigest(int signatureAlgorithm) {
        // https://www.iana.org/assignments/cose/cose.xhtml#algorithms
        switch (signatureAlgorithm) {
            case -8:{
                return DigestUtils.getSha512Digest();
            }

            case -257: {
                return DigestUtils.getSha256Digest();
            }

            case -65535: {
                return DigestUtils.getSha1Digest();
            }

            default: {
                throw new Fido2RuntimeException("Unknown mapping");
            }

        }
    }

    public void verifySignature(byte[] signature, byte[] signatureBase, Certificate certificate, int signatureAlgorithm) {
        verifySignature(signature, signatureBase, certificate.getPublicKey(), signatureAlgorithm);
    }

}
