package org.example;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "puttest", mixinStandardHelpOptions = true, description = "Execute Coherence Put Test.")
public class PutTest implements Callable<Integer> {
    @CommandLine.Option(names = {"-jmeterHome", "--jmeterHome"}, required = true, description = "JMeter's Home directory.")
    private String jmeterHome;
    @CommandLine.Option(names = {"-threads", "--threads"}, defaultValue = "10", description = "Parallel threads number you want to specify. (Like how many users)")
    private int threads;
    @CommandLine.Option(names = {"-hosts", "--hosts"}, split=",", defaultValue = "127.0.0.1",description = "The coherence rest proxy hosts. You can specicy multiple hosts seperated by [,] sign. eg 192.168.0.1,192.168.0.2")
    private String[] hosts;
    @CommandLine.Option(names = {"-port", "--port"}, defaultValue = "8080",description = "The port number of coherence rest proxy. Default is 8080.")
    private int port;

    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new PutTest()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        // Create the Jmeter engine
        StandardJMeterEngine jm = new StandardJMeterEngine();

        // Configure JMeter properties
        JMeterUtils.setJMeterHome(jmeterHome);
        JMeterUtils.loadJMeterProperties(jmeterHome+"/bin/jmeter.properties");
        JMeterUtils.initLocale();

        // Response Assertion
        ResponseAssertion responseAssertion = new ResponseAssertion();
        responseAssertion.setTestFieldResponseCode();
        responseAssertion.addTestString("200");

        // Create a loop controller
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
//        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();

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
        TestPlan testPlan = new TestPlan("Coherence Put Test Plan");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Create a new hash tree to hold our test elements
        ListedHashTree testPlanTree = new ListedHashTree();
        // Add the test plan to our hash tree, this is the top level of our test
        testPlanTree.add(testPlan);

        // Create another hash tree and add the thread group to our test plan
        HashTree[] threadGroupHashTrees = new HashTree[threads];
        for(int i = 0; i< threads; i++){
            threadGroupHashTrees[i] = testPlanTree.add(testPlan, threadGroups[i]);
        }

        File testdataDir = new File("./testdata");
        File[] files = testdataDir.listFiles();
        for(File jsonFile:files){
            // Create a sampler
            HTTPSamplerProxy putHttpSampler = new HTTPSamplerProxy();
            putHttpSampler.setDomain(hosts[new Random().nextInt(hosts.length)]);
            putHttpSampler.setPort(port);
            putHttpSampler.setPath("/cache/json/"+jsonFile.getName());
            putHttpSampler.setMethod("PUT");
            putHttpSampler.setProtocol("http");
            putHttpSampler.setProperty(TestElement.TEST_CLASS, HTTPSampler.class.getName());
            putHttpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
            putHttpSampler.setPostBodyRaw(true);
            HTTPFileArg file = new HTTPFileArg(jsonFile.getAbsolutePath());
            putHttpSampler.setHTTPFiles(new HTTPFileArg[]{file});

            // Create a hash tree to add the post processor to
            HashTree httpSamplerTree = new HashTree();
            httpSamplerTree.add(putHttpSampler, responseAssertion);
            httpSamplerTree.add(putHttpSampler, createHeaderManager());
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
        File testResultsFile = new File("./CoherencePutTestResults.jtl");
        if(testResultsFile.exists()){
            testResultsFile.delete();
        }
        rc.setFilename("./CoherencePutTestResults.jtl");
        testPlanTree.add(testPlanTree.getArray()[0], rc);

        // Configure
        jm.configure(testPlanTree);

        // Run
        jm.run();

        return 0;
    }


    private static HeaderManager createHeaderManager() {
        HeaderManager manager = new HeaderManager();
        manager.add(new Header("Content-Type", "application/json"));
        manager.setName(JMeterUtils.getResString("header_manager_title"));
        manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        return manager;
    }
}
