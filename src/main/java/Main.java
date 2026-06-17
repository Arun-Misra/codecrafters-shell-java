
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            if (!s.hasNextLine()) {
                break;
            }
            String cmd = s.nextLine();
            if(cmd.equals("exit")){
                break;
            }
            if(cmd.startsWith("echo ")){
                System.out.println(cmd.substring(5));
            }
            if(cmd.startsWith("type ")){
                if(cmd.substring(5).equals("exit") || cmd.substring(5).equals("echo")|| cmd.substring(5).equals("type")){
                System.out.println(cmd.substring(5) + "is a shell builtin");}
                else{
                    System.out.println(cmd.substring(5) + ": not found");
                }
            }
            else{
            System.out.println(cmd + ": command not found");}
        }
    }
}
