package com.bluetooth.unvarnishedtransmission;

import java.io.File;  
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.bluetooth.unvarnishedtransmission.R;
import android.app.AlertDialog;  
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;  
import android.widget.ArrayAdapter;  
import android.widget.ListView;  
import android.widget.TextView;  

public class readList extends ListActivity {  
    /** Called when the activity is first created. */  
    private List<String> items = null;//存放名称  
    private List<String> paths = null;//存放路径  
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Sendig/";  
    private TextView tv;  
    private Context context;
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.readlist);  
        tv = (TextView) this.findViewById(R.id.TV);  
        context = getApplicationContext();
        this.getFileDir(rootPath,"csv");//获取rootPath目录下的文件.  
    }  
  
    public void getFileDir(String filePath,String ext) {  
        try{  
            this.tv.setText("当前路径:"+filePath);// 设置当前所在路径  
            items = new ArrayList<String>();  
            paths = new ArrayList<String>();  
            File f = new File(filePath);  
            File[] files = f.listFiles();// 列出所有文件  
            
            // 将所有文件存入list中  
            if(files != null){  
            	List<File> directoryListing = new ArrayList<File>();
            	directoryListing.addAll(Arrays.asList(files));
            	//限制类型
            	for(int i=0;i<files.length;i++){
            		if (!directoryListing.get(i).getName().contains(ext)){
            			directoryListing.remove(i);
            		}
            	}
            	Collections.sort(directoryListing, new SortFileName());
            	Collections.reverse(directoryListing);
                int count = files.length;// 文件个数  
                for (int i = 0; i < count; i++) {  
                    File file = directoryListing.get(i);  
                    items.add(file.getName());  
                    paths.add(file.getPath());  
                }  
            }  
  
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  
                    android.R.layout.simple_list_item_1, items);  
            this.setListAdapter(adapter);  
        }catch(Exception ex){  
            ex.printStackTrace();  
        }  
  
    }  
    public class SortFileName implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
              return f1.getName().compareTo(f2.getName());
        }
    }
    @Override  
    protected void onListItemClick(ListView l, View v, int position, long id) {  
        super.onListItemClick(l, v, position, id);  
        String path = paths.get(position);  
        final File file = new File(path);  
        //如果是文件夹就继续分解  
        
            new AlertDialog.Builder(this).setTitle(context.getString(R.string.title)).setMessage(context.getString(R.string.open)+file.getName()+"？").setPositiveButton("OK", new DialogInterface.OnClickListener(){  
  
                public void onClick(DialogInterface dialog, int which) {  
                	Intent intent2=new Intent();
        	        
        	        intent2.putExtra(UnvarnishedTransmissionActivity.FILE_PATH, file.getAbsolutePath());
                   setResult(RESULT_OK,intent2);
                   finish();
                }  
                  
            }).show();  
          
    }  
      
}  