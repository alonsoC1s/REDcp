package mx.com.redcup.redcup.myDataModels;


public class MyDate {
    public int year;
    public int month;
    public int  day;
    public int hour;
    public int minute;

    public MyDate(){

    }

    public MyDate(int year, int month, int day, int hour, int minute ){
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour(){return this.hour;}

    public int getMinutes(){ return  this.minute;}
}
