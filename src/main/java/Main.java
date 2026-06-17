
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

            String command = s.nextLine();
            System.out.println(command + ": command not found");
        }
    }
}
