import java.io.*;
import java.util.*;

public class myshell {
    // Global variables
    private static ProcessBuilder pb;
    private static String[] systemCommands = new String[] { "cd", "clr", "dir", "environ", "echo", "help", "pause",
            "quit", "ls", "env", "cls", "set", "myshell"};
    private static File currentDirectory;

    public static void main(String[] args) throws IOException {
        String[] initialProcessBuilderCommand = new String[] {"bin/bash","-c"};
        currentDirectory = new File(System.getProperty("user.dir"));
        constructProcessBuilder(initialProcessBuilderCommand);
        Scanner in = new Scanner(System.in);

        try {
            boolean stopped = false;
            String[] commands;
            String value;

            while (!stopped) {
                System.out.print("shell=" + pb.directory() + "> ");
                value = in.nextLine().trim();
                commands = value.split(" ");

                if (value.equals("quit")) {
                    System.out.println("Shell terminated.");
                    stopped = true;
                    break;
                } else if (!value.isEmpty()) {
                    if (value.equals("clr")) {
                        clearCommand();
                    } else if (isSystemCommand(commands[0])) {
                        runProcess(commands);
                    } else {
                        System.out.println("That was not a system command");
                    }
                }
                System.out.println();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } catch (InterruptedException ex) {
            System.out.println("InterruptedException");
            Thread.currentThread().interrupt();
        } finally {
            in.close();
        }

    }

    public static void runProcess(String[] commandArray) throws IOException, InterruptedException {
        List<String> input = new ArrayList<String>(Arrays.asList(commandArray));
        String[] command = new String[]{"/bin/bash", "-c", input.get(0)};
        constructProcessBuilder(command);

        if (input.size() > 1) {
            if (input.size() == 2) {
                if (input.get(0).equals("cd")) {
                    cdNewDirCommand(input);
                } else if (input.get(0).equals("echo")) {
                    echoCommand(input);
                } else {
                    if (input.get(0).equals("myshell")) {
                        runFile(commandArray);
                    } else {
                        System.out.println("Incorrect command arguments");
                    }
                }
            } else if (input.size() > 2) {
                if (input.get(0).equals("echo")) {
                    echoCommand(input);
                } else {
                    System.out.println("Error: this myshell can not handle that command");
                }
            }
        } else {
            if (input.get(0).equals("cd")) {
                currentDirectory = new File(System.getProperty("user.dir"));
                pb.directory(currentDirectory);
            } else if (input.get(0).equals("pause")) {
                try {
                    Scanner in2 = new Scanner(System.in);
                    long millis = 2000;
                    System.out.println("Press enter to resume process.");
                    Thread.sleep(millis);
                    in2.nextLine();
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else if (isSystemCommand(input.get(0))) {
                constructStream();
            }
        }
    }

    public static boolean isSystemCommand(String value) {
        boolean isSystem = false;
        for (int i = 0; i < systemCommands.length; i++) {
            if (systemCommands[i].equals(value)) {
                isSystem = true;
                break;
            }
        }
        return isSystem;
    }

    public static void echoCommand(List<String> input) {
        String echo = "";
        for (int i = 1; i < input.size(); i++) {
            echo = echo + " " + input.get(i);
        }
        System.out.println(echo);
    }

    public static void clearCommand() {
        for (int i = 0; i < 20; i++) {
            System.out.println();
        }
    }

    public static void cdNewDirCommand(List<String> input) throws IOException {
        File testDirectory = new File(currentDirectory.getAbsolutePath(), input.get(1) + "/");
        if (testDirectory.isDirectory()) {
            currentDirectory = new File(currentDirectory.getAbsolutePath(), input.get(1) + "/");
            pb.directory(currentDirectory);
            pb.start();
        } else {
            System.out.println("Error: No such file or directory");
        }
    }

    public static void constructProcessBuilder(String[] command) throws IOException {
        pb = new ProcessBuilder(command);
        pb.directory(currentDirectory);

        // Debug
        // System.out.printf("Output of %s is:\n", Arrays.toString(command));
        // System.out.println("Shell success.");

    }

    public static void constructStream() throws IOException, InterruptedException {
        Process p = pb.start();
        InputStream stream = p.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(reader);

        constructOutput(br);

        // Debug
        // int shellStatus = p.waitFor();
        // System.out.println("Exit status " + shellStatus);

        stream.close();
    }

    public static void constructOutput(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

    public static void runFile(String[] commandArray) throws IOException, InterruptedException {
        List<String> input = new ArrayList<String>(Arrays.asList(commandArray));
        File filePath = new File(currentDirectory.getAbsolutePath(),input.get(1));

        if (filePath.exists()) {
            FileReader reader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String[] commands;
            String value;

            while ((value = bufferedReader.readLine()) != null) {
                commands = value.split(" ");
                runProcess(commands);
            }
        }
    }
}