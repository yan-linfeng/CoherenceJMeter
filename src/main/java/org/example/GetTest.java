package org.example;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.example.common.Util;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "gettest", mixinStandardHelpOptions = true, description = "Execute Coherence Get Test.")
public class GetTest implements Callable<Integer> {
    @CommandLine.Option(names = {"-jmeterHome", "--jmeterHome"}, required = true, description = "JMeter's Home directory.")
    private String jmeterHome;
    @CommandLine.Option(names = {"-threads", "--threads"}, defaultValue = "10", description = "Parallel threads number you want to specify. (Like how many users)")
    private int threads;
    @CommandLine.Option(names = {"-hosts", "--hosts"}, split=",", defaultValue = "127.0.0.1", description = "The coherence rest proxy hosts. You can specicy multiple hosts seperated by [,] sign. eg 192.168.0.1,192.168.0.2")
    private String[] hosts;
    @CommandLine.Option(names = {"-port", "--port"}, defaultValue = "8080", description = "The port number of coherence rest proxy. Default is 8080.")
    private int port;

    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new GetTest()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Create the Jmeter engine
        StandardJMeterEngine jm = Util.initJmeterEngine(jmeterHome);
        // Create a loop controller
        LoopController loopController = Util.createLoopController();

        ThreadGroup[] threadGroups = new ThreadGroup[threads];
        for(int i = 0; i< threads; i++){
            // Create a thread group
            ThreadGroup threadGroup = new ThreadGroup();
            threadGroup.setName("Thread Group");
            threadGroup.setNumThreads(1);
            threadGroup.setRampUp(0);
            threadGroup.setSamplerController(loopController);
            threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
            threadGroups[i] = threadGroup;
        }

        // Create a test plan
        TestPlan testPlan = new TestPlan("Coherence Get Test Plan");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Create a new hash tree to hold our test elements
        ListedHashTree testPlanTree = new ListedHashTree();
        // Add the test plan to our hash tree, this is the top level of our test
        testPlanTree.add(testPlan);

        // Create another hash trees and add the thread group to our test plan
        HashTree[] threadGroupHashTrees = new HashTree[threads];
        for(int i = 0; i< threads; i++){
            threadGroupHashTrees[i] = testPlanTree.add(testPlan, threadGroups[i]);
        }

        File testdataDir = new File("./testdata");
        File[] files = testdataDir.listFiles();
        for(File jsonFile:files){
            // Create a sampler
            HTTPSamplerProxy getHttpSampler = new HTTPSamplerProxy();
            getHttpSampler.setDomain(hosts[new Random().nextInt(hosts.length)]);
            getHttpSampler.setPort(port);
            getHttpSampler.setPath("/cache/json/"+jsonFile.getName());
            getHttpSampler.setMethod("GET");
            getHttpSampler.setProtocol("http");
            getHttpSampler.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
            getHttpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

            // Create a hash tree to add the post processor to
            HashTree httpSamplerTree = new HashTree();
            httpSamplerTree.add(getHttpSampler, Util.createRespnoseAssertion());

            // Add the http sampler to the hash tree that contains the thread group
            threadGroupHashTrees[new Random().nextInt(threadGroupHashTrees.length)].add(httpSamplerTree);
        }

        // Summariser
        Summariser summariser = null;
        String summariserName = JMeterUtils.getPropDefault("summarise.names", "summary response");
        if (summariserName.length() > 0) {
            summariser = new Summariser(summariserName);
        }

        ResultCollector logger = new ResultCollector(summariser);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        // Write to a file
        ResultCollector rc = new ResultCollector();
        rc.setEnabled(true);
        rc.setErrorLogging(false);
        rc.isSampleWanted(true);
        SampleSaveConfiguration ssc = new SampleSaveConfiguration();
        ssc.setTime(true);
        ssc.setAssertionResultsFailureMessage(true);
        ssc.setThreadCounts(true);
        rc.setSaveConfig(ssc);
        File testResultsFile = new File("./CoherenceGetTestResults.jtl");
        if(testResultsFile.exists()){
            testResultsFile.delete();
        }
        rc.setFilename("./CoherenceGetTestResults.jtl");
        testPlanTree.add(testPlanTree.getArray()[0], rc);

        // Configure
        jm.configure(testPlanTree);
        // Run
        jm.run();
        return 0;
    }


}
