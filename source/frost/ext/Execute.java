package frost.ext;

import java.util.logging.*;


/**
 * Supports execution of external programs
 * @author Jan-Thomas Czornack
 */
public class Execute {

	private static Logger logger = Logger.getLogger(Execute.class.getName());

    /**
     * start external program, and return their output
     * @param order the command to execute
     * @return the output generated by the program. Standard ouput and Error output are captured.
     */
    public static String run(String order) {
	logger.info("-------------------------------------------------------------------\n" +
				"Execute: " + order + "\n" + 
				"-------------------------------------------------------------------");

	Transit inputData = new Transit();
	Transit errorData = new Transit();

	try {
	    Process p = Runtime.getRuntime().exec(order);    		

	    InputStreamThread ist = new InputStreamThread(p, inputData);
	    ist.start();

	    ErrorStreamThread est = new ErrorStreamThread(p, errorData);
	    est.start();

// 	    ist.join();
// 	    est.join();

// 	    p.destroy();
// 	    System.out.println("process stopped");
	    
	}
	catch(Exception exception) {
		logger.log(Level.SEVERE, "Error in exec", exception);
	}

	//	System.out.println(inputData.getString() + errorData.getString());
	return inputData.getString() + errorData.getString();
    }
}




