
import java.io.File;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
        Scanner s = new Scanner(System.in);
        String[] pth = System.getenv("PATH").split(File.pathSeparator);
        while (true) {
            System.out.print("$ ");
            String cmd = s.nextLine();
            if (cmd.equals("exit")) {
                break;
            } else if (cmd.startsWith("echo ")) {
                System.out.println(cmd.substring(5));
            } else if (cmd.startsWith("type ")) {
                String chk = cmd.substring(5);

                if (chk.equals("exit") || chk.equals("echo") || chk.equals("type")) {
                    System.out.println(chk + " is a shell builtin");
                } else {
                    boolean fnd = false;
                    for (String dir : pth) {
                        File f = new File(dir, chk);
                        if (f.exists() && f.canExecute()) {
                            System.out.println(chk + " is " + f.getAbsolutePath());
                            fnd = true;
                            break;
                        }
                    }
                    if (!fnd) {
                        System.out.println(chk + ": not found");
                    }
                }
            }
            else{
                String[] pt = cmd.split(" ");
                String prog = pt[0];
                File exe = null;
                for(String dir : pth){
                    File f = new File(dir, prog);
                    if(f.exists() && f.canExecute()){
                        exe = f;
                        break;
                    }
                }
                if(exe!=null){
                    ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
                    pb.command().set(0, exe.getAbsolutePath());
                    pb.inheritIO();
                    Process p = pb.start();
                    p.waitFor();
                }
                else{
                    System.out.println(cmd + ": command not found");
                }
            }
        }
    }
}
