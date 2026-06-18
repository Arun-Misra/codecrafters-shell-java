import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
            String outputFile = null;
            String errorFile = null;
            for (int i = 0; i < parts.size(); i++) {
                String token = parts.get(i);

                if (token.equals(">") || token.equals("1>")) {
                    outputFile = parts.get(i + 1);
                    parts = new ArrayList<>(parts.subList(0, i));
                    break;
                }

                if (token.equals("2>")) {
                    errorFile = parts.get(i + 1);
                    parts = new ArrayList<>(parts.subList(0, i));
                    break;
                }
            }

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

                if (errorFile != null) {
                    Files.writeString(Path.of(errorFile), "");
                }

                StringBuilder sb = new StringBuilder();

                for (int i = 1; i < parts.size(); i++) {
                    if (i > 1)
                        sb.append(" ");
                    sb.append(parts.get(i));
                }

                String out = sb.toString();

                if (outputFile == null) {
                    System.out.println(out);
                } else {
                    Files.writeString(
                            Path.of(outputFile),
                            out + System.lineSeparator());
                }
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

                    pb.redirectError(ProcessBuilder.Redirect.INHERIT);

                    if (outputFile != null) {
                        pb.redirectOutput(new File(outputFile));
                    } else {
                        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    }

                    if (errorFile != null) {
                        pb.redirectError(new File(errorFile));
                    } else {
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    }

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

            if (inSingle) {
                if (c == '\'') {
                    inSingle = false;
                } else {
                    cur.append(c);
                }
            }

            else if (inDouble) {
                if (c == '"') {
                    inDouble = false;
                } else if (c == '\\') {
                    if (i + 1 < s.length()) {
                        char nxt = s.charAt(i + 1);
                        if (nxt == '"' || nxt == '\\') {
                            cur.append(nxt);
                            i++;
                        } else {
                            cur.append('\\');
                        }
                    } else {
                        cur.append('\\');
                    }
                } else {
                    cur.append(c);
                }
            }

            else {
                if (c == '\'') {
                    inSingle = true;
                } else if (c == '"') {
                    inDouble = true;
                } else if (c == '\\') {
                    if (i + 1 < s.length()) {
                        cur.append(s.charAt(++i));
                    }
                } else if (Character.isWhitespace(c)) {
                    if (cur.length() > 0) {
                        args.add(cur.toString());
                        cur.setLength(0);
                    }
                } else {
                    cur.append(c);
                }
            }
        }

        if (cur.length() > 0) {
            args.add(cur.toString());
        }

        return args;
    }
}