package myApps.Auditing;

import android.app.Activity;
import android.os.Bundle;

import android.content.Context;
//import android.Manifest;
//import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
//import CustomAdapter1;

import java.util.ArrayList;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.lang.reflect.Array;

public class DirectoryActivity extends Activity {
    
    public static String currentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static String currentDirectory = currentPath;
    public static String baseDirectory = currentDirectory;
    public static EditText searchBox;
    public static File[] files;
    public static ArrayList<String> filesName;
    public static Directory rootDir = null;
    public static Directory currentDir = null;
    public static Runnable function = null;
    public static Runnable searchFunction = null;
    static int selectedItemIndex = -1;
    
    private static ListView filesListView;
    private static ArrayAdapter<String> adapter;
    //private static CustomAdapter1 adapter;
    private static DirectoryActivity directoryActivity;
    
  @Override
  protected void onDestroy() {
    rootDir = null;
    currentDir = null;
    function = null;
    searchFunction = null;
    selectedItemIndex = -1;
    super.onDestroy();
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_directory);

    directoryActivity = this;
    filesListView = findViewById(R.id.files_list);
    searchBox = (EditText) findViewById(R.id.textBox_search);
    findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v){
        if (null == DirectoryActivity.searchFunction){

        }
        else if (!"".equals(searchBox.getText().toString())){
          DirectoryActivity.searchFunction.run();
        }
      }
    });

    AdapterView.OnItemClickListener defaultItemClickListener = new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Handle item click here
        //String selectedItem = parent.getItemAtPosition(position).toString();
        String selectedItem = filesName.get(position);
        if (selectedItem.equals("..")){
          String[] parts = currentDirectory.split("/");
          String[] newParts = new String[parts.length-1];
          System.arraycopy(parts, 0, newParts, 0, newParts.length);
          currentPath = String.join("/",newParts);
        } else currentPath = currentDirectory+selectedItem;

        File file = new File(DirectoryActivity.currentPath);
        if (file.isDirectory()){
          currentDirectory = currentPath+"/";
          defaultReload();
        }
        else{
          if (function != null)
            function.run();
//        MainActivity.fileNameView.setText(selectedItem);
//        MainActivity.playsound();
          finish();
        }
      }
    };
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (currentDir.getParent() != null && position == 0) {
            currentDir = currentDir.getParent();
            reload();
        }
        else {
          if (currentDir.getParent() != null)
            position -= 1;
          if (position < currentDir.getDirs().length){
            currentDir =  currentDir.getDirs()[position];
            reload();
          }
          else {
            position -= currentDir.getDirs().length;
            selectedItemIndex = position;
            if (function != null)
              try {
                function.run();
              } catch (Exception e){
                CmdActivity.println("Exception on call Rumnable function on DirectoryActivity : "+e.toString());
              }
            finish();
          }
        }
      }
    };

    if (rootDir == null) {
      filesListView.setOnItemClickListener(defaultItemClickListener);
      defaultReload();
    }
    else {
      currentDir = rootDir;
      filesListView.setOnItemClickListener(itemClickListener);
      reload();
    }
  }

  static void defaultReload(){
    //implementation files("libs/CustomAdapter1.java");
    filesName = new ArrayList<String>();
    File directory = new File(currentDirectory);
    if (directory.exists() && directory.isDirectory()){
      files = directory.listFiles();
      if (files != null) {
        // Sort files by name
        Arrays.sort(files, new Comparator<File>() {
          @Override
          public int compare(File f1, File f2) {
            // Sort files by name
            //return f1.getName().compareTo(f2.getName());
            // Sort files by last modified date
            return Long.compare(f1.lastModified(), f2.lastModified());
          }
        });
      }
      if (!currentDirectory.equals(baseDirectory))
        filesName.add("..");
      for (File file: files)
        filesName.add(file.getName());

      if (!filesName.contains("playlist.txt"))
         adapter = new ArrayAdapter<>(directoryActivity, android.R.layout.simple_list_item_1, filesName);
      else {
         ArrayList<String> num_filesName = new ArrayList<String>();
         int j = 1;
         for (String fn : filesName) {
           if (fn.equals("..") || fn.equals("playlist.txt")) 
             num_filesName.add(fn);
           else{
             num_filesName.add(j+"@ "+fn);
             j++;
           }
         }
         adapter = new ArrayAdapter<>(directoryActivity, android.R.layout.simple_list_item_1, num_filesName);
      }
      filesListView.setAdapter(adapter);

      if (files != null){// && DirectiryActivity.files.length != 0){
      } else{
         Toast.makeText(directoryActivity, "empty-folder or permission", Toast.LENGTH_SHORT).show();
         //finish();
      }
    } else{
      Toast.makeText(directoryActivity, "diroctory not-found", Toast.LENGTH_SHORT).show();
      //finish();
    }
  }
  static void reload(){
    String[] itemsNames = currentDir.getLocal();
    adapter = new ArrayAdapter<>(directoryActivity, android.R.layout.simple_list_item_1, itemsNames);
    filesListView.setAdapter(adapter);
  }
  public static void openDirectoryActivity(Activity activity, Directory rootDir, Runnable function, Runnable searchFunction){
    DirectoryActivity.rootDir = rootDir;
    DirectoryActivity.function = function;
    DirectoryActivity.searchFunction = searchFunction;
    activity.startActivity(MainActivity.directoryActivity);
  }
  public static void openDirectoryActivity(Activity activity, Directory rootDir, Runnable function){
    openDirectoryActivity(activity, rootDir, function, null);
  }
  public static void openDirectoryActivity(Activity activity){
    openDirectoryActivity(activity, null, null, null);
  }
}

