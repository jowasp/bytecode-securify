package the.bytecode.club.bytecodeviewer.xposedgenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.objectweb.asm.tree.ClassNode;
import the.bytecode.club.bytecodeviewer.decompilers.bytecode.ClassNodeDecompiler;
import the.bytecode.club.bytecodeviewer.BytecodeViewer;
import the.bytecode.club.bytecodeviewer.decompilers.FernFlowerDecompiler;
import the.bytecode.club.bytecodeviewer.decompilers.SmaliDisassembler;

public class XposedGenerator {	
	public String decompileClassNode(ClassNode cn, byte[] b) {
        throw new IllegalArgumentException();
    }
	 	
	public void ParseChosenFileContent(String classname, String containerName, ClassNode classNode){
		
		 //Parse content - Extract methods after APK /JAR has been extracted
			byte[] cont = BytecodeViewer.getFileContents(classname.toString());
			ClassNodeDecompiler decompiler = new ClassNodeDecompiler();
			//Use one of the decompilers
			FernFlowerDecompiler decompilefern = new FernFlowerDecompiler();
			//SmaliDisassembler smaliDecompiler = new SmaliDisassembler();
			
			//Decompile using Fern
			String decomp  = decompilefern.decompileClassNode(classNode, cont);
			//String decompileByteToSmali = smaliDecompiler.decompileClassNode(classNode, cont);
			
			String[] xposedTemplateTypes = {"Empty","Parameters","Helper"};
			JComboBox xposedTemplateList = new JComboBox(xposedTemplateTypes);
			 //Set results of parsed methods into an a list 
			List<String> methodsExtracted = ProcessContentExtractedClass(decomp);
			String packgExtracted = ProcessContentExtractedPackage(decomp);
			System.out.println("PACKAGE NAME: " +packgExtracted);
			     	  
			//Get a clean list
			List<String> cleanMethods = null;
			cleanMethods = ProcessCleanMethodsAll(methodsExtracted);
			JComboBox<String> cb = new JComboBox<>(cleanMethods.toArray(new String[cleanMethods.size()]));
			//Add Panel elements		   		      
		    //Start Panel
		      JPanel myPanel = new JPanel();
		      myPanel.add(Box.createHorizontalStrut(15));
		      myPanel.add(xposedTemplateList);
		      myPanel.add(cb);

		     //output methods to pane box
				int result = JOptionPane.showConfirmDialog(null, myPanel, 
				 "Choose Template and Method for Xposed Module", JOptionPane.OK_CANCEL_OPTION);
			      if (result == JOptionPane.OK_OPTION) {
			    	//clear list
					cleanMethods.clear();
					cleanMethods.removeAll(cleanMethods);
			    	  //Read Chosen Class
			    	 System.out.println("SELECTED CLASS is" + cb.getSelectedItem());
			    	 String selectedClass = cb.getSelectedItem().toString();
			    	 String selectedXposedTemplate = xposedTemplateList.getSelectedItem().toString();
			    	
			    	 //WriteXposed Class with extracted data				
			    	 WriteXposedModule(selectedClass, packgExtracted, classname, selectedXposedTemplate);
			    	 cleanMethods.removeAll(cleanMethods);			    	 
			      }	
			      
	}
	
	public void WriteXposedModule(String functionToHook, String packageName, String classToHook, String template) {
		if (template == "Empty") 
		{
		 try {			 
			 //TODO: Change to dynamic path	depending on the template Xposed module		 
			 File file = new File("/Users/johannacuriel/Documents/Java-Classes/XposedModule/app/src/main/java/androidpentesting/com/xposedmodule/XposedClassTest.java");
		
			// if file doesn't exists, then create it
						if (!file.exists()) {
							file.createNewFile();
						}
						//Extract the package name only
						String packageNameOnly = packageName.substring(8,packageName.length() - 2 );						
						String classToHookNameOnly = classToHook.substring(0, packageName.length() - 9);
						String[] classClean = classToHookNameOnly.split("\\/");
						String[] functionSplitValues = functionToHook.split("\\s+");
						//select
						String onlyClass = classClean[classClean.length-1];
						//String onlyFunctionParateses = functionSplitValues[functionSplitValues.length-2];
						
						String onlyFunction = CleanUpFuunction(functionSplitValues);
						//String functionToHookOnly = "dummy function";
						System.out.println(onlyClass);
						System.out.println(packageNameOnly);
						
						//Write Xposed Class 
						String XposedClassText = 
								"package androidpentesting.com.xposedmodule;"+ "\r\n" +
								"import de.robv.android.xposed.IXposedHookLoadPackage;" + "\r\n" +
								 "\r\n" +
								"import de.robv.android.xposed.XC_MethodHook;" +"\r\n" +
								"import de.robv.android.xposed.XposedBridge;" +"\r\n" +
								"import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;"+"\r\n" +
								"import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;"+"\r\n" +"\r\n" +
								"public class XposedClassTest implements IXposedHookLoadPackage {"+"\r\n" +"\r\n" +
								"	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {" + "\r\n" +"\r\n" +
								"		String classToHook = " + "\"" + packageNameOnly + "." + onlyClass + "\" ;" + "\r\n" +
								"		String functionToHook = "+"\""+ onlyFunction+"\";"+"\r\n" +
								"		if (lpparam.packageName.equals("+"\""+packageNameOnly+ "\""+")){"+ "\r\n" +
								"			XposedBridge.log(" + "\" Loaded app: \" " + " + lpparam.packageName);"+ "\r\n" +"\r\n" +
								"			findAndHookMethod("+"\""+onlyClass+"\"" + ", lpparam.classLoader, "+" \"" +onlyFunction + "\""+", int.class,"+ "\r\n" +
				                "			new XC_MethodHook() {"+ "\r\n" +
				                "			@Override"+ "\r\n" +
				                "		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {"+ "\r\n" +
				                "		//TO BE FILLED BY ANALYST {"+ "\r\n" +
				                "			}"+ "\r\n" +
				                "		});"+"\r\n" +
				                "	}"+ "\r\n" +
				                "}"+ "\r\n" +
				                "}"+ "\r\n" 
				                //"}"+ "\r\n" 
								;
						FileWriter fw = new FileWriter(file.getAbsoluteFile());
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(XposedClassText);
						bw.write("\r\n");
						bw.close();
	
						System.out.println("Done");
		 		} catch (IOException e) {
			 e.printStackTrace();
		 	}
		}
	 }
	
