package com.zyy.baidumap;



import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//方向传感器
public class MyOrientationListener implements SensorEventListener {

	private SensorManager mSensorManager;//传感器的管理者
	private Context mContext;//上下文
	private Sensor mSensor; //传感器
	
	
	private float lastX;
	
	
	public MyOrientationListener(Context context){
		this.mContext=context;
		
	}
	
	//开始监听
	@SuppressWarnings("deprecation")
	public void start(){
		
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		if(mSensorManager!=null){
			
			//获得方向传感器
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			
		}
		if(mSensor!=null){
			
			mSensorManager.registerListener(this, mSensor,SensorManager.SENSOR_DELAY_UI);
		}
		
		
	}
	
	//结束监听
	public void stop(){
		
		mSensorManager.unregisterListener(this);
		
	}
	
	//传感器 方向发生变化
	@SuppressWarnings(
	{ "deprecation" })
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
		{
			float x = event.values[SensorManager.DATA_X];

			if (Math.abs(x - lastX) > 1.0)
			{
				if (mOnOrientationListener != null)
				{
					mOnOrientationListener.onOrientationChanged(x);
				}
			}

			lastX = x;

		}
	}

	private OnOrientationListener mOnOrientationListener;

	public void setOnOrientationListener(
			OnOrientationListener mOnOrientationListener)
	{
		this.mOnOrientationListener = mOnOrientationListener;
	}

	public interface OnOrientationListener
	{
		void onOrientationChanged(float x);
	}

     
   //精度
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub

 	}
 	

}



  
  
  
  
  
  
  
  