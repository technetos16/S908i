package com.bluetooth.unvarnishedtransmission;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.opencsv.CSVReader;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

public class StringByteTrans {
	
	
	private static final String TAG = "UnvarnishedTransmissionActivity";
	//�ж��ٶ�״̬
	public static int compareVel(int dev,int bea, float vel){
		float[][][] velData = new float[4][2][3];
		float[] defined=new float[3];
		velData[0][0][0]= (float) 2.3;velData[0][0][1]= (float) 4.5;velData[0][0][2]= (float) 7.1;
		velData[0][1][0]= (float) 3.5;velData[0][1][1]= (float) 7.1;velData[0][1][2]= (float)11;
		velData[1][0][0]= (float) 1.4;velData[1][0][1]= (float) 2.8;velData[1][0][2]= (float) 4.5;
		velData[1][1][0]= (float) 2.3;velData[1][1][1]= (float) 4.5;velData[1][1][2]= (float) 7.1;
		velData[2]=velData[0];velData[3]=velData[1];
		if(dev==4){
			try {
			CSVReader data = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath()+"/config.csv"));
			String[] unuse= data.readNext();
			String[] unuse2= data.readNext();
			String[] unuse3= data.readNext();
			String[] unuse4= data.readNext();
			String[] line = data.readNext();
			int i;
			String[]line2=data.readNext();
			for(i=0;i<3;i++){
				defined[i]=Float.parseFloat(line2[i]);}
			data.close();}
		catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
		catch (IOException e) {
				e.printStackTrace();}
		if (vel>defined[2]){return Color.parseColor("#FF0000");}
					else if (vel>defined[1]){return Color.parseColor("#FFFF00");}
					else if (vel>defined[0]){return Color.parseColor("#00FF00");}
					else{return Color.parseColor("#FFFFFF");}
				}
		else{if (vel>velData[dev][bea][2]){return Color.parseColor("#FF0000");}
				else if (vel>velData[dev][bea][1]){return Color.parseColor("#FFFF00");}
				else if (vel>velData[dev][bea][0]){return Color.parseColor("#00FF00");}
				else{return Color.parseColor("#FFFFFF");}}
		
	}
	
	//�жϼ��ٶ�״̬
	public static int compareAcc(float acc){
		float[] accData=new float[3];
		try {
			
			CSVReader data = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath()+"/config.csv"));
			int i=0;
			String[] line = data.readNext();
			for(i=0;i<3;i++){
				accData[i]=Float.parseFloat(line[i]);
				Log.d(TAG, "value is" + line[i]);
			}
			
	
			data.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (acc>accData[2]){return Color.parseColor("#FF0000");}
		else if (acc>accData[1]){return Color.parseColor("#FFFF00");}
		else if (acc>accData[0]){return Color.parseColor("#00FF00");}
		else{return Color.parseColor("#FFFFFF");}
	}
	
	//�жϰ���״̬
	public static int compareEnv(float env){
		float[] envData=new float[3];
		try {
			
			CSVReader data = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath()+"/config.csv"));
			int i=0;
			//�������õ���Ҫ����
			String[] unuse= data.readNext();
			String[] line = data.readNext();
			for(i=0;i<3;i++){
				envData[i]=Float.parseFloat(line[i]);
			}
	
			data.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (env>envData[2]){return Color.parseColor("#FF0000");}
		else if (env>envData[1]){return Color.parseColor("#FFFF00");}
		else if (env>envData[0]){return Color.parseColor("#00FF00");}
		else{return Color.parseColor("#FFFFFF");}
	}
	
	//�ж�λ��״̬
	public static int compareDis(int dev,int bea, float dis){
		float[][][] disData = new float[4][2][3];
		float[] defined=new float[3];
		disData[0][0][0]= (float) 82;disData[0][0][1]= (float) 161;disData[0][0][2]= (float) 255;
		disData[0][1][0]= (float) 127;disData[0][1][1]= (float) 255;disData[0][1][2]= (float)396;
		disData[1][0][0]= (float) 62;disData[1][0][1]= (float) 127;disData[1][0][2]= (float) 201;
		disData[1][1][0]= (float) 105;disData[1][1][1]= (float) 201;disData[1][1][2]= (float) 320;
		disData[2][0][0]= (float) 51;disData[2][0][1]= (float) 102;disData[2][0][2]= (float) 158;
		disData[2][1][0]= (float) 79;disData[2][1][1]= (float) 158;disData[2][1][2]= (float)255;
		disData[3][0][0]= (float) 31;disData[3][0][1]= (float) 62;disData[3][0][2]= (float) 102;
		disData[3][1][0]= (float) 51;disData[3][1][1]= (float) 102;disData[3][1][2]= (float) 158;
		
		if(dev==4){
			try {
			CSVReader data = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath()+"/config.csv"));
			String[] unuse= data.readNext();
			String[] unuse2= data.readNext();
			String[] unuse3= data.readNext();
			String[] line = data.readNext();
			String[]line2=data.readNext();
			for(int i=0;i<3;i++){
				defined[i]=Float.parseFloat(line2[i]);}
			data.close();}
		catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
		catch (IOException e) {
				e.printStackTrace();}
		if (dis>defined[2]){return Color.parseColor("#FF0000");}
		else if (dis>defined[1]){return Color.parseColor("#FFFF00");}
		else if (dis>defined[0]){return Color.parseColor("#00FF00");}
		else{return Color.parseColor("#FFFFFF");}
			}
		else{if (dis>disData[dev][bea][2]){return Color.parseColor("#FF0000");}
				else if (dis>disData[dev][bea][1]){return Color.parseColor("#FFFF00");}
				else if (dis>disData[dev][bea][0]){return Color.parseColor("#00FF00");}
				else{return Color.parseColor("#FFFFFF");}}
	}
	
	//��ȡ�ֺ�
	public static float getTextSize(String a){
		float[] textSize=new float[3];
		try {
			
			CSVReader data = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath()+"/config.csv"));
			
			String[] unuse= data.readNext();
			String[] unuse2= data.readNext();
			String[] unuse3= data.readNext();
			String[] line = data.readNext();
			for(int i=0;i<3;i++){
				textSize[i]=Float.parseFloat(line[i]);
			}
			data.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (a=="S"){return textSize[0];}
		else if (a=="M"){return textSize[1];}
		else{return textSize[2];}
		
	}
	
	//��ȡϵ����DAVE�ֱ��Ӧ0123
	public static float getFactor(String a){
		float[] factor=new float[4];
		try {
			
			CSVReader data = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath()+"/config.csv"));
			
			String[] unuse= data.readNext();
			String[] unuse2= data.readNext();
			String[] line = data.readNext();
			for(int i=0;i<4;i++){
				factor[i]=Float.parseFloat(line[i]);
			}
			data.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (a=="D"){return factor[0];}
		else if (a=="A"){return factor[1];}
		else if (a=="V"){return factor[2];}
		else{return factor[3];}
		
	}
	
	
	/**   
	 * �ַ���ת����ʮ�������ַ���  
	 * @param String str ��ת����ASCII�ַ���  
	 * @return String ÿ��Byte֮��ո�ָ�����: [61 6C 6B]  
	 */      
	public static String str2HexStr(String str)    
	{      
	  
	    char[] chars = "0123456789ABCDEF".toCharArray();      
	    StringBuilder sb = new StringBuilder("");    
	    byte[] bs = str.getBytes();      
	    int bit;      
	        
	    for (int i = 0; i < bs.length; i++)    
	    {      
	        bit = (bs[i] & 0x0f0) >> 4;      
	        sb.append(chars[bit]);      
	        bit = bs[i] & 0x0f;      
	        sb.append(chars[bit]);    
	        sb.append(' ');    
	    }      
	    return sb.toString().trim();      
	}    
	    
	/**   
	 * ʮ������ת���ַ���  
	 * @param String str Byte�ַ���(Byte֮���޷ָ��� ��:[616C6B])  
	 * @return String ��Ӧ���ַ���  
	 */      
	public static String hexStr2Str(String hexStr)    
	{      
	    String str = "0123456789ABCDEF";      
	    hexStr = hexStr.toUpperCase();
	    char[] hexs = hexStr.toCharArray();
	    
	    byte[] bytes = new byte[hexStr.length() / 2];      
	    // judge the input string good or not
        if (hexStr.length() % 2 == 1) {
        	return new String();
        }
        // judge the input string good or not
	    for(int i = 0; i < hexStr.length(); i++) {
	    	if((hexs[i] >= '0' && hexs[i] <= '9') || (hexs[i] >= 'A' && hexs[i] <= 'F')) {
            } else {
            	return new String();
            }
	    }
	    int n;      
	  
	    for (int i = 0; i < bytes.length; i++)    
	    {      
	        n = str.indexOf(hexs[2 * i]) * 16;      
	        n += str.indexOf(hexs[2 * i + 1]);
	        bytes[i] = (byte) (n & 0xff);
	    }      
	    return new String(bytes);      
	}    
	    
	
	/**  
	 * bytesת����ʮ�������ַ���  
	 * @param byte[] b byte����  
	 * @return String ÿ��Byteֵ֮��ո�ָ�  
	 */    
	public static String byte2HexStr(byte[] b)    
	{    
	    String stmp="";    
	    StringBuilder sb = new StringBuilder("");    
	    for (int n=0;n<b.length;n++)    
	    {    
	        stmp = Integer.toHexString(b[n] & 0xFF);    
	        sb.append((stmp.length()==1)? "0"+stmp : stmp);    
	        sb.append(" ");    
	    }    
	    return sb.toString().toUpperCase().trim();    
	}    
	    
	/**  
	 * bytes�ַ���ת��ΪByteֵ  
	 * @param String src Byte�ַ�����ÿ��Byte֮��û�зָ���  
	 * @return byte[]  
	 */    
	public static byte[] hexStringToBytes(String hexString) {  
	    if (hexString == null || hexString.equals("")) {  
	        return null;  
	    }  
	    hexString = hexString.toUpperCase();  
	    int length = hexString.length() / 2;  
	    char[] hexChars = hexString.toCharArray();  
	    byte[] d = new byte[length];  
	    for (int i = 0; i < length; i++) {  
	        int pos = i * 2;  
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
	    }  
	    return d;  
	}  
	private static byte charToByte(char c) {  
	    return (byte) "0123456789ABCDEF".indexOf(c);  
	}  
	/**  
	 * ASCII�ַ���ת��ΪByteֵ  
	 * @param String src ascii�ַ���
	 * @return byte[]  
	 */    
	public static byte[] Str2Bytes(String str)    
	{    
		if (str == null) {
            throw new IllegalArgumentException(
                    "Argument str ( String ) is null! ");
        }
        byte[] b = new byte[str.length() / 2];
        
        try {
			b = str.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return b; 
	}    
	  
	/**
     * �ֽ�����תΪ��ͨ�ַ�����ASCII��Ӧ���ַ���
     * 
     * @param bytearray
     *            byte[]
     * @return String
     */
    public static String Byte2String(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }
    public static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    }  
    public static float getFloat(byte[] a) {  
    		    // 4 bytes  
    			
    		    int accum = 0;  
    		    for ( int shiftBy = 0; shiftBy < 4; shiftBy++ ) {  
    		            accum |= (a[shiftBy] & 0xff) << shiftBy * 8;  
    		    }  
    		    return Float.intBitsToFloat(accum);  
    		} 
	/**  
	 * String���ַ���ת����unicode��String  
	 * @param String strText ȫ���ַ���  
	 * @return String ÿ��unicode֮���޷ָ���  
	 * @throws Exception  
	 */    
	public static String strToUnicode(String strText)    
	    throws Exception    
	{    
	    char c;    
	    StringBuilder str = new StringBuilder();    
	    int intAsc;    
	    String strHex;    
	    for (int i = 0; i < strText.length(); i++)    
	    {    
	        c = strText.charAt(i);    
	        intAsc = (int) c;    
	        strHex = Integer.toHexString(intAsc);    
	        if (intAsc > 128)    
	            str.append("\\u" + strHex);    
	        else // ��λ��ǰ�油00    
	            str.append("\\u00" + strHex);    
	    }    
	    return str.toString();    
	}    
	    
	/**  
	 * unicode��Stringת����String���ַ���  
	 * @param String hex 16����ֵ�ַ��� ��һ��unicodeΪ2byte��  
	 * @return String ȫ���ַ���  
	 */    
	public static String unicodeToString(String hex)    
	{    
	    int t = hex.length() / 6;    
	    StringBuilder str = new StringBuilder();    
	    for (int i = 0; i < t; i++)    
	    {    
	        String s = hex.substring(i * 6, (i + 1) * 6);    
	        // ��λ��Ҫ����00��ת    
	        String s1 = s.substring(2, 4) + "00";    
	        // ��λֱ��ת    
	        String s2 = s.substring(4);    
	        // ��16���Ƶ�stringתΪint    
	        int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);    
	        // ��intת��Ϊ�ַ�    
	        char[] chars = Character.toChars(n);    
	        str.append(new String(chars));    
	    }    
	    return str.toString();    
	}   
	public static byte[] int2byte(int a) throws Exception{
		return new byte[] {  
		        (byte) ((a >> 24) & 0xFF),  
		        (byte) ((a >> 16) & 0xFF),     
		        (byte) ((a >> 8) & 0xFF),     
		        (byte) (a & 0xFF)};  
	}
	public static int byte2int(byte[] b) throws Exception{
		return   b[3] & 0xFF |  
	            (b[2] & 0xFF) << 8 |  
	            (b[1] & 0xFF) << 16 |  
	            (b[0] & 0xFF) << 24;  
	}
}