	private static List <String> ProcessContentExtractedClass(String contentFile){
		Scanner scanner = null;
		try{
			 scanner = new Scanner(contentFile);		
					  //@TODO : Improve patterns to match other excepts 'public'
					   String regexclass = "public";
					   //String regexPkg = "package";
					   Pattern pattern = Pattern.compile(regexclass, Pattern.CASE_INSENSITIVE);
					   //Pattern patternVoid = Pattern.compile(regexVoid , Pattern.CASE_INSENSITIVE);
					  // Pattern patternPkg = Pattern.compile(regexPkg , Pattern.CASE_INSENSITIVE);
					   //scanner.useDelimiter(";");			   
					   while (scanner.hasNextLine()) {				   
						
					   String line = scanner.nextLine();
					   // process the line
					   Matcher matcher = pattern.matcher(line);
					   while (matcher.find()){					   
					   
						   if (matcher.group() != null)
						   {
						   System.out.println("find() found the pattern \"" + quote(line.trim())) ;
						   methodsNames.add(quote(line.trim()));						   
						   }
						   else
						   {
							   methodsNames.add("No methods found");
						   }
					   		
					     }
				   
					   }					
		  		
					   if (methodsNames.isEmpty())
					   {
						   methodsNames.add("No methods found");				   
					   }
					   else
					   {
						   return methodsNames;
					   }
					return methodsNames;
		}
		finally {
			if (scanner!=null)
				scanner.close();
		}
		}
	
	private static List <String> ProcessCleanMethodsAll(List<String> rawMethods)
	{
		for (String m:rawMethods)
		{
			//Exclude class declaration
			//TODO:add a list containing all possible types
			if (m.contains("extends") || (m.contains("implements") || (!m.contains("("))))
			{
				continue;
			}
			else
			{
				cleanMethodsNames.add(m);
			}
		
		}
		return cleanMethodsNames;
		
		
	}
	
	private static String CleanUpFuunction(String[] rawFunction)
	{
		String onlyFunc = "functiondummy";
			for (String m:rawFunction)
			{
				if(m.contains("("))
				{					
					String[] functions = m.split("[ ,()]+");
					onlyFunc = functions[functions.length -1];
					return onlyFunc;
				}
				else
				{
					continue;
				}
			}

			return onlyFunc;
			
		}
	

	
	private static String ProcessContentExtractedPackage(String contentFile){
		Scanner scanner = null;
		try {
		    scanner = new Scanner(contentFile);		
		   	String regexPkg = "package";
		    Pattern patternPkg = Pattern.compile(regexPkg , Pattern.CASE_INSENSITIVE);
		    String line = scanner.nextLine();
			// process the line
			   Matcher matcher = patternPkg.matcher(line);
			   while (matcher.find()){					   
				   
				   if (matcher.group() != null)
				   {
				   System.out.println("find() found the pattern \"" + quote(line.trim())) ;
				   foundpckg  = quote(line.trim());
				   
				   }
				   else
				   {
					   foundpckg  = "";
				   }
			   }

		   if (foundpckg.isEmpty() || foundpckg == null)
		   {
			   foundpckg  = "No Package Found";				   
		   }				   		
		}
		finally
		{
			if(scanner != null)
				scanner.close();
		}
		return foundpckg;

}
	
	private static String quote(String aText){
		    String QUOTE = "'";
		    return QUOTE + aText + QUOTE;
		  }	

	//PRIVATE
	private static List<String> methodsNames = new ArrayList<String>();
	private static List<String> cleanMethodsNames = new ArrayList<String>();
	private static List<String> cleanFunctionNames = new ArrayList<String>();
	private static String foundpckg;
	private final static Charset ENCODING = StandardCharsets.UTF_8;		
}
