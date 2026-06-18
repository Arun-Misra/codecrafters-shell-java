import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);

        String[] pth = System.getenv("PATH").split(File.pathSeparator);
        File currentDir = new File(System.getProperty("user.dir"));

        while (true) {
            System.out.print("$ ");

            String cmd = s.nextLine();
            List<String> parts = parse(cmd);

            if (parts.isEmpty()) {
                continue;
            }

            if (cmd.equals("exit")) {
                break;
            }

            else if (parts.get(0).equals("pwd")) {
                System.out.println(currentDir.getAbsolutePath());
            }

            else if (parts.get(0).equals("cd")) {
                if (parts.size() < 2) {
                    continue;
                }

                String path = parts.get(1);
                File newDir;

                if (path.equals("~")) {
                    newDir = new File(System.getenv("HOME"));
                } else if (new File(path).isAbsolute()) {
                    newDir = new File(path);
                } else {
                    newDir = new File(currentDir, path);
                }

                if (newDir.exists() && newDir.isDirectory()) {
                    currentDir = newDir.getCanonicalFile();
                } else {
                    System.out.println("cd: " + path + ": No such file or directory");
                }
            }

            else if (parts.get(0).equals("echo")) {
                for (int i = 1; i < parts.size(); i++) {
                    if (i > 1)
                        System.out.print(" ");
                    System.out.print(parts.get(i));
                }
                System.out.println();
            }

            else if (parts.get(0).equals("type")) {
                if (parts.size() < 2) {
                    continue;
                }

                String chk = parts.get(1);

                if (chk.equals("exit") || chk.equals("echo") || chk.equals("type") || chk.equals("pwd")
                        || chk.equals("cd")) {
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
            } else {
                String prog = parts.get(0);

                File exe = null;

                for (String dir : pth) {
                    File f = new File(dir, prog);

                    if (f.exists() && f.canExecute()) {
                        exe = f;
                        break;
                    }
                }

                if (exe != null) {
                    ProcessBuilder pb = new ProcessBuilder(parts);
                    pb.directory(currentDir);
                    pb.inheritIO();

                    Process p = pb.start();
                    p.waitFor();
                } else {
                    System.out.println(cmd + ": command not found");
                }
            }
        }
    }
    static List<String> parse(String s) {
        List<String> args = new ArrayList<>();
        StringBuilder cur = new StringBuilder();

        boolean inSingle = false;
        boolean inDouble = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
            }
            else if (c == '"' && !inSingle) {
                inDouble = !inDouble;
            }
            else if (Character.isWhitespace(c) && !inSingle && !inDouble) {
                if (cur.length() > 0) {
                    args.add(cur.toString());
                    cur.setLength(0);
                }
            }
            else {
                cur.append(c);
            }
        }

        if (cur.length() > 0) {
            args.add(cur.toString());
        }

        return args;
    }
}