/*
 * Copyright 2013 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package julius.sky.voicehud.core.voice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import julius.sky.voicehud.App;
import julius.sky.voicehud.core.router.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VoiceCommandManager implements Runnable{
	private App app;
    private static final String ACOUSTIC_MODEL =
        "resource:/edu/cmu/sphinx/models/en-us/en-us";
    private static final String DICTIONARY_PATH =
        "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static final String GRAMMAR_PATH =
    "resource:/julius/sky/voicehud/core/voice/";    
    private static final String LANGUAGE_MODEL =
        "resource:/edu/cmu/sphinx/demo/dialog/weather.lm";
    private LiveSpeechRecognizer jsgfRecognizer;
    private Configuration configuration;
    private Callable callOnReady;
    

    public VoiceCommandManager(App app, Callable callToCompleteInit) throws Exception{
    	this.app = app;
    	this.callOnReady = callToCompleteInit; 
    	// setup configuration, acoustic model, dictionary, grammar
    	configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setUseGrammar(true);
        configuration.setGrammarName("dialog");
		jsgfRecognizer = new LiveSpeechRecognizer(configuration);
        

     // use more grammars, models etc.
     // bug in sphinx on windows where only one speech recogniser can be used at once.
             
//             configuration.setGrammarName("digits.grxml");
//             LiveSpeechRecognizer grxmlRecognizer =
//                 new LiveSpeechRecognizer(configuration);
     //
//             configuration.setUseGrammar(false);
//             configuration.setLanguageModelPath(LANGUAGE_MODEL);
//             LiveSpeechRecognizer lmRecognizer =
//                 new LiveSpeechRecognizer(configuration);
             
             
    }
    
    
    private static final Map<String, String> COMMANDS =
    		new HashMap<String, String>();
    
    static {
    	COMMANDS.put("hud", "show");
    	COMMANDS.put("hide", "hide");
    	COMMANDS.put("hello", "greeting");
    	COMMANDS.put("music", "music");
    	COMMANDS.put("listen", "music");
    	
    }

    private static final Map<String, Integer> DIGITS =
        new HashMap<String, Integer>();

    static {
        DIGITS.put("oh", 0);
        DIGITS.put("zero", 0);
        DIGITS.put("one", 1);
        DIGITS.put("two", 2);
        DIGITS.put("three", 3);
        DIGITS.put("four", 4);
        DIGITS.put("five", 5);
        DIGITS.put("six", 6);
        DIGITS.put("seven", 7);
        DIGITS.put("eight", 8);
        DIGITS.put("nine", 9);
    }
    
    public void run() {
		// TODO Auto-generated method stub
		try {
			this.callOnReady.call();
			this.startDialog();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
    public void pauseDialog() throws Exception{
        jsgfRecognizer.stopRecognition();

    }
    
    public void startDialog() throws Exception{

        jsgfRecognizer.startRecognition(true);
//    	app.notify();
        while (true) {
         
        	System.out.println("voicehud is listening");
        	
            String utterance = jsgfRecognizer.getResult().getHypothesis();
            
            System.out.println("Utterance heard by sphinx: " + utterance);

//            
            // first stop recognition
            jsgfRecognizer.stopRecognition();

            // route command and determine whether to continue recognition or not.
            boolean continueRecognition = this.app.getRouter().routeCommand(utterance);
            if(continueRecognition){
                jsgfRecognizer.startRecognition(true);
            }
            else{
            	break;
            }
          
        }
    }

    private static double parseNumber(String[] tokens) {
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < tokens.length; ++i) {
            if (tokens[i].equals("point"))
                sb.append(".");
            else
                sb.append(DIGITS.get(tokens[i]));
        }

        return Double.parseDouble(sb.toString());
    }
    

    private static void recognizeDigits(LiveSpeechRecognizer recognizer) {
        System.out.println("Digits recognition (using GrXML)");
        System.out.println("--------------------------------");
        System.out.println("Example: one two three");
        System.out.println("Say \"101\" to exit");
        System.out.println("--------------------------------");

        recognizer.startRecognition(true);
        while (true) {
            String utterance = recognizer.getResult().getHypothesis();
            if (utterance.equals("one zero one")
                || utterance.equals("one oh one"))
                break;
            else
                System.out.println(utterance);
        }
        recognizer.stopRecognition();
    }

//    private static void recognizerBankAccount(LiveSpeechRecognizer recognizer) {
//        System.out.println("This is bank account voice menu");
//        System.out.println("-------------------------------");
//        System.out.println("Example: balance");
//        System.out.println("Example: withdraw zero point five");
//        System.out.println("Example: deposit one two three");
//        System.out.println("Example: back");
//        System.out.println("-------------------------------");
//
//        double savings = .0;
//        recognizer.startRecognition(true);
//
//        while (true) {
//            String utterance = recognizer.getResult().getHypothesis();
//            if (utterance.endsWith("back")) {
//                break;
//            } else if (utterance.startsWith("deposit")) {
//                double deposit = parseNumber(utterance.split("\\s"));
//                savings += deposit;
//                System.out.format("Deposited: $%.2f\n", deposit);
//            } else if (utterance.startsWith("withdraw")) {
//                double withdraw = parseNumber(utterance.split("\\s"));
//                savings -= withdraw;
//                System.out.format("Withdrawn: $%.2f\n", withdraw);
//            } else if (!utterance.endsWith("balance")) {
//                System.out.println("Unrecognized command: " + utterance);
//            }
//
//            System.out.format("Your savings: $%.2f\n", savings);
//        }
//
//        recognizer.stopRecognition();
//    }

    private static void recognizeWeather(LiveSpeechRecognizer recognizer) {

        recognizer.startRecognition(true);
        while (true) {
            String utterance = recognizer.getResult().getHypothesis();
            if (utterance.equals("the end"))
                break;
            else
                System.out.println(utterance);
        }
        recognizer.stopRecognition();
    }
    
    private static void recognizeMusic(LiveSpeechRecognizer recognizer) {

        recognizer.startRecognition(true);
        while (true) {
            String utterance = recognizer.getResult().getHypothesis();
            if (utterance.equals("the end"))
                break;
            else
                System.out.println(utterance);
        }
        recognizer.stopRecognition();
    }

	/**
	 * @param i
	 * @throws InterruptedException 
	 */
	public void sleep(int i) throws InterruptedException {
		// TODO Auto-generated method stub
		Thread.sleep(i);
	}
    


}
