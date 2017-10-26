package com.bluetooth.unvarnishedtransmission;


import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

public class UnvarnishedTransmissionActivity extends Activity {
	// Debug 
	private static final String TAG = "UnvarnishedTransmissionActivity";
	private boolean D = true;
	
	// Intent request codes
	private static final int REQUEST_SEARCH_BLE_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_FILE = 3;
	
	// msg for ui update
	private static final int MSG_EDIT_RX_STRING_UPDATE = 1;
	private static final int MSG_EDIT_TX_STRING_UPDATE = 2;
	private static final int MEASURE_FAIL = 3;
	private static final int MSG_CONNECTION_STATE_UPDATE = 4;
	private static final int MSG_SEND_MESSAGE_ERROR_UI_UPDATE = 5;
	private static final int MEASURE_FINISHED=6;
	
	
	
	// MTU size 
	private static int MTU_SIZE_EXPECT = 300;
	private static int MTU_PAYLOAD_SIZE_LIMIT = 30;
	
	// Return flag
	public static final String EXTRAS_DEVICE = "DEVICE";
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    //public static final String EXTRAS_DEVICE_RSSI = "DEVICE_RSSI";
    public static final String FILE_PATH="FILE";
    
    // flag for rx on/off
    private boolean isRxOn = false;
    
    //shake hand
    public boolean shake=false;
    
    // flag for auto tx 
    private boolean isAutoTx = false;
    
    // flag for connect state
    private boolean isConnectDevice = false;
    
    // RX counter and TX counter
    private volatile long mRxCounter;
    private volatile long mTxCounter;
    
    
    // 同步信号量，用于异步程序处理
    // should add volatile
 	private volatile boolean isWriteCharacteristicOk = true;
 	private volatile boolean writeCharacteristicError = false;
    
 	// Thread
    private ThreadRx mThreadRx;
    private ThreadUnpackSend mUnpackThread;
    
    
    // RX string builder, store the rx data
    // max rx speed is MAX_RX_BUFFER/RX_STRING_UPDATE_TIME = 40KByte/s
    private float a0,a1,a2,a3,a4;
    private byte[] d1=new byte[4];
    private byte[] d2=new byte[4];
    private byte[] d3=new byte[4];
    private byte[] d4=new byte[4];
    private int MAX_RX_BUFFER = 2000; 
    private int RX_STRING_UPDATE_TIME = 50;
    
    // Unpack sending flag
    private boolean isUnpackSending = false;
    
    // Selected Device information
    private BluetoothDevice mDevice;
    
    
    // bluetooth control
    private BluetoothAdapter mBluetoothAdapter;
    
    //Spinner
    private Spinner devType;
    private Spinner beaType;
    private Spinner dataType;
    
    // Bluetooth GATT
    private BluetoothGatt mBluetoothGatt;
    
    // Bluetooth GATT Service
    
    // Test Characteristic
    private BluetoothGattCharacteristic mTestCharacter;
    
    // bluetooth Manager
    private BluetoothManager mBluetoothManager;
    // Button
    private Button mbtnTx;
    private Button disMeas;
    private Button envMeas;
    private Button accMeas;
    private Button velMeas;
    private Button readD;
    
    //轴承图标
    private ImageView icon;
    private Bitmap red;
    private Bitmap green;
    private Bitmap yellow;
    
    
    //设备和轴承类型判断
    private int dev;
    private int bea;
    private int accColor;
    private int envColor;
    
    //最大值最小值平均值判断
    private int data=1;
    private StringBuilder mRxStringBuildler;
    //字号,分别对应下拉菜单，数字和其他
    private float smallSize;
    private float mediumSize;
    private float largeSize;
    //系数
    private float factorD;
    private float factorA;
    private float factorV;
    private float factorE;
        
    // EditText
    private EditText mDis;
    private EditText mAcc;
    private EditText mVel;
    private EditText mEnv;
    
    private EditText max;
    private EditText min;
    private EditText avg;
    
    // Chart
    private String title;
    private XYSeries seriesA;
    private XYSeries seriesD;
    private XYSeries seriesE;
    private XYSeries seriesV;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer renderer;
    private Context context;
    private double addY;
    
    public double yRange=100;
    public int xRange=10;
    public int xA=0;
    public int xD=0;
    public int xV=0;
    public int xE=0;
    //读取文件
    public String FilePath="";

    // TextView 
    private TextView mdisStatus;
    private TextView maccStatus;
    private TextView mvelStatus;
    private TextView menvStatus;
    private TextView mtvGattStatus;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private TextView text5;
    private TextView text6;
    private TextView text7;
    private TextView text8;
    private TextView text9;
    private TextView text10;
    private TextView text11;
    private TextView text12;
    private TextView text13;
    private TextView text14;
    
    // measure state
    private int measState=4;
    // State control
    private GattConnectState mConnectionState;
    private enum GattConnectState {
    	STATE_INITIAL,
    	STATE_DISCONNECTED,
    	STATE_CONNECTING,
    	STATE_CONNECTED,
    	STATE_CHARACTERISTIC_CONNECTED,
    	STATE_CHARACTERISTIC_NOT_FOUND;
    }
    
    // UUID, modify for new spec
    //private final static UUID TEST_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");      //ffe0
    private final static UUID TEST_SERVICE_UUID = UUID.fromString("0000e0ff-3c17-d293-8e48-14fe2e4da212");      //123bit
    private final static UUID TEST_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    
    /** Client configuration descriptor that will allow us to enable notifications and indications */
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");   //geyun:just need support ccc
    
