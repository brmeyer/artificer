package org.overlord.sramp.integration.artifacttypedetector;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;

import java.io.InputStream;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Brett Meyer
 */
public class DefaultArtifactTypeDetectorTest {

    @Test
    public void testPolicyDetection() throws Exception {
        InputStream testSrcContent = null;
        try {
            testSrcContent = getClass().getResourceAsStream("/sample-files/policy/simple-policy.xml");
            ArtifactContent artifactContent = new ArtifactContent("simple-policy.xml", testSrcContent);

            DefaultArtifactTypeDetector detector = new DefaultArtifactTypeDetector();
            ArtifactType artifactType = detector.detect(artifactContent);

            assertEquals(ArtifactType.PolicyDocument(), artifactType);
        } finally {
            IOUtils.closeQuietly(testSrcContent);
        }
    }
}
