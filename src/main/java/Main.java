
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
            System.out.println(cmd + ": command not found");
        }
    }
}
