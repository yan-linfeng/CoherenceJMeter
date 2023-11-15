package org.example.common;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class Util {

    public static StandardJMeterEngine initJmeterEngine(String jmeterHome){
        // Create the Jmeter engine
        StandardJMeterEngine jMeterEngine = new StandardJMeterEngine();
        // Configure JMeter properties
        JMeterUtils.setJMeterHome(jmeterHome);
        JMeterUtils.loadJMeterProperties(jmeterHome + "/bin/jmeter.properties");
        JMeterUtils.initLocale();
        return jMeterEngine;
    }

    public static ResponseAssertion createRespnoseAssertion(){
        ResponseAssertion responseAssertion = new ResponseAssertion();
        responseAssertion.setTestFieldResponseCode();
        responseAssertion.addTestString("200");
        return responseAssertion;
    }

    public static LoopController createLoopController(){
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        return loopController;
    }
}
