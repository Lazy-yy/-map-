package com.zyy.baidumap;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.zyy.baidumap.Info;
import com.zyy.baidumap.R;

public class Info implements Serializable{

	private static final long serialVersionUID = -1010711775392052966L;
	private double latitude;
	private double longitude;
	private int imgId;
	
	private String name;
	private String distance;
	private int zan;
	
	
	public static List<Info> infos = new ArrayList<Info>();
	
	       
	static
	{
		infos.add(new Info(22.1286625012,113.3552065451, R.drawable.a01, "Ӣ�׹���С�ù�",
				"����209��", 1456));
		infos.add(new Info(22.1349632004,113.3616957725, R.drawable.a02, "ɳ������ϴԡ����",
				"����897��", 456));
		infos.add(new Info(22.1310989745,113.3604290745, R.drawable.a03, "�廷��װ��",
				"����249��", 1456));
		infos.add(new Info(22.1267925012,113.3573165451, R.drawable.a04, "���׼�����С��",
				"����679��", 1456));
	}
		
	
	
	
	
	
	
	
	public Info(double latitude, double longitude, int imgId, String name, String distance, int zan) {
		
		this.latitude = latitude;
		this.longitude = longitude;
		this.imgId = imgId;
		this.name = name;
		this.distance = distance;
		this.zan = zan;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public int getImgId() {
		return imgId;
	}
	public void setImgId(int imgId) {
		this.imgId = imgId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public int getZan() {
		return zan;
	}
	public void setZan(int zan) {
		this.zan = zan;
	}
	
	
	
}
