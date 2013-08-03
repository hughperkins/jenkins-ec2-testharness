/*
 * The MIT License
 *
 * Copyright (c) 2013, Hugh Perkins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.ec2harness;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Node;
import hudson.plugins.ec2.*;
import hudson.plugins.ec2.EC2Tag;
import hudson.plugins.ec2.SlaveTemplate;

import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import com.amazonaws.services.ec2.model.*;

/// This wraps the normal SlaveTemplate (technically: derives from
/// the normal SlaveTemplate).  We need this because we need to 
/// be able to change the ssh port, and/or the remoteFS,
/// so that we can create many
/// slaves that actually run on the same machine
/// Other than that, it's identical to the parent class
/// We will hook this into the framework via the DescriptorImpl of 
/// our AmazonEC2CloudTester class
public class SlaveTemplateForTests extends SlaveTemplate {
	public final String slaveSimulatorHostname;

    @DataBoundConstructor
	public SlaveTemplateForTests(String slaveSimulatorHostname, String ami, String zone,
			SpotConfiguration spotConfig, String securityGroups,
			String remoteFS, String sshPort, InstanceType type,
			String labelString, Node.Mode mode, String description,
			String initScript, String userData, String numExecutors,
			String remoteAdmin, String rootCommandPrefix, String jvmopts,
			boolean stopOnTerminate, String subnetId, List<EC2Tag> tags,
			String idleTerminationMinutes, boolean usePrivateDnsName,
			String instanceCapStr, String iamInstanceProfile) {
		super(ami, zone, spotConfig, securityGroups, remoteFS, sshPort, type,
				labelString, mode, description, initScript, userData, numExecutors,
				remoteAdmin, rootCommandPrefix, jvmopts, stopOnTerminate, subnetId,
				tags, idleTerminationMinutes, usePrivateDnsName, instanceCapStr,
				iamInstanceProfile);
		this.slaveSimulatorHostname = slaveSimulatorHostname;
    	System.out.println("SlaveTemplateForTests() " + slaveSimulatorHostname );
	}

	@Override
    protected EC2OndemandSlave newOndemandSlave(Instance inst) throws FormException, IOException {
         EC2OndemandSlave ec2OndemandSlave = new EC2OndemandSlave(inst.getInstanceId(), description, 
        		remoteFS + "/" + inst.getInstanceId(), getSshPort(), getNumExecutors(), labels, 
        		mode, initScript, remoteAdmin, rootCommandPrefix, jvmopts, stopOnTerminate,
        		idleTerminationMinutes, slaveSimulatorHostname, slaveSimulatorHostname,
        		EC2Tag.fromAmazonTags(inst.getTags()), parent.name, usePrivateDnsName);
         ec2OndemandSlave.publicDNS = slaveSimulatorHostname;
         ec2OndemandSlave.privateDNS = slaveSimulatorHostname;
         inst.setPublicDnsName(slaveSimulatorHostname);
         inst.setPrivateDnsName(slaveSimulatorHostname);
         return ec2OndemandSlave;
    }

	@Override
	protected EC2SpotSlave newSpotSlave(SpotInstanceRequest sir, String name) throws FormException, IOException {
		throw new RuntimeException("Not implemented yet.  This could be your opportunity to contribute to Jenkins!");
    }

	@Extension
	public static class DescriptorImpl extends Descriptor<SlaveTemplate> {

		@Override
		public String getDisplayName() {
			return "SlaveTemplateForTests";
		}
		
	}
}
