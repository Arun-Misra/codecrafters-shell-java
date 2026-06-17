
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String cmd = s.nextLine();
            if (cmd.equals("exit")) {
                break;
            } else if (cmd.startsWith("echo ")) {
                System.out.println(cmd.substring(5));
            } else if (cmd.startsWith("type ")) {
                String arg = cmd.substring(5);

                if (arg.equals("exit") || arg.equals("echo") || arg.equals("type")) {
                    System.out.println(arg + " is a shell builtin");
                } else {
                    System.out.println(arg + ": not found");
                }
            } else {
                System.out.println(cmd + ": command not found");
            }
        }
    }
}
