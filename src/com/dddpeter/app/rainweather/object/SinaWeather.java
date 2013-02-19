package com.dddpeter.app.rainweather.object;

public class SinaWeather {
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDayStatus() {
		return dayStatus;
	}
	public void setDayStatus(String dayStatus) {
		this.dayStatus = dayStatus;
	}
	public String getNightStatus() {
		return nightStatus;
	}
	public void setNightStatus(String nightStatus) {
		this.nightStatus = nightStatus;
	}
	public String getWindDayDrection() {
		return windDayDrection;
	}
	public void setWindDayDrection(String windDayDrection) {
		this.windDayDrection = windDayDrection;
	}
	public String getWindNightDrection() {
		return windNightDrection;
	}
	public void setWindNightDrection(String windNightDrection) {
		this.windNightDrection = windNightDrection;
	}
	public String getWindDayPower() {
		return windDayPower;
	}
	public void setWindDayPower(String windDayPower) {
		this.windDayPower = windDayPower;
	}
	public String getWindNightPower() {
		return windNightPower;
	}
	public void setWindNightPower(String windNightPower) {
		this.windNightPower = windNightPower;
	}
	public int getHighTemprature() {
		return highTemprature;
	}
	public void setHighTemprature(int highTemprature) {
		this.highTemprature = highTemprature;
	}
	public int getLowTemprature() {
		return lowTemprature;
	}
	public void setLowTemprature(int lowTemprature) {
		this.lowTemprature = lowTemprature;
	}
	public String getCurrentDate() {
		return currentDate;
	}
	public void setCurrentDate(String currentDate) {
		this.currentDate = currentDate;
	}
	private String city;
	private String dayStatus;
	private String nightStatus;
	private String windDayDrection;
	private String windNightDrection;
	private String  windDayPower;
	private String  windNightPower;
	private int highTemprature;
	private int lowTemprature;
	private String  currentDate;
}