    // InputMethodManager
    InputMethodManager mInputMethodManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unvarnished_transmission);
        if(D) Log.d(TAG, "-------onCreate-------");
        // Set Title
        context = getApplicationContext();
        getActionBar().setTitle("S908i");
        icon = (ImageView) findViewById(R.id.icon);
        green=BitmapFactory.decodeResource(getResources(), R.drawable.green);
        red=BitmapFactory.decodeResource(getResources(), R.drawable.red);
        yellow=BitmapFactory.decodeResource(getResources(), R.drawable.yellow);
        icon.setImageBitmap(green);
        // get the bluetooth adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        
        // judge whether android have bluetooth
        if(null == mBluetoothAdapter) {
        	if(D) Log.e(TAG, "This device do not support Bluetooth");
            Dialog alertDialog = new AlertDialog.Builder(this).
                    setMessage("This device do not support Bluetooth").
                    create();
            alertDialog.show();
        }
        
        // ensure that Bluetooth exists
        if (!EnsureBleExist()) {
        	if(D) Log.e(TAG, "This device do not support BLE");
        	finish();
        }
        
        //字号
        smallSize=StringByteTrans.getTextSize("S");
        mediumSize=StringByteTrans.getTextSize("M");
        largeSize=StringByteTrans.getTextSize("L");
        
        // Button
        mbtnTx = (Button) findViewById(R.id.btnTx);
        mbtnTx.setText(R.string.measure_all);
        mbtnTx.setTextSize(largeSize);
        envMeas = (Button) findViewById(R.id.envMeas);
        envMeas.setText(R.string.measure);
        envMeas.setTextSize(largeSize);
        accMeas = (Button) findViewById(R.id.accMeas);
        accMeas.setText(R.string.measure);
        accMeas.setTextSize(largeSize);
        velMeas = (Button) findViewById(R.id.velMeas);
        velMeas.setText(R.string.measure);
        velMeas.setTextSize(largeSize);
        disMeas = (Button) findViewById(R.id.disMeas);
        disMeas.setText(R.string.measure);
        disMeas.setTextSize(largeSize);
        readD = (Button) findViewById(R.id.readData);
        readD.setTextSize(largeSize);
     
        // Button listener 
        mbtnTx.setOnClickListener(new ButtonClick());
        envMeas.setOnClickListener(new ButtonClick());
        accMeas.setOnClickListener(new ButtonClick());
        velMeas.setOnClickListener(new ButtonClick());
        disMeas.setOnClickListener(new ButtonClick());
        readD.setOnClickListener(new ButtonClick());
        
        // EditText initial
        mdisStatus = (TextView) findViewById(R.id.disStatus);
        maccStatus = (TextView) findViewById(R.id.accStatus);
        mvelStatus = (TextView) findViewById(R.id.velStatus);
        menvStatus = (TextView) findViewById(R.id.envStatus);
        text1=(TextView) findViewById(R.id.t1);
        text2=(TextView) findViewById(R.id.t2);
        text3=(TextView) findViewById(R.id.t3);
        text4=(TextView) findViewById(R.id.t4);
        text5=(TextView) findViewById(R.id.t5);
        text6=(TextView) findViewById(R.id.t6);
        text7=(TextView) findViewById(R.id.t7);
        text8=(TextView) findViewById(R.id.t8);
        text9=(TextView) findViewById(R.id.t9);
        text10=(TextView) findViewById(R.id.t10);
        text11=(TextView) findViewById(R.id.t11);
        text12=(TextView) findViewById(R.id.t12);
        text13=(TextView) findViewById(R.id.t13);
        text14=(TextView) findViewById(R.id.t14);
        text1.setTextSize(largeSize);
        text2.setTextSize(largeSize);
        text3.setTextSize(largeSize);
        text4.setTextSize(largeSize);
        text5.setTextSize(largeSize);
        text6.setTextSize(largeSize);
        text7.setTextSize(largeSize);
        text8.setTextSize(largeSize);
        text9.setTextSize(largeSize);
        text10.setTextSize(largeSize);
        text11.setTextSize(largeSize);
        text12.setTextSize(largeSize);
        text13.setTextSize(largeSize);
        text14.setTextSize(largeSize);
        
        mDis = (EditText) findViewById(R.id.displacement);
        mDis.setTextSize(mediumSize);
        mAcc = (EditText) findViewById(R.id.acc);
        mAcc.setTextSize(mediumSize);
        mVel = (EditText) findViewById(R.id.velocity);
        mVel.setTextSize(mediumSize);
        mEnv = (EditText) findViewById(R.id.envelope);
        mEnv.setTextSize(mediumSize);
        max = (EditText) findViewById(R.id.max);
        max.setTextSize(mediumSize);
        min = (EditText) findViewById(R.id.min);
        min.setTextSize(mediumSize);
        avg = (EditText) findViewById(R.id.avg);
        avg.setTextSize(mediumSize);
                
        // chart
        LinearLayout layout = (LinearLayout)findViewById(R.id.chart);
        //这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
        seriesA = new XYSeries(context.getString(R.string.acc));
        seriesD = new XYSeries(context.getString(R.string.dis));
        seriesV = new XYSeries(context.getString(R.string.vel));
        seriesE = new XYSeries(context.getString(R.string.env));
        
        //创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();
        
        //将点集添加到这个数据集中，顺序很重要
        mDataset.addSeries(seriesD);
        mDataset.addSeries(seriesA);
        mDataset.addSeries(seriesV);
        mDataset.addSeries(seriesE);
        
        //以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        PointStyle style = PointStyle.CIRCLE;
        XYSeriesRenderer rA = buildRenderer(Color.GREEN, style, true);        
        XYSeriesRenderer rV = buildRenderer(Color.YELLOW, style, true);
        XYSeriesRenderer rD = buildRenderer(Color.BLUE, style, true);
        XYSeriesRenderer rE = buildRenderer(Color.RED, style, true);
        renderer = new XYMultipleSeriesRenderer(); 
        renderer.addSeriesRenderer(rA);
        renderer.addSeriesRenderer(rE);
        renderer.addSeriesRenderer(rV);
        renderer.addSeriesRenderer(rD);
        
        
        //设置好图表的样式
        setChartSettings(renderer, "", "", 0, xRange, 0, yRange, Color.WHITE, Color.WHITE);
        
        //生成图表
        chart = ChartFactory.getLineChartView(context, mDataset, renderer);
        
        //将图表添加到布局中去
        layout.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        //系数
        factorA=StringByteTrans.getFactor("A");
        factorD=StringByteTrans.getFactor("D");
        factorV=StringByteTrans.getFactor("V");
        factorE=StringByteTrans.getFactor("E");
        
        
        
        // Spinner initial
        devType=(Spinner)findViewById(R.id.DeviceType);
        beaType=(Spinner)findViewById(R.id.BearingType);
        dataType=(Spinner)findViewById(R.id.dataType);

        ArrayAdapter<CharSequence> adapter_device = ArrayAdapter.createFromResource(this,R.array.device,android.R.layout.simple_spinner_item);
        adapter_device.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        devType.setAdapter(adapter_device);
        devType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
        	
        	@Override
        	public void onItemSelected(AdapterView parent,View v,int position,long id){
        		dev=parent.getSelectedItemPosition();
        		if (dev==4){beaType.setEnabled(false);}
        		else{beaType.setEnabled(true);}
        	TextView tv = (TextView)v;  
            tv.setTextColor(getResources().getColor(R.color.black));    //设置颜色  

             tv.setTextSize(smallSize); }   //设置大小 
        	public void onNothingSelected(AdapterView parent){}
			});
       
        ArrayAdapter<CharSequence> adapter_bearing = ArrayAdapter.createFromResource(this,R.array.bearing,android.R.layout.simple_spinner_item);
        adapter_bearing.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beaType.setAdapter(adapter_bearing);
        beaType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
        	
        	@Override
        	public void onItemSelected(AdapterView parent,View v,int position,long id){
        		bea=parent.getSelectedItemPosition();
        		
        	TextView tv = (TextView)v;  
            tv.setTextColor(getResources().getColor(R.color.black));    //设置颜色  

             tv.setTextSize(smallSize); }   //设置大小 
        	public void onNothingSelected(AdapterView parent){}
			});
        
        ArrayAdapter<CharSequence> adapter_data = ArrayAdapter.createFromResource(this,R.array.data,android.R.layout.simple_spinner_item);
        adapter_data.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataType.setAdapter(adapter_data);
        dataType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
        	
        	@Override
        	public void onItemSelected(AdapterView parent,View v,int position,long id){
        		data=parent.getSelectedItemPosition()+1;
        		getStatistics();TextView tv = (TextView)v;  
                tv.setTextColor(getResources().getColor(R.color.black));    //设置颜色  
                
                tv.setTextSize(smallSize);    //设置大小 
        	}
        	
        	public void onNothingSelected(AdapterView parent){}
			});
        
        
        
        // TextView initial 

        mtvGattStatus = (TextView)findViewById(R.id.tvGattStatus);
        
        // RX string builder initial capacity with MAX_RX_BUFFER
            	
    	// Get the bluetooth manager
    	mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    	mRxStringBuildler = new StringBuilder(MAX_RX_BUFFER);
    	// Update the connection state
    	SetConnectionState(GattConnectState.STATE_INITIAL);
    	
    	//InputMethodManager
    	mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    
    @Override
    protected void onResume() {
    	if(D) Log.d(TAG, "-------onResume-------");
        super.onResume();
        /*chart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              // handle the click event on the chart
              SeriesSelection seriesSelection = chart.getCurrentSeriesAndPoint();
              try {
                // display information of the clicked point
            	  String x="";
            	  double s=0.0;
            	  if (seriesSelection.getSeriesIndex()==0){
            		  x=context.getString(R.string.displacement);
            		  s=seriesSelection.getValue();}
            	  if (seriesSelection.getSeriesIndex()==1){
            		  x=context.getString(R.string.acc);
            		  s= seriesSelection.getValue()/10;}
            	  if (seriesSelection.getSeriesIndex()==2){
            		  x=context.getString(R.string.vel);
            		  s= seriesSelection.getValue()/10;}
            	  if (seriesSelection.getSeriesIndex()==3){
            		  x=context.getString(R.string.env);
            		  s= seriesSelection.getValue()/50;}
                
				Toast.makeText(
                		UnvarnishedTransmissionActivity.this,x + (int)seriesSelection.getXValue()+", "
                    + (s-s%0.01), Toast.LENGTH_SHORT).show();
              }catch (Exception e) {
                  Toast.makeText(UnvarnishedTransmissionActivity.this, "No chart element", Toast.LENGTH_SHORT).show();
                } 
            }
          });*/
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
            	if(D) Log.d(TAG, "start bluetooth");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        
    }
    //设置渲染器
    protected XYSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
        
        
        //设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(color);
        r.setPointStyle(style);
        r.setFillPoints(fill);
        r.setLineWidth(3);
        
        return r;
        }
    
    //更新图表
    private void updateChart(int x, double s,XYSeries series) {
        
        //设置好下一个需要增加的节点
        addY=s;
        series.add(x, addY);
        
        //调整图像界限
        if (series.getItemCount()==xRange*0.9){
        	xRange=xRange*2;}
        if (series.getMaxY()>yRange){
        	yRange=series.getMaxY()*1.1;}
        setChartSettings(renderer, "", "", 0, xRange, 0, yRange, Color.WHITE, Color.WHITE);
        
        //视图更新，没有这一步，曲线不会呈现动态
        chart.invalidate();
        }
    
    //设置图表属性
     protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
       double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        //有关对图表的渲染可参看api文档
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GREEN);
        renderer.setXLabels(20);
        renderer.setYLabels(10);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setPointSize((float) 3);
        renderer.setShowLegend(true);
        renderer.setClickEnabled(false);
        renderer.setLegendTextSize(20);
        renderer.setMargins(new int[] {20,20,0,20});
        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.parseColor("#000000"));
        renderer.setGridColor(Color.parseColor("#919191"));
        renderer.setPanEnabled(true, true);
        renderer.setPanLimits(new double[]{0.0,xRange*1.0,0.0,yRange});
       }
     
     
        @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	menu.clear();
    	// if not find the special characteristic
        if(GattConnectState.STATE_CHARACTERISTIC_CONNECTED != mConnectionState) {
        	menu.add(context.getString(R.string.action_connect)).setIcon(R.drawable.select).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
        	menu.add(context.getString(R.string.action_disconnect)).setIcon(R.drawable.disconnect).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
        if(D) Log.d(TAG, "-------onOptionsItemSelected-------");
        switch(item.getItemId()) {//得到被点击的item的itemId
	        case 0: //对应的ID就是在add方法中所设定的Id
	        	// if have a gatt connection, disconnect and unregister it
	            if(GattConnectState.STATE_CHARACTERISTIC_CONNECTED == mConnectionState 
	            	|| GattConnectState.STATE_CONNECTED == mConnectionState 
	            	|| GattConnectState.STATE_CHARACTERISTIC_NOT_FOUND == mConnectionState ) {
	            	
	            	// only find the characteristic, then return, disconnect will very soon change the state, so do like this
	            	if(GattConnectState.STATE_CHARACTERISTIC_CONNECTED == mConnectionState) {
	            		// Try to disconnect the gatt server, ensure unregister the callback (in the callback register it)
	            		if(BluetoothProfile.STATE_CONNECTED == mBluetoothManager.getConnectionState(mDevice, BluetoothProfile.GATT)) {
	            			mBluetoothGatt.disconnect();
	            			shake=false;
		            	}
	            		return true;
	            	}
	            	
	            	// Try to disconnect the gatt server, ensure unregister the callback (in the callback register it)
	            	// here din't return
	            	if(BluetoothProfile.STATE_CONNECTED == mBluetoothManager.getConnectionState(mDevice, BluetoothProfile.GATT)) {
            			mBluetoothGatt.disconnect();
            			shake=false;
	            	}
	            }
	        	
	            // if didn't hava a gatt connect, find a device and create it
	        	if(D) Log.i(TAG, "start select ble device");
	        		        	
	        	// start the device search activity for select device
	        	Intent intent = new Intent(this, DeviceSearchActivity.class);
	            startActivityForResult(intent, REQUEST_SEARCH_BLE_DEVICE);
	            break;
	            
	        default:
	        	if(D) Log.e(TAG, "something may error");
	            break;
	    }
        return true;
    }
    
    @Override
    public void onStop( ) {
    	if(D) Log.d(TAG, "-------onStop-------");
    	// Do something when activity on stop
    	// kill the auto unpack thread
    	if(null != mUnpackThread) {
    		mUnpackThread.interrupt();
    	}
    
    	// kill the rx thread
    	if(null != mThreadRx) {
    		mThreadRx.interrupt();
    	}
    	
    	super.onStop();
    }
    
    @Override
    public void onDestroy( ) {
    	if(D) Log.d(TAG, "-------onDestroy-------");
    	
    	// Try to disconnect the gatt server
    	if(BluetoothProfile.STATE_CONNECTED == mBluetoothManager.getConnectionState(mDevice, BluetoothProfile.GATT)) {
			mBluetoothGatt.disconnect();
			shake=false;
    	}
    	
    	mTestCharacter = null;
    	
    	super.onDestroy();
    }
    //根据返回指令做出反应
    public void onActivityResult(int requestCode, int resultCode, Intent intent)  { 
    	if(D) Log.d(TAG, "-------onActivityResult-------");
    	if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_SEARCH_BLE_DEVICE:
	    	// When the request to enable Bluetooth returns 
	    	if (resultCode == Activity.RESULT_OK) {
	    		//Stroe the information
	    		//mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
	            //mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
	    		//mRssi=intent.getStringExtra(EXTRAS_DEVICE_RSSI);
	    		mDevice = intent.getParcelableExtra(EXTRAS_DEVICE);
	    		if(D) Log.e(TAG, "select a device, the name is " + mDevice.getName() + ", addr is " + mDevice.getAddress());
	    		//rssi=intent.getParcelableExtra(EXTRAS_DEVICE_RSSI);
	        	// Update the connection state
	        	SetConnectionState(GattConnectState.STATE_CONNECTING);
	        	//Rssi.setText(mRssi);
	    		ConnectDevice(mDevice);
	        } else {
	        	// do nothing
	        	if(D) Log.e(TAG, "the result code is error!");
	        }
	    	break;
        
        case REQUEST_ENABLE_BT:
	    	// When the request to enable Bluetooth returns 
	    	if (resultCode == Activity.RESULT_OK) {
	    		//do nothing
	            Toast.makeText(this, "Bt is enabled!", Toast.LENGTH_SHORT).show();            
	        } else {
	        	// User did not enable Bluetooth or an error occured
	        	Toast.makeText(this, "Bt is not enabled!", Toast.LENGTH_SHORT).show();
	        	finish();
	        }
	    	break;
	    case REQUEST_FILE:
        	if (resultCode == Activity.RESULT_OK) {
        		FilePath = intent.getStringExtra(FILE_PATH);
        		
        		try {
					read();
				} catch (Exception e) {
					e.printStackTrace();
				}
        	}
        default:
        	break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }  
    
    // judge the support of ble in android  
    private boolean EnsureBleExist() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This device does not support BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    
    //发送握手指令
    private void Shake(){
    			SendMessageToRemote("AA");
    			try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		};
    		
    //滚动轴承状态判断
    private void Check(){
    	if (accColor==Color.parseColor("#FF0000")&&envColor==Color.parseColor("#FF0000")){
    		icon.setImageBitmap(red);
    	}
    	else if (
    			(accColor==Color.parseColor("#FFFF00")&&envColor==Color.parseColor("#FFFF00"))||
    			((accColor==Color.parseColor("#FF0000")^envColor==Color.parseColor("#FF0000"))&&(accColor==Color.parseColor("#FFFF00")^envColor==Color.parseColor("#FFFF00")))){
    		icon.setImageBitmap(yellow);
    	}
    	else{
    		icon.setImageBitmap(green);
    	}
    	//icon.setImageBitmap(red);
    }
    //更新最大值最小值平均值
    private void getStatistics(){
    	if (data==1){
			max.setText(Float.toString((float) (seriesD.getMaxY()-seriesD.getMaxY()%0.1)));
			min.setText(Float.toString((float) (seriesD.getMinY()-seriesD.getMinY()%0.1)));
			avg.setText(Float.toString((float) (getAverage(seriesD)-getAverage(seriesD)%0.1)));
		}
		else if (data==2){
			max.setText(Float.toString((float) (seriesA.getMaxY()/10-seriesA.getMaxY()/10%0.01)));
			min.setText(Float.toString((float) (seriesA.getMinY()/10-seriesA.getMinY()/10%0.01)));
			avg.setText(Float.toString((float) (getAverage(seriesA)/10-getAverage(seriesA)/10%0.01)));
		}
		else if (data==3){
			max.setText(Float.toString((float) (seriesV.getMaxY()/10-seriesV.getMaxY()/10%0.01)));
			min.setText(Float.toString((float) (seriesV.getMinY()/10-seriesV.getMinY()/10%0.01)));
			avg.setText(Float.toString((float) (getAverage(seriesV)/10-getAverage(seriesV)/10%0.01)));
		}
		else if (data==4){
			max.setText(Float.toString((float) (seriesE.getMaxY()/50-seriesE.getMaxY()/50%0.01)));
			min.setText(Float.toString((float) (seriesE.getMinY()/50-seriesE.getMinY()/50%0.01)));
			avg.setText(Float.toString((float) (getAverage(seriesE)/50-getAverage(seriesE)/50%0.01)));
		}
    }
    
    //计算平均值
    private double getAverage(XYSeries series){
    	int length = series.getItemCount();
    	double accum = 0;
    	for (int i=0;i<length;i++){
    		accum+=series.getY(i);
    	}
    	return accum/length;
    }
    //自动通讯时diasable其他按钮
    private void AutoTx(){
    	if (isAutoTx==true){
    		if (measState == 0){
    			accMeas.setText(R.string.stop);
    			mbtnTx.setEnabled(false);
        		envMeas.setEnabled(false);
        		disMeas.setEnabled(false);
        		velMeas.setEnabled(false);
    			}
    		else if (measState == 1){
    			velMeas.setText(R.string.stop);
    			mbtnTx.setEnabled(false);
        		envMeas.setEnabled(false);
        		disMeas.setEnabled(false);
        		accMeas.setEnabled(false);
        		}
    		else if (measState==2){
    			disMeas.setText(R.string.stop);
    			mbtnTx.setEnabled(false);
        		envMeas.setEnabled(false);
        		velMeas.setEnabled(false);
        		accMeas.setEnabled(false);
        		}
    		else if (measState==3){
    			envMeas.setText(R.string.stop);
    			mbtnTx.setEnabled(false);
        		disMeas.setEnabled(false);
        		velMeas.setEnabled(false);
        		accMeas.setEnabled(false);
    			}
    		else if (measState==4){
    			mbtnTx.setText(R.string.stop_all);
        		envMeas.setEnabled(false);
        		disMeas.setEnabled(false);
        		velMeas.setEnabled(false);
        		accMeas.setEnabled(false);
        		}
    	}
    	else{
    		if (measState == 0){
    			accMeas.setText(R.string.measure);
    			mbtnTx.setEnabled(true);
        		envMeas.setEnabled(true);
        		disMeas.setEnabled(true);
        		velMeas.setEnabled(true);
        		Shake();
    			}
    		else if (measState == 1){
    			velMeas.setText(R.string.measure);
    			mbtnTx.setEnabled(true);
        		envMeas.setEnabled(true);
        		disMeas.setEnabled(true);
        		accMeas.setEnabled(true);
        		Shake();
        		}
    		else if (measState==2){
    			disMeas.setText(R.string.measure);
    			mbtnTx.setEnabled(true);
        		envMeas.setEnabled(true);
        		velMeas.setEnabled(true);
        		accMeas.setEnabled(true);
        		Shake();
        		}
    		else if (measState==3){
    			envMeas.setText(R.string.measure);
    			mbtnTx.setEnabled(true);
        		disMeas.setEnabled(true);
        		velMeas.setEnabled(true);
        		accMeas.setEnabled(true);
        		Shake();
    			}
    		else if (measState==4){
    			mbtnTx.setText(R.string.measure_all);
        		envMeas.setEnabled(true);
        		disMeas.setEnabled(true);
        		velMeas.setEnabled(true);
        		accMeas.setEnabled(true);
        		Shake();
        		}
    	}
    }
    //按钮事件
    class ButtonClick implements OnClickListener
	{
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			//If the Tx button click
			case R.id.btnTx:
				// send message to remote
				measState =4;
				
				isAutoTx=!isAutoTx;
				AutoTx();
				if (isAutoTx){
					clearFile();
					SendMessageToRemote("5340010100");}
				else{
					try {
						save();
						} 
					catch (Exception e) {
						e.printStackTrace();
			}}
				break;
				
			case R.id.envMeas:
				measState = 3;
				isAutoTx=!isAutoTx;
				AutoTx();
				dataType.setSelection(3);
				if (isAutoTx){
					clearFile();
					SendMessageToRemote("5343010000");}
				else{
					try {
						save();
						} 
					catch (Exception e) {
						e.printStackTrace();
			}}
				break;
				
			case R.id.disMeas:
				measState = 2;
				isAutoTx=!isAutoTx;
				AutoTx();
				dataType.setSelection(0);
				if (isAutoTx){
					clearFile();
					SendMessageToRemote("5342010000");}
				else{
					try {
					save();
					} 
				 catch (Exception e) {
					e.printStackTrace();
				}}
				break;
				
			case R.id.accMeas:
				measState = 0;
				
				isAutoTx=!isAutoTx;
				AutoTx();
				dataType.setSelection(1);
				if (isAutoTx){
					clearFile();
				SendMessageToRemote("5340010000");}
				else{
					try {
						save();
						} 
					catch (Exception e) {
						e.printStackTrace();
			}}
				break;
				
			case R.id.velMeas:
				measState = 1;
				
				isAutoTx=!isAutoTx;
				AutoTx();
				dataType.setSelection(2);
				if (isAutoTx){clearFile();
				SendMessageToRemote("5341010000");}
				
				else{
					try {
						save();
						} 
					catch (Exception e) {
						e.printStackTrace();
			}}
				break;
			
			case R.id.readData:
					Intent intent2 = new Intent(UnvarnishedTransmissionActivity.this, readList.class);
			        startActivityForResult(intent2, REQUEST_FILE);
				
			}
		}
	}
    
    //保存文件，如未测量则存null
    private void save()throws Exception{
    	//定义writer
    	CSVWriter writer = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Sendig/"+getTime()+".csv"));
        
    	// 写入数据
        for(int i=0;i<maximum(seriesA.getItemCount(),seriesV.getItemCount(),seriesD.getItemCount(),seriesE.getItemCount());i++){
        	String valueD;String valueA;String valueV;String valueE;
        	try{valueD=Float.toString((float)seriesD.getY(i));}catch(IndexOutOfBoundsException e){valueD="null";}
        	try{valueA=Float.toString((float)seriesA.getY(i)/10);}catch(IndexOutOfBoundsException e){valueA="null";}
        	try{valueV=Float.toString((float)seriesV.getY(i)/10);}catch(IndexOutOfBoundsException e){valueV="null";}
        	try{valueE=Float.toString((float)seriesE.getY(i)/50);}catch(IndexOutOfBoundsException e){valueE="null";}
        
        	String[] entries = {String.valueOf(i+1),valueD,valueA,valueV,valueE};
        	writer.writeNext(entries);}
        //关闭writer
   	 	writer.close();
    }
    //开始测量后清除现有文件
    private void clearFile(){
    	seriesD.clear();
    	seriesA.clear();
    	seriesV.clear();
    	seriesE.clear();
    	
    	mVel.setText("");
    	mDis.setText("");
    	mEnv.setText("");
    	mAcc.setText("");
    	
    	xA=0;
        xD=0;
        xV=0;
        xE=0;
    	chart.invalidate();
    	
    	xRange=10;
    	yRange=100;
    	setChartSettings(renderer, "", "", 0, xRange, 0, yRange, Color.WHITE, Color.WHITE);
    }
    //读取文件并更新图表
    public void read() throws Exception{
    	clearFile();
        Log.i(TAG, FilePath);
        CSVReader history = new CSVReader(new FileReader(FilePath));
        
        String[] row= history.readNext();
        
        while(row!=null){
        	try{
        		updateChart(Integer.valueOf(row[0]),Float.parseFloat(row[1]),seriesD);
        		dataType.setSelection(0);}catch(NumberFormatException e){}
        	try{
            	updateChart(Integer.valueOf(row[0]),Float.parseFloat(row[2])*10,seriesA);
            	dataType.setSelection(1);}catch(NumberFormatException e){}
        	try{
            	updateChart(Integer.valueOf(row[0]),Float.parseFloat(row[3])*10,seriesV);
            	dataType.setSelection(2);}catch(NumberFormatException e){}
        	try{
            	updateChart(Integer.valueOf(row[0]),Float.parseFloat(row[4])*50,seriesE);
            	dataType.setSelection(3);}catch(NumberFormatException e){}
        	row= history.readNext();
        }
        history.close();
        
        getStatistics();
    }
    //求最大值
    private float maximum(float i1,float i2,float i3,float i4){
    	float[] length=new float[4];
    	length[0]=i1;
    	length[1]=i2;
    	length[2]=i3;
    	length[3]=i4;
    	float m=0;
    	for (int i=0;i<4;i++){
    		if (length[i]>m){m=length[i];}
    	}
    	return m;
    }
    //求现在时间
    private String getTime(){
    	long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();  
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        Date d1=new Date(time);  
        String t1=format.format(d1); 
        return t1;
    }
    // send data to the remote device and update ui
    public void SendMessageToRemote(String msg) {
    	byte[] sendData;
		if(0 == msg.length()) {
			if(D) Log.w(TAG, "the tx string is empty!");
			// send the msg to update the ui
	    	Message message = new Message();
	        message.what = MSG_SEND_MESSAGE_ERROR_UI_UPDATE;
	        message.arg1 = 0;
	        handler.sendMessage(message);
			return;
		}
    	
		// judge the type of edit Tx String buffer, and change to byte[]
		//hex
			// remove the space and the "0x", and change to byte[]
			sendData = StringByteTrans.hexStringToBytes(msg.toString().replace(" ", "").replace("0x", ""));
		
		if(sendData == null) {
			if(D) Log.e(TAG, "the tx string have some error!");
			
			// stop the auto tx
			isAutoTx = false;
			
			// send the msg to update the ui
	    	Message message = new Message();
	        message.what = MSG_SEND_MESSAGE_ERROR_UI_UPDATE;
	        message.arg1 = 1;
	        handler.sendMessage(message);
			return;
		}
		
		
		// ensure last unpack tx is OK
		if(true == isUnpackSending) {
			if(D) Log.e(TAG, "the last tx string didn't all send!");
			
			// stop the auto tx
			isAutoTx = false;
			
			// send the msg to update the ui
	    	Message message = new Message();
	        message.what = MSG_SEND_MESSAGE_ERROR_UI_UPDATE;
	        message.arg1 = 2;
	        handler.sendMessage(message);
			return;
		}
		
		// send data to the remote device
		mUnpackThread = new ThreadUnpackSend(sendData);
		mUnpackThread.start();
		
		if(D) Log.d(TAG, "send data is: " + Arrays.toString(sendData));
		
    	
    }
    
    
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param The destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    //连接蓝牙
    public boolean ConnectDevice(BluetoothDevice device) {
    	if (device == null) {
    		if(D) Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
    	
    	// Try to connect the gatt server
    	mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    	if(D) Log.d(TAG, "Trying to create a new connection.");
		return D;
    	
    }
    
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
    	@Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if(D) Log.e(TAG, "onMtuChanged new mtu is " + mtu);
            if(D) Log.e(TAG, "onMtuChanged new status is " + String.valueOf(status));
            // change the mtu real payloaf size
            MTU_PAYLOAD_SIZE_LIMIT = mtu - 3;

            // Attempts to discover services after successful connection.
            if(D) Log.i(TAG, "Attempting to start service discovery:" +
                    mBluetoothGatt.discoverServices());
        }



        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
    		if(D) Log.d(TAG, "the new staus is " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
            	// Update the connection state
            	SetConnectionState(GattConnectState.STATE_CONNECTED);

                if(D) Log.i(TAG, "Connected to GATT server.");
                
                // only android 5.0 add the requestMTU feature
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
	                // Attempts to discover services after successful connection.
	                if(D) Log.i(TAG, "Attempting to start service discovery:" +
	                        mBluetoothGatt.discoverServices());
                } else {
                	if (D) Log.i(TAG, "Attempting to request mtu size, expect mtu size is: " + String.valueOf(MTU_SIZE_EXPECT));
                    mBluetoothGatt.requestMtu(MTU_SIZE_EXPECT);
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            	// Update the connection state
            	SetConnectionState(GattConnectState.STATE_DISCONNECTED);
            	shake=false;
            	
            	// Try to close the gatt server, ensure unregister the callback
            	if(null != mBluetoothGatt) {
            		mBluetoothGatt.close();
            	}
            	
                if(D) Log.i(TAG, "Disconnected from GATT server.");
            }
        }
    	
    	@Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	// get all service on the remote device
            	final List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                	if(D) Log.d(TAG, "the GATT server uuid is " + service.getUuid());
                	// get all Characteristics in the service, then we can use the UUID or the attribute handle to get the Characteristics's value
                	List<BluetoothGattCharacteristic> characters = service.getCharacteristics();
                	for(BluetoothGattCharacteristic character : characters) {
                		if(D) Log.d(TAG, "the GATT server include Characteristics uuid is " + character.getUuid());
                		if(D) Log.d(TAG, "the Characteristics's permissoion is " + character.getPermissions());
                		if(D) Log.d(TAG, "the Characteristics's properties is 0x" + Integer.toHexString(character.getProperties()));
                		// get all descriptors in the characteristic 
                		List<BluetoothGattDescriptor> descriptors = character.getDescriptors();
                		for(BluetoothGattDescriptor descriptor : descriptors) {
                    		if(D) Log.d(TAG, "the Characteristics include descriptor uuid is " + descriptor.getUuid());
                    	}
                		
                		if(character.getUuid().equals(TEST_CHARACTERISTIC_UUID)) {
                			// find the test character, then we can use it to set value or get value.
                			mTestCharacter = character;
                			
                			// Update the connection state
                        	SetConnectionState(GattConnectState.STATE_CHARACTERISTIC_CONNECTED);
                		}
                	}
                }
                
                // if not find the special characteristic
                if(GattConnectState.STATE_CHARACTERISTIC_CONNECTED != mConnectionState) {
                	// Update the connection state
                	SetConnectionState(GattConnectState.STATE_CHARACTERISTIC_NOT_FOUND);
                }
            }
        }
    	
    	@Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
    		byte[] data;
    		if(TEST_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
    			data = characteristic.getValue();
    			// call function to deal the data
    			onDataReceive(data);
    		} else {
    			if(D) Log.w(TAG, "receive other notification");
    			return;
    		}
        }
    	
    	@Override
        public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID.equals(descriptor.getUuid())) {
                    if(D) Log.d(TAG, "CCC:  ok,try to write test----> ");
                }
            } else {
            	if(D) Log.e(TAG, "Descriptor write error: " + status);
            }
        };
        
    	@Override
    	public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    		// Here can do something to verify the write
    		
            if (TEST_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
            	if(D) Log.d(TAG, "onCharacteristicWrite UUID is: " + characteristic.getUuid());
            	if(D) Log.d(TAG, "onCharacteristicWrite data value:"+Arrays.toString(characteristic.getValue()));
            	
        		if (status == BluetoothGatt.GATT_SUCCESS) {
        			writeCharacteristicError = false;
        		} else {
        			writeCharacteristicError = true;
                	if(D) Log.e(TAG, "Characteristic write error: " + status + "try again.");
                }
        		
        		isWriteCharacteristicOk = true;
            }
	        
    	}
    };
    
    // data receive
    public void onDataReceive(byte[] data) {
    	if(true == isRxOn) {
	    	if(D) Log.d(TAG, "receive data is: " + Arrays.toString(data));
	    	mRxCounter = mRxCounter + data.length;
	    	mRxStringBuildler.append(data);
	    	// Store the receive data, store with ASCII, should store in a StringBuilder first, 
	    	// because the receive speed will be very fast, near 10ms a packet
	    	if(data.length>=4){
	    		
	    		byte[] b=new byte[4];
	    		byte[] c=new byte[1];
	    		//根据通讯协议读取相关数据，1A，2V，3D，4E
	    		for (int i = 0; i < 4; i++) {
	    		b[i]=data[i+5];
	    		d1[i]=data[i+9];
	    		d2[i]=data[i+13];
	    		d3[i]=data[i+17];
	    		d4[i]=data[i+21];
	    		}
	    		c[0]=data[25];
	    		int accum=0;
	    		for(int i=0;i<23;i++){
	    			
	    			accum+=data[i+2];
	    		}
	    		Log.w(TAG, "check value is"+(byte)accum);
	    		
	    		if (data[25]==(byte)accum){
	    		a0 = StringByteTrans.getFloat(b);
	    		a1=StringByteTrans.getFloat(d1)*factorA;
	    		a2=StringByteTrans.getFloat(d2)*factorV;
	    		a3=StringByteTrans.getFloat(d3)*factorD;
	    		a4=StringByteTrans.getFloat(d4)*factorE;
	    		Message message = new Message();
	    		message.what = MSG_EDIT_RX_STRING_UPDATE;
	    		handler.sendMessage(message);
	    	}
	    		else{
	    			Message message=new Message();
	    	    	message.what=MEASURE_FAIL;
	    	    	handler.sendMessage(message);
	    			}
	    		}
	    		
	    	else if (StringByteTrans.bytesToHexString(data).equals("55")){shake=true;}
	    	else{Message message=new Message();
	    	message.what=MEASURE_FINISHED;
	    	handler.sendMessage(message);}	    	//mRxStringBuildler.append(DigitalTrans.bytetoString(data));
	
	    	// send the msg, here may send less times MSG, so we use the StringBuildler
	    	}
    	}
    

    
    
    // for ui update
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == MSG_EDIT_RX_STRING_UPDATE){
            	if(0 == mRxStringBuildler.length()) {
            		//if(D) Log.w(TAG, "no rx data, don't update");
            		return;
            	}
            	
            	
            	mRxStringBuildler.delete(0, mRxStringBuildler.length());
            	// 录入数据
            	if(measState==4){
            		String s1 = Float.toString((float) (a1-a1%0.01));
                	String s2 = Float.toString((float) (a2-a2%0.01));
                	String s3 = Float.toString((float) (a3-a3%0.1));
                	String s4 = Float.toString((float) (a4-a4%0.01));
                	mDis.setText(s3);
                	mAcc.setText(s1);
                	mVel.setText(s2);
                	mEnv.setText(s4);
                	//根据10816标准判断状态
                	accColor=StringByteTrans.compareAcc(Float.parseFloat(s1));
                	envColor=StringByteTrans.compareEnv(Float.parseFloat(s4));
                	mdisStatus.setBackgroundColor(StringByteTrans.compareDis(dev,bea,Float.parseFloat(s3)));
                	maccStatus.setBackgroundColor(accColor);
                	mvelStatus.setBackgroundColor(StringByteTrans.compareVel(dev,bea,Float.parseFloat(s2)));
                	menvStatus.setBackgroundColor(envColor);
                	Check();
                	//更新图表
                	updateChart(xA,a1*10,seriesA);xA+=1;
                	updateChart(xV,a2*10,seriesV);xV+=1;
                	updateChart(xD,a3,seriesD);xD+=1;
                	updateChart(xE,a4*50,seriesE);xE+=1;
                	getStatistics();
                	}
            	else if(measState==2){
            		String s=Float.toString((float) (a0*factorD-a0*factorD%0.1));
            		mDis.setText(s);
            		mdisStatus.setBackgroundColor(StringByteTrans.compareDis(dev,bea,Float.parseFloat(s)));
            		updateChart(xD,a0*factorD-a0*factorD%0.1,seriesD);xD+=1;
            		getStatistics();
            		}
            	else if(measState==0){
            		String s=Float.toString((float) (a0*factorA-a0*factorA%0.01));
            		mAcc.setText(s);
            		accColor=StringByteTrans.compareAcc(a0*factorA);
            		maccStatus.setBackgroundColor(accColor);
            		Check();
            		updateChart(xA,(a0*factorA-a0*factorA%0.01)*10,seriesA);xA+=1;
            		getStatistics();}
            	else if(measState==1){
            		String s=Float.toString((float) (a0*factorV-a0*factorV%0.01));
            		mVel.setText(s);
            		mvelStatus.setBackgroundColor(StringByteTrans.compareVel(dev,bea,a0));
            		updateChart(xV,(a0*factorV-a0*factorV%0.01)*10,seriesV);xV+=1;
            		getStatistics();}
            	else if(measState==3){
            		String s=Float.toString((float) (a0*factorE-a0*factorE%0.01));
            		mEnv.setText(s);
            		envColor=StringByteTrans.compareEnv(a0*factorE);
            		menvStatus.setBackgroundColor(envColor);
            		Check();
            		getStatistics();
            		updateChart(xE,(a0*factorE-a0*factorE%0.01)*50,seriesE);xE+=1;}
            }
           else if(msg.what == MEASURE_FINISHED){
            	//发送52获取数据
            	SendMessageToRemote("52");
            }
           else if(msg.what == MEASURE_FAIL){
           	//发送52获取数据
        	   Dialog();
           }
             else if(msg.what == MSG_CONNECTION_STATE_UPDATE) {
            	if(GattConnectState.STATE_INITIAL == mConnectionState) {
            		
            		if(D) Log.i(TAG, "The connection state now is STATE_INITIAL");
            		// Initial state
            		
            		
            		
            		mbtnTx.setEnabled(false);
            		envMeas.setEnabled(false);
            		disMeas.setEnabled(false);
            		velMeas.setEnabled(false);
            		accMeas.setEnabled(false);
            		
            		// Change the connect state text
            		mtvGattStatus.setText("Please find a device to connect");
            		mtvGattStatus.setTextColor(android.graphics.Color.RED);
            	} else if(GattConnectState.STATE_CONNECTING == mConnectionState) {
            		if(D) Log.i(TAG, "The connection state now is STATE_CONNECTING");
            		
            		
            		mbtnTx.setEnabled(false);
            		envMeas.setEnabled(false);
            		disMeas.setEnabled(false);
            		velMeas.setEnabled(false);
            		accMeas.setEnabled(false);
            		// Change the connect state text
            		mtvGattStatus.setText("Waiting for connecting");
            		mtvGattStatus.setTextColor(android.graphics.Color.RED);
            	} else if(GattConnectState.STATE_DISCONNECTED == mConnectionState) {
            		if(D) Log.i(TAG, "The connection state now is STATE_DISCONNECTED");
            		// disable rx
            	
            		mbtnTx.setEnabled(false);
            		envMeas.setEnabled(false);
            		disMeas.setEnabled(false);
            		velMeas.setEnabled(false);
            		accMeas.setEnabled(false);
            		// change the connect state
            		isConnectDevice = false;
            		
            		// Change the connect state text
            		mtvGattStatus.setText("No connected device");
            		mtvGattStatus.setTextColor(android.graphics.Color.RED);
            		            		
            		// Update Menu
            		invalidateOptionsMenu();
            		
            		// turn off the update speed ui thread
            	} else if(GattConnectState.STATE_CONNECTED == mConnectionState) {
            		if(D) Log.i(TAG, "The connection state now is STATE_CONNECTED");
            		
            		mbtnTx.setEnabled(false);
            		envMeas.setEnabled(false);
            		disMeas.setEnabled(false);
            		velMeas.setEnabled(false);
            		accMeas.setEnabled(false);
            		// Change the connect state text
            		mtvGattStatus.setText("Waiting for the device to respond.");
            		mtvGattStatus.setTextColor(android.graphics.Color.RED);
            	} else if(GattConnectState.STATE_CHARACTERISTIC_CONNECTED == mConnectionState) {
            		if(D) Log.i(TAG, "The connection state now is STATE_CHARACTERISTIC_CONNECTED");
            	
            		mbtnTx.setEnabled(true);
            		envMeas.setEnabled(true);
            		disMeas.setEnabled(true);
            		velMeas.setEnabled(true);
            		accMeas.setEnabled(true);
            		// change the connect state
            		isConnectDevice = true;
            		
            		// Change the connect state text
            		mtvGattStatus.setText("Connected with ( " + mDevice.getName() + " )");
            		mtvGattStatus.setTextColor(android.graphics.Color.BLUE);

    	    		enableNotification(mBluetoothGatt, mTestCharacter);
        	        isRxOn = true;
        	        // Create a thread to receice data and update ui
        	        mThreadRx = new ThreadRx();
        	        mThreadRx.start();
        	        shake=false;
        	        Shake();
            		// Update Menu
            		invalidateOptionsMenu();
            		
            		// turn on the update speed ui thread
            		// Create a thread to update speed ui
            	} else if(GattConnectState.STATE_CHARACTERISTIC_NOT_FOUND == mConnectionState) {
            		if(D) Log.i(TAG, "The connection state now is STATE_CHARACTERISTIC_NOT_FOUND");
            		
            		mbtnTx.setEnabled(false);
            		envMeas.setEnabled(false);
            		disMeas.setEnabled(false);
            		velMeas.setEnabled(false);
            		accMeas.setEnabled(false);
            		// Change the connect state text
            		mtvGattStatus.setText(mDevice.getName() + "is not the right device");
            		mtvGattStatus.setTextColor(android.graphics.Color.RED);
            	}
            } else if(msg.what == MSG_SEND_MESSAGE_ERROR_UI_UPDATE) {
            	if(msg.arg1 == 0) {
            		Toast.makeText(UnvarnishedTransmissionActivity.this, "the tx string is empty!", Toast.LENGTH_SHORT).show();
            	} else if (msg.arg1 == 1) {
            		Toast.makeText(UnvarnishedTransmissionActivity.this, "the tx string have some error!", Toast.LENGTH_SHORT).show();
        			
            	} else if(msg.arg1 == 2) {
            		Toast.makeText(UnvarnishedTransmissionActivity.this, "the last tx string didn't all send!", Toast.LENGTH_SHORT).show();
            	}
            	// close the auto tx
            	
            }
            
            
        }
    };
    
    // Auto Tx Thread
   
    
    
    // Rx Thread
    public class ThreadRx extends Thread {
    	public void run() {
    		if(D) Log.i(TAG, "rx thread is run");
    		while(isRxOn) {
    			// every 100ms send a update message, about 20/10*100=200Byte
    			try {
    				Thread.sleep(RX_STRING_UPDATE_TIME);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			// send the msg, here may send less times MSG, so we use the StringBuildler
    	    	//Message message = new Message();
    	        //message.what = MSG_EDIT_RX_STRING_UPDATE;
    	        //handler.sendMessage(message);
    		}
    		if(D) Log.i(TAG, "rx thread is stop");
    	}
    }
    
    
    // unpack and send thread
    public class ThreadUnpackSend extends Thread {
    	byte[] sendData;
    	ThreadUnpackSend(byte[] data) {
    		sendData = data;
    	}
    	public void run() {
    		if(D) Log.d(TAG, "ThreadUnpackSend is run");
    		// time test
    		if(D) Log.e("TIME_thread run", String.valueOf(System.currentTimeMillis()));
    		// set the unpack sending flag 
    		isUnpackSending = true;
    		// send data to the remote device
    		if(null != mTestCharacter) {
    			// unpack the send data, because of the MTU size is limit
    			int length = sendData.length;
    			int unpackCount = 0;
    			byte[] realSendData;
    			do {
    				
    				if(length <= MTU_PAYLOAD_SIZE_LIMIT) {
    					realSendData = new byte[length];
    					System.arraycopy(sendData, unpackCount * MTU_PAYLOAD_SIZE_LIMIT, realSendData, 0, length);
    					
    					// update length value
    		            length = 0;
    				} else {
    					realSendData = new byte[MTU_PAYLOAD_SIZE_LIMIT];
    					System.arraycopy(sendData, unpackCount * MTU_PAYLOAD_SIZE_LIMIT, realSendData, 0, MTU_PAYLOAD_SIZE_LIMIT);
    					
    					// update length value
    		            length = length - MTU_PAYLOAD_SIZE_LIMIT;
    				}
    				
    				SendData(realSendData);
    				
    	            // unpack counter increase
    	            unpackCount++;
    	            
    	            
    			} while(length != 0);
    			
    			// set the unpack sending flag 
        		isUnpackSending = false;
        		
    			if(D) Log.d(TAG, "ThreadUnpackSend stop");
    		}//if(null != mTestCharacter)
    	}//run
    }
    
    private void SendData(byte[] realData) {
    	// for GKI get buffer error exit
		long timeCost = 0;
    	
        
		// initial the status
		writeCharacteristicError = true;
        
		while(true == writeCharacteristicError) {
			// mBluetoothGatt.getConnectionState(mDevice) can not use in thread, so we use a flag to
			// break the circulate when disconnect the connect
			if(true == isConnectDevice) {
				// for GKI get buffer error exit
				timeCost = System.currentTimeMillis();
				
				if(D) Log.e(TAG, "writeCharacteristicError start Status:" + writeCharacteristicError);
				
                // initial the status
                isWriteCharacteristicOk = false;
                
				// Set the send type(command)
				mTestCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
				// include the send data
				mTestCharacter.setValue(realData);
				// send the data
	            mBluetoothGatt.writeCharacteristic(mTestCharacter);
	            
	            // wait for characteristic write ok
                while(isWriteCharacteristicOk != true) {
                	if(false == isConnectDevice) {
                		if(D) Log.e(TAG, "break the circulate when disconnect the connect, no callback");
                		break;
                	}
                	
                	// for GKI get buffer error exit
                	// if 10 seconds no callback we think GKI get buffer error
                	if((System.currentTimeMillis() - timeCost)/1000 > 10) {
                		if(D) Log.e(TAG, "GKI get buffer error close the BT and exit");
                		// becouse GKI error, so we should close the bt
                        if (mBluetoothAdapter.isEnabled()) {
                            if(D) Log.d(TAG, "close bluetooth");
                            mBluetoothAdapter.disable();
                        }
                        
                        // close the activity
                        finish();
                	}
                };
                
                if(D) Log.e(TAG, "writeCharacteristicError stop Status:" + writeCharacteristicError);
			} else {
				// break the circulate when disconnect the connect
				if(D) Log.e(TAG, "break the circulate when disconnect the connect, write error");
				break;
			}
		}
		
		if(D) Log.d(TAG, "send data is: " + Arrays.toString(realData));
		
		// update tx counter, only write ok then update the counter
		if(false == writeCharacteristicError) {
			mTxCounter = mTxCounter + realData.length;
		}
		
		// send the msg, here may send less times MSG, so we use the StringBuildler
    	Message message = new Message();
        message.what = MSG_EDIT_TX_STRING_UPDATE;
        handler.sendMessage(message);
        
    }
    
    // set the connect state
    private void SetConnectionState(GattConnectState state) {
    	// Update the connect state
    	mConnectionState = state;
    	
    	// send the msg
    	Message message = new Message();
        message.what = MSG_CONNECTION_STATE_UPDATE;
        handler.sendMessage(message);
    }
    
    /*geyun send ccc---------notify characteristic*/
    private void enableNotification(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        if(D) Log.i(TAG, "geyun write CCC    +");
        gatt.setCharacteristicNotification(characteristic, true);
        // enable remote notification
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
        if(D) Log.i(TAG, "geyun write CCC   -");
    }
    private void Dialog(){
    	new AlertDialog.Builder(this).setTitle(context.getString(R.string.title)).setMessage("Error").setPositiveButton("OK", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).show();
    }
}
