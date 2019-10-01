package lawi.ads.arduino;

import com.fazecast.jSerialComm.SerialPort;
import lawi.ads.ArrayUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ArduinoConnector {

    private final SerialPort comPort;

    public ArduinoConnector(String portDescription, int baudRate) {
        comPort = SerialPort.getCommPort(portDescription);
        comPort.setBaudRate(baudRate);
    }

    public boolean openConnection(){
        boolean result = comPort.openPort();
        try{ Thread.sleep(500); } catch (InterruptedException e) {}
        return result;
    }

    public void closeConnection() {
        comPort.closePort();
    }

    /* Получение и установка параметров соединения */

    public void setBaudRate(int baudRate){
        comPort.setBaudRate(baudRate);
    }

    public int getBaudRate() {
        return comPort.getBaudRate();
    }

    public String getPortDescription(){
        return comPort.getSystemPortName();
    }

    public InputStream getInputStream(){
        return comPort.getInputStream();
    }

    public OutputStream getOutputStream(){
        return comPort.getOutputStream();
    }

    /* Подготовка к приему и передаче данных */

    private void prepareSerialRead(){
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
    }

    private void prepareSerialWrite(){
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        try{Thread.sleep(5);} catch(InterruptedException e){}
    }

    /* Ввод данных */

    public void serialReadClear() {
        try {
            comPort.getInputStream().skip(comPort.getInputStream().available());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public int serialReadByte(){
        prepareSerialRead();
        try {
            return getInputStream().read();
        } catch (IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    public byte serialReadByteWait(int sleepMills) {
        int result;
        while ((result = serialReadByte()) == -1) {
            if (sleepMills == 0){
                Thread.yield();
            } else {
                try{Thread.sleep(sleepMills);} catch(InterruptedException e){}
            }
        }

        return (byte) result;
    }

    public byte[] serialReadBytes() throws IOException{
        prepareSerialRead();

        int available = getInputStream().available();
        byte[] result = new byte[available];
        getInputStream().read(result);

        return result;
    }

    public byte[] serialReadBytesWait(int count, int sleepMills) throws IOException{
        if (count <= 0) return new byte[0];

        byte[] result = new byte[count];
        while (getInputStream().available() < count) {
            if (sleepMills == 0){
                Thread.yield();
            } else {
                try{Thread.sleep(sleepMills);} catch(InterruptedException e){}
            }
        }

        getInputStream().read(result);
        return result;
    }

    public String serialReadLine(){
        prepareSerialRead();
        Scanner in = new Scanner(getInputStream());

        String result = null;
        if (in.hasNextLine()) result = in.nextLine();

        in.close();
        return result;
    }

    public String serialReadLineWait(int sleepMills){
        String result;
        while ((result = serialReadLine()) == null) {
            if (sleepMills == 0){
                Thread.yield();
            } else {
                try{Thread.sleep(sleepMills);} catch(InterruptedException e){}
            }
        }

        return result;
    }

    public String[] serialReadLines(){
        prepareSerialRead();
        Scanner in = new Scanner(getInputStream());
        List<String> result = new ArrayList<>();

        while(in.hasNextLine()) result.add(in.nextLine());

        in.close();
        return ArrayUtils.collectionStringToArray(result);
    }

    /* Вывод данных */

    public void serialWrite(byte b) throws IOException{
        prepareSerialWrite();
        getOutputStream().write(b);
        getOutputStream().flush();
    }

    public void serialWrite(byte[] b) throws IOException{
        prepareSerialWrite();
        getOutputStream().write(b);
        getOutputStream().flush();
    }

    public void serialWrite(String s){
        prepareSerialWrite();
        PrintWriter out = new PrintWriter(comPort.getOutputStream());

        out.print(s);
        out.close();
    }

    public void serialWriteLine(String s){
        serialWrite(s + "\r\n");
    }
}