/*
 * Janssen Project software is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.configapi.plugin.fido2;

import io.jans.configapi.core.test.BaseTest;

import java.util.Map;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

public class Fido2BaseTest extends BaseTest {

    // Execute before each test is run
    @BeforeMethod
    public void before() {
        log.error("\n\n\n *** FIDO2 Plugin propertiesMap.get(fido2Url):{} {}", propertiesMap.get("fido2Url"), "\n\n\n");
        boolean isAvailable = isEndpointAvailable(propertiesMap.get("fido2Url"), accessToken);
        log.error("\n\n\n *** FIDO2 Plugin isAvailable:{} {}", isAvailable, "\n\n\n");
        // check condition, note once you condition is met the rest of the tests will be
        // skipped as well
        if (!isAvailable) {
            throw new SkipException("FIDO2 Plugin Not deployed");
        }else {
            log.info("\n\n\n *** FIDO2 Plugin is Deployed {}", "\n\n");
        }
    }

}
