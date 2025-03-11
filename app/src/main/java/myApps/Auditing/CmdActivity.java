package myApps.Auditing;//

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

import java.io.File;
//import java.nio.file.Paths;
//import java.nio.file.Path;
import java.nio.file.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class CmdActivity extends Activity {
    
  public static CmdActivity cmdActivity;
  static String IN_text = "";
  static EditText IN;
  static String OUT_str = "";
  static TextView OUT;
  public static String BASE_PATH = "/storage/emulated/0/";
  static Handler events;
  static Intent textualDataActivity;

  static String PWD = BASE_PATH;
    public static ArrayList<String> cmdHistory = new ArrayList<String>();
    public static int cmdIndex = 0;
    public static String curCmd;
    public static String[] cmds = {"cmds","ls","cd","pwd"};
    static ArrayList<Cmd> cmdlines = new ArrayList<Cmd>();
    static int[] seggPos = new int[]{0,0};
  private static boolean isLoaded = false;
    @Override
    protected void onDestroy() {
      cmdActivity = null;
      textualDataActivity = null;
      super.onDestroy();
    }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    try{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_cmd);
        cmdActivity = this;
        textualDataActivity = new Intent(this, TextualDataActivity.class);

        events = new Handler(Looper.getMainLooper());

        OUT = findViewById(R.id.output_box);
        OUT.setText(OUT_str);
        IN = findViewById(R.id.input_box);
        IN.setText(IN_text);
        findViewById(R.id.exe_button).setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v){
              String cmd = IN.getText().toString();
              IN.setText("");
              exe(cmd);
            }
          }
        );
    findViewById(R.id.tab_button).setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v){
          int l = IN.getSelectionStart();
          String os = IN.getText().toString();
          String s = os.substring(0,l);
          if (!s.contains(" ")) {
            String output="";
            String[] segg = seggest(s,cmds);
            if (segg != null && segg.length == 1) {
              IN.setText(segg[0]);
              IN.setSelection(IN.getText().length());
            }
            else if (segg != null){
              for (String word: segg) 
                output += word+"\n";
            }
            segg = seggest(s, getAllCmds());
            if (segg != null) {
              for (String line: segg)
                output += line+"\n";
            }
            CmdActivity.seggPos = println(output, seggPos[0], seggPos[1]);
            
          }
          else { // writed many words
            int h=l-1;
            for (; h>=0; h--){
              if (s.charAt(h) == ' ' || s.charAt(h) == '"'){
                s = s.substring(h+1,l);
                break;
              }
            }
            
            String[] sfs = s.split("/");
            String pre = sfs[sfs.length-1];
            sfs[sfs.length-1] = "";
            String sf = String.join("/", sfs); 
            String[] dirs = seggest(pre, ls(sf));
            if (dirs != null && dirs.length == 1){
              s = sf+dirs[0];
              if (s.contains(" ") && os.charAt(h) != '"')
                s= "\""+s;
              IN.setText(os.substring(0,h+1)+s+os.substring(l,os.length()));
              IN.setSelection(IN.getText().length());
            }
            else if (dirs != null){
              for(int i=0; i<dirs.length; i++){
                print(dirs[i]+"\n");
              }
            }

          }
        }
          }
        );
        findViewById(R.id.cmd_up_button).setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v){
              if (cmdIndex > 0) {
                if (cmdIndex == cmdHistory.size()) curCmd = IN.getText().toString();
                cmdIndex -= 1;
                IN.setText(cmdHistory.get(cmdIndex));
              }
            }
          }
        );
        findViewById(R.id.cmd_down_button).setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v){
              if (cmdIndex < cmdHistory.size()){
                cmdIndex += 1;
                if (cmdIndex == cmdHistory.size()) IN.setText(curCmd);
                else IN.setText(cmdHistory.get(cmdIndex));
              }
            }
          }
        );
        if (!isLoaded){
          loadCmds();
          isLoaded = true;
        }
    } catch (Exception e) {println(e.toString());}
  }
  static void openTextualDataActivity(String data, Runnable function){
    if (textualDataActivity == null)
      if (cmdActivity != null)
        textualDataActivity = new Intent(cmdActivity, TextualDataActivity.class);
      else
        textualDataActivity = new Intent(MainActivity.mainActivity, TextualDataActivity.class);
    TextualDataActivity.data = data;
    TextualDataActivity.function = function;
    cmdActivity.startActivity(textualDataActivity);
  }
  static void openTextualDataActivity(Runnable function){
    openTextualDataActivity(null, function);
  }
    static String[] seggest(String pre, String[] words) {
      ArrayList<String> fwords = new ArrayList<String>();
      int len = pre.length();
      for (String word : words){
        if (word.length() >= len && pre.equals(word.substring(0,len)) ) {
          fwords.add(word);
        }
      }
      if (fwords.size()>0){
        String[] filtered = fwords.toArray(new String[fwords.size()]);
        return filtered;
      } else return null;
    }
    static void keepOnINbox(String s){
      IN_text = s;
      if (cmdActivity != null)
        IN.setText(IN_text);
    }
    static int[] print(String s){ //[s,e[
      int[] i = new int[2];
      i[0] = OUT_str.length();
      OUT_str += s;
      if (cmdActivity != null)
        OUT.setText(OUT_str);
      i[1] = i[0]+s.length();
      return i;
    }
    static int[] print(String s, int b, int e){
      int[] i = new int[]{b, b+s.length()};
      String old = OUT_str;
      OUT_str = old.substring(0,b)+s+old.substring(e,old.length());
      if (cmdActivity != null)
        OUT.setText(OUT_str);
      return i;
    }
    static int[] println(String s){return print(s+"\n");}
    static int[] println(String s, int b, int e){return print(s+"\n", b, e);}
    static String[] splitInput(String s){
      ArrayList<String> args = new ArrayList<String>();
      args.add("");
      int index = 0;
      char sp = ' ';
      for(int i=0; i<s.length(); i++){
        if (s.charAt(i) == sp){
          if (!args.get(index).equals("")) {
            args.add(""); index++;
          }
          sp = ' ';
        }
        else if (s.charAt(i) == '"'){
          if (!args.get(index).equals("")) {
            args.add(""); index++;
          }
          sp = '"';
        }
        else
            args.set(index, args.get(index)+s.charAt(i));
      }
      if (args.size()>1 && args.get(index).equals(""))
         args.remove(args.size()-1);
      return (String[]) args.toArray(new String[args.size()]);
    }
  private static String[] parseCmdOptions(String cmdl) {
    ArrayList<String> options = new ArrayList<String>();
    options.add("");
    int index = 0;
    for (int i=0; i<cmdl.length(); i++){
      if (cmdl.charAt(i) == '|'){
        options.add(""); index++;
      }
      else 
        options.set(index, options.get(index)+cmdl.charAt(i));
    }
    return (String[]) options.toArray(new String[options.size()]);
  }
  static String[] parseCmd(String cmdl){
    ArrayList<String> cmdlns = new ArrayList<String>();
    cmdlns.add("");
    boolean optionsMode = false;
    int lh=0, lt, size;
    int inner = 0; // for inner bracket if opened +1 if closed -1;
    String[] options;
   try{
    for (int i=0; i<cmdl.length(); i++){
      if (Set.of('[','(').contains(cmdl.charAt(i))){
        inner += 1;
        optionsMode = true;
        if (inner > 1) continue;
        lh = i;
      }
      else if (cmdl.charAt(i) == ')'){
        inner --;
        if (inner > 0) continue;
        optionsMode = false;
        lt = i;
        size = cmdlns.size();
        options = parseCmdOptions(cmdl.substring(lh+1, lt));
        for (int j=1; j<options.length; j++){
          String[] subOptions = parseCmd(options[j]);
          for (String subOption: subOptions){
            for (int ln=0; ln<size; ln++){
              cmdlns.add(cmdlns.get(ln)+subOption);
            }
          }
        }
        String[] subOptions = parseCmd(options[0]);
        for (String subOption: subOptions){
          for (int ln=0; ln<size; ln++)
            cmdlns.set(ln, cmdlns.get(ln)+subOption);
        }
      }
      else if (cmdl.charAt(i) == ']') {
        // [] mode_option
        inner --;
        if (inner > 0)
          continue;
        optionsMode = false;
        lt = i;
        size = cmdlns.size();
        String[] regulareLines = parseCmd(cmdl.substring(lh+1, lt));
        for (String line: regulareLines){
          for (String option: parseCmdOptions(line)){
            for (int ln=0; ln<size; ln++) {
              cmdlns.add(cmdlns.get(ln)+option);
            }
          }
        }
      }
      else if (!optionsMode){
        for (int j=0; j<cmdlns.size(); j++)
          if (cmdl.charAt(i)==' '&& cmdlns.get(j).charAt(cmdlns.get(j).length()-1) == ' ') {
          }
          else cmdlns.set(j, cmdlns.get(j)+cmdl.charAt(i));
      }
    }
   } catch (Exception e) {
      println("Exception{ in cmdParse(\""+cmdl+"\"), ["+e.toString()+"]");
      println("stoped after parse:");
      for (String cmdln: cmdlns)
        println("|- "+cmdln);
      println("}");
   }
    return (String[]) cmdlns.toArray(new String[cmdlns.size()]);
  }
  static String[] parseCmdInput(String cmdln) {
    ArrayList<String> argsTypes = new ArrayList<String>();
    char argType = '\0'; //n, s, e(identifier)
    String digits = "0123456789";
    for (int c=0; c<cmdln.length(); c++){
      if (cmdln.charAt(c)==' '&& argType != 's') {
        argType = '\0';
      }
      else if (argType != 's'&& '"' == cmdln.charAt(c)){
        argType = 's';
        argsTypes.add("<str>");
      }
      else if (argType == 's'){
        if (cmdln.charAt(c) == '"')
          argType = '\0';
      }
      else if (argType != 'e'&& digits.indexOf(cmdln.charAt(c))!=-1){
        if (argType != 'n'){
          argType = 'n';
          argsTypes.add("<num>");
        }
      }
      else if (argType != 'e'){
        argType = 'e';
        argsTypes.add(cmdln.charAt(c)+"");
      }
      else {
        argsTypes.set(argsTypes.size()-1, argsTypes.get(argsTypes.size()-1)+cmdln.charAt(c));
      }
    }
    if (argsTypes.size()>0){
        return (String[]) argsTypes.toArray(new String[argsTypes.size()]);
    } else return null;
  }
  static class Cmd{
    Executable exe;
    String cmdln;
    String[] parsedCmdln;
    Cmd(Executable exe, String cmdln) {
      this.exe=exe; this.cmdln=cmdln;
      parsedCmdln = cmdln.split(" ");
      for (int i=0; i<parsedCmdln.length; i++){
        String parts[] = parsedCmdln[i].split(":");
        parsedCmdln[i] = parts[parts.length-1];
      }
    }
  }
  static void addCmd(String cmdline, Executable erun) {
    String[] cmdlns = parseCmd(cmdline);
    for(String cmdln: cmdlns){
      cmdlines.add(new Cmd(erun, cmdln));
    }
  }
  static Cmd[] findCmd(String inputln){
    ArrayList<Cmd> cmds = new ArrayList<Cmd>();
    String[] parsed = parseCmdInput(inputln);
    for (Cmd cmdline: cmdlines) {
      if (parsed.length == cmdline.parsedCmdln.length){
        int i=0;
        for (; i<parsed.length; i++){
          if (!parsed[i].equals(cmdline.parsedCmdln[i])){
            break;
          }
        }
        if (i==parsed.length)
          cmds.add(cmdline);
        }
    }
    if (cmds.size()>0)
      return (Cmd[]) cmds.toArray(new Cmd[cmds.size()]);
    return null;
  }
  static String[] getAllCmds(){
    ArrayList<String> cmds = new ArrayList<String>();
    for (Cmd cmdln: cmdlines)
      cmds.add(cmdln.cmdln);

    if (cmds.size()>0)
      return (String []) cmds.toArray(new String[cmds.size()]);
    else return null;
  }
  static void loadCmds(){
    try{
      addCmd("clear", new Executable(){
        @Override
        public void run(String args[], String targs[]) {
          CmdActivity.OUT.setText("");
        }
      });
    }catch (Exception e) {println(e.toString());}
  }
  static void exe(String cmd){
    try{
        cmdHistory.add(cmd); cmdIndex += 1;
        seggPos = print("", seggPos[0], seggPos[1]);
        print("~$ "+cmd+"\n");
        String argvs[] = splitInput(cmd);
        String argv0 = argvs[0], argv = String.join(" ", Arrays.copyOfRange(argvs, 1, argvs.length));;
        Cmd[] commands = findCmd(cmd);
      if (commands != null) {
        if (commands.length == 1){
          String args[], targs[];
          args = Arrays.copyOfRange(argvs, 1, argvs.length);
          targs = parseCmdInput(cmd.substring(argv0.length(), cmd.length()));
          commands[0].exe.run(args, targs);
        }
        else {
          println("there are many commands");
          for (Cmd command: commands)
            println(" "+command.cmdln);
        }

      }
      else if (argv0.equals("args")){
        for (int i=0; i<argvs.length; i++)
          println(i+"@ "+argvs[i]);
      }
      else if (argv0.equals("cmdln")){
        for (String cmdln: parseCmd(argvs[1]))
          println(cmdln);
      }
      else if(argv0.equals("arg-type")){
        for (String arg: parseCmdInput(cmd)){
          println(arg);
        }
      }
      else if(argv0.equals("is-cmd")){
        String cmdln = cmd.substring(argv0.length(), cmd.length());
        commands = findCmd(cmdln);
        if (commands != null)
          for (Cmd command: commands)
            println(command.cmdln);
      }
      else  if (argv0.equals("cmds")){
          for (int i=0; i<cmds.length; i++){
            print(cmds[i]);
            if (i != 0 && i % 5 == 0) print("\n");
            else print(", ");
          }
        }
      else if (argv0.equals("ls")) {
        String lines = new String();
         for (String filePath: ls(argv)){
           lines += filePath +"\n";
         }
         print(lines);
      }
        else if (argv0.equals("cd")) {
                File file = new File(PWD+argv);
                if(file.exists() && file.isDirectory()) {
                    PWD = (Paths.get(PWD+argv)).normalize().toString()+"/";
                }
                else print("Error cd: not found");
            }
        else if (argv0.equals("pwd")) {
                print(PWD+"\n");
            }
      else println("not cmd");
    } catch(Exception e) {print(e.toString()+"\n");}
  }
    public static boolean cp(String src, String dest){
      Path source = Paths.get(src);
      Path destination = Paths.get(dest);

        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
          return true;
        } catch (IOException e) {
          print("Err: "+e.toString());
          return false;
        }
    }
    public static String[] ls(String path){
        ArrayList<String> lines = new ArrayList<String>();
        String linesArr[];
        try {
            // new ProcessBuilder("myCommand", "myArg1", "myArg2");
            ProcessBuilder processBuilder = new ProcessBuilder("ls",PWD+path);
            Process process = processBuilder.start() ;
            
            // Get input and output streams
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            // Read output
            String line ;
            while ((line = reader.readLine()) != null) {
              if ((new File(PWD+path+line)).isDirectory())
                line += "/";
              lines.add(line);
            }
            
            // Read error (if any)
            while ((line = errorReader.readLine()) != null) {
                println("Error: " + line);
            }
            
        }
        catch (IOException e){e.printStackTrace();}
        if (lines.size()>1){
          linesArr = lines.toArray(new String[lines.size()]);
          return linesArr;
        } else return null;
    }
    public static String readFile(String absulotePath){
        String data = "";
        try {
                    FileInputStream fis = new FileInputStream(new File(absulotePath));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                    String line;
            if ((line = reader.readLine()) != null){
                        data += line;
            }
            while ((line = reader.readLine()) != null){
                        data += "\n" + line;
            }
                    return data;
        } catch (IOException e) { return null; }
        
    }
    private static boolean writeFile(String absulotePath, String data){
        try {
            FileOutputStream fis = new FileOutputStream(new File(absulotePath));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fis));
            writer.write(data); writer.flush();
            return true;
        } catch (IOException e) { return false; }
    }
}
abstract class Executable implements Runnable{
    @Override
    public void run(){}
    abstract public void run(String args[], String targs[]);
  }
