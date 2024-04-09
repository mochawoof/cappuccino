import java.util.Properties;
import java.util.Scanner;
import java.io.*;
import java.nio.file.*;
class Main {
	public static String HELPLINK = "https://github.com/mochawoof/cappuccino";
	public static String VERSION = "0.1.0";
	
	private static void warn(String msg) {
		System.out.println("WARNING! " + msg);
	}
	private static void error(String msg) {
		System.out.println("ERROR! " + msg);
	}
	private static void error(String msg, Exception e) {
		error(msg + "\n" + e.toString());
	}
	private static int handleProcess(String[] args, boolean inputEnabled) {
		try {
			System.out.println();
			Process process = Runtime.getRuntime().exec(args);
			if (inputEnabled) {
				String in = new Scanner(System.in).nextLine();
				OutputStream inStream = process.getOutputStream();
				inStream.write(in.getBytes());
				inStream.flush();
				inStream.close();
			}
			InputStream outStream = process.getInputStream();
			InputStream errorStream = process.getErrorStream();
			boolean hasLines = true;
			while (hasLines) {
				hasLines = false;
				int outStreamRead = outStream.read();
				if (outStreamRead > -1) {
					System.out.print((char)outStreamRead);
					hasLines = true;
				}
				int errorStreamRead = errorStream.read();
				if (errorStreamRead > -1) {
					System.out.print((char)errorStreamRead);
					hasLines = true;
				}
			}
			outStream.close();
			errorStream.close();
			return process.exitValue();
		} catch (Exception e) {
			error("", e);
			return 1;
		}
	}
	private static void printHelp() {
		System.out.println("cappuccino v" + VERSION);
		System.out.println(HELPLINK);
		System.out.println();
		System.out.println("Commands:");
		System.out.println("b: Just build everything");
		System.out.println("r [args]: Run built project");
		System.out.println("c: Cleanup all .class files");
		System.out.println("j: Make executable jar file");
		System.out.println();
		System.out.println("m [JDK path]: Make new .cappuccino file");
		System.out.println();
		System.out.println("Commands can be run one after the other. Ex: br, brc, bjc");
	}
	public static void main(String[] args) {
		if (args.length > 0) {
			boolean fileLoaded = false;
			Properties file = new Properties();
			try {
				file.load(new FileInputStream("./.cappuccino"));
				fileLoaded = true;
			} catch (Exception e) {
				error("Failed to load .cappuccino file! Does it exist?", e);
			}
	
			String arg = args[0].toLowerCase();
			String secondaryArg = "";
			if (args.length > 1) {
				secondaryArg = args[1];
			}
			for (char c : arg.toCharArray()) {
				int exit = -1;
				System.out.println();
				if (c == 'm') { //make .cappuccino
					System.out.println("Making .cappuccino file in current directory...");
					
					try {
						String jdkToSet = "changeme";
						String envJdk = System.getenv("JAVA_HOME");
						if (!secondaryArg.isEmpty()) {
							jdkToSet = secondaryArg;
						} else if (envJdk != null) {
							jdkToSet = envJdk;
						} else {
							warn("JDK directory not found! You must set it yourself!");
						}
						file.setProperty("jdk", jdkToSet);
						file.setProperty("madefor", "2.0");
						file.setProperty("name", "project");
						file.setProperty("main", "Main");
						file.setProperty("include", "*.class");
						
						file.store(new FileWriter(".cappuccino"), HELPLINK);
						exit = 0;
					} catch (IOException e) {
						error("Failed to save .cappuccino file! Do you have write permissions?", e);
						exit = 1;
					}
				}
				if (fileLoaded) {
					if (file.getProperty("jdk") != null) {
						Runtime runtime = Runtime.getRuntime();
						Path jdkPath = Paths.get(file.getProperty("jdk"));
						if (c == 'b') { //build
							System.out.println("Building...");
							exit = handleProcess(new String[] {
								jdkPath.resolve("bin/javac.exe").toString(),
								"*.java"}, false);
						} else if (c == 'r') { //run
							System.out.println("Running...");
							System.out.print("Enter program input: ");
							exit = handleProcess(new String[] {
								jdkPath.resolve("bin/java").toString(),
								file.getProperty("main"),
								secondaryArg}, true);
						} else if (c == 'c') { //clean
							System.out.println("Cleaning...");
							File[] files = new File(".").listFiles();
							int deleted = 0;
							for (File f : files) {
								if (f.getPath().endsWith(".class")) {
									f.delete();
									deleted++;
								}
							}
							System.out.println("Deleted " + deleted + ((deleted == 1) ? " file" : " files") + ".");
							exit = 0;
						} else if (c == 'j') { //make jar
							System.out.println("Making jar..." + (new File(".").getPath()));
							exit = handleProcess(new String[] {
								jdkPath.resolve("bin/jar").toString(),
								"cvfe",
								file.getProperty("name") + ".jar",
								file.getProperty("main"),
								file.getProperty("include")}, false);
						}
						if (exit != 0) {
							error("Failed! Code: " + exit);
						} else {
							if (c != 'r') { //prevent clutter when running
								System.out.println("Done!");
							}
						}
					} else {
						error("JDK not found!");
					}
				}
			}
		} else {
			printHelp();
		}
	}
}