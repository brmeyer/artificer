package org.overlord.sramp.integration.artifactbuilder;

import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyDocument;
import org.overlord.sramp.common.ArtifactContent;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author Brett Meyer
 */
public class PolicyArtifactBuilderTest {

    @Test
    public void testPolicyArtifactBuilder() throws Exception {
        PolicyArtifactBuilder builder = new PolicyArtifactBuilder();
        PolicyDocument artifact = new PolicyDocument();
        artifact.setArtifactType(BaseArtifactEnum.POLICY_DOCUMENT);
        artifact.setName("simple-policy.xml");
        InputStream is = getClass().getResourceAsStream("/sample-files/policy/simple-policy.xml");

        // Derive
        Collection<BaseArtifactType> derivedArtifacts = builder.buildArtifacts(artifact,
                new ArtifactContent("simple-policy.xml", is)).getDerivedArtifacts();
        builder.buildRelationships(new MockRelationshipContext());
    }
}