//class DirectoryItem{
//  String name;
//  DirectoryItem(String name){
//    this.name = name;
//  }
//}
//class Directory extends DirectoryItem{
//  private Directory parent;
//  private ArrayList<DirectoryItem> items = new ArrayList<>();
//  private ArrayList<Directory> directories = new ArrayList<Directory>();
//  private ArrayList<DirectoryItem> files = new ArrayList<>();
//
//  Directory (String name){
//    super(name);
//  }
//  Directory getBack(){
//    return parent;
//  }
//  
//  Directory getChildDir(String dirName){
//    for (Directory dir: directories){
//      if (dir.name.equals(dirName))
//        if (dir.parent == null)
//          dir.parent = this;
//        return dir;
//    }
//    return null;
//  }
//  Directory findDir(String dirPath){
//    Directory curDir = this;
//    String[] childNames = dirPath.split("/");
//    for (String name: childNames){
//      if (!name.equals("")){
//        curDir = getChildDir(name);
//        if (curDir == null)
//          return null;
//      }
//    }
//    return curDir;
//  }
//  String[] getList() {
//    ArrayList<String> list = new ArrayList<String>();
//    if (parent != null)
//      list.add("..");
//    for (DirectoryItem item: items) {
//      list.add(item.name);
//    }
//   return (String[]) list.toArray(new String[list.size()]);
//  }
//  DirectoryItem[] getItems(){
//    return (DirectoryItem[]) items.toArray(new DirectoryItem[items.size()]);
//  }
//  void addDir(Directory d){
//      // if has parent move it to that parent
//    d.parent = this;
//    directories.add(d);
//    items.add(d);
//  }
//  void addFile(DirectoryItem item){
//    files.add(item);
//    items.add(item);
//  }
//}
class Directory <ItemType> {
  String name;
  private Directory<ItemType> parent = null;
  private ArrayList<Directory<ItemType>> dirs = new ArrayList<>();
  private ArrayList<ItemType> items = new ArrayList<>();

  Directory(String name){
    this.name = name;
  }
  Directory<ItemType> getParent(){
    return parent;
  }
  Directory[] getDirs(){
    return (Directory[]) dirs.toArray(new Directory[dirs.size()]);
  }
  ItemType[] getItems(Class<ItemType> clazz){
    //return (ItemType[]) items.toArray(new ItemType[items.size()]);
    //return items.toArray((ItemType[]) Array.newInstance(ItemType.class, items.size()));
    return (ItemType[]) Array.newInstance(clazz, items.size());
  }
  ItemType getItem(int index) {
    if (index < items.size())
      return items.get(index);
    return null;
  }
  String getPath(){
    String path = "";
    Directory<ItemType> dir = this;
    while (dir.getParent() != null){
      if (!path.equals(""))
        path += "/";
      path += dir.name;
      dir = dir.parent;
    }
    return path;
  }
  Directory<ItemType> getChildDir(String name){
    for (int i=0; i<dirs.size(); i++){
      if (dirs.get(i).name.equals(name)){
        //CmdActivity.println("found "+dirs.get(i).name);
        return dirs.get(i);
      }
    }
    return null;
  }
  void addDir(Directory<ItemType> dir){
    for (Directory<ItemType> d: dirs){
      if (d.name.equals(dir.name)){
//        for(Directory<ItemType> subDir: dir.getDirs())
//          addDir(subDir);
//        for(ItemType item: getitems)
//          addItem(item);
        return;
      }
    }
    dir.parent = this;
    dirs.add(dir);
    //CmdActivity.println("added "+dir.name);
  }
  void addDir(String name){
    this.addDir(new Directory<ItemType>(name));
  }
  void addItem(ItemType item){
    items.add(item);
  }
  String[] getLocal(){
    ArrayList<String> list = new ArrayList<>();
    if (parent != null)
      list.add("..");
    for (Directory<ItemType> dir: dirs)
      list.add(dir.name);
    for (ItemType item: items)
      list.add(item.toString());
    return (String[]) list.toArray(new String[list.size()]);
  }
}
