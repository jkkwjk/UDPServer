/**
 * 软串口高波特率下会出现某些位传输错误
 * 以及 我的ESP01flash太小 找不到合适的固件 不能修改波特率
 * 
 * 本程序配合服务端可以实现连接过程中错误重发, 但如果是在数据传输透传时发生错误则不能纠错
**/
#include <SoftwareSerial.h>
int wait = 100;

SoftwareSerial wifi(7,6); // RX, TX
#define debugSerial wifi
const String appId = "test,";
bool sendUtilSuccess(String cmd, int start, int end, String result, String msg, int timeout = 0) {
  String data;
  int cnt = 0;
  do{
    if (timeout != 0 && cnt >= timeout) {
      return false;
    }
    debugSerial.println(cmd);
    delay(wait);
    
    data = readAllInSS();
    delay(wait);
    cnt++;
  }while(data.substring(start, end) != result);
  
  Serial.println(msg);
  return true;
}

/**
 * 连接成功后, 发送数据
 */
void send(String cmd, String data) {
  
}

void setup() {
  Serial.begin(115200);
  debugSerial.begin(115200);
  modeSwitch();

  reset: // 如果一直连不上的话就重新执行这些代码

  Serial.print("connecting...");
  delay(wait);
  debugSerial.print("+++");
  while(true) {
    delay(wait);
    debugSerial.println("AT+CWJAP?"); //获得连接的AP
    delay(wait);
    
    String data = readAllInSS();
    if (data.substring(12,19) == "+CWJAP:") {
      Serial.println("连接WIFI成功");

      String cmd = "AT+CIPSTART=\"UDP\",\"192.168.1.106\",2333";
      sendUtilSuccess(cmd, 0, cmd.length(), cmd, "连接服务器成功");

      cmd = "AT+CIPMODE=1";
      sendUtilSuccess(cmd, 0, cmd.length(), cmd, "设置透传模式");
      
      cmd = "AT+CIPSEND";
      bool isSuccess = sendUtilSuccess(cmd, 0, 2, "OK", "进入透传", 20);
      if (isSuccess) {
        debugSerial.print(appId);
      }else {
        wait += 500; // 增大稳定性延迟
        wait = max(wait, 2000);
        goto reset; //重试
      }
      
      break;
    }else {
      Serial.print(".");
    }
  }
}

void loop() {
  if (debugSerial.available()){
    String cmd = readAllInSS();
    if (cmd == "1") {
      digitalWrite(8, HIGH);
      debugSerial.print(appId + "OK");
    }else if (cmd == "0") {
      digitalWrite(8, LOW);
      debugSerial.print(appId + "OK");
    }else {
      Serial.print(data);
    }
  }
  

  delay(10);
}



String readAllInSS() {
  String data = "";
  while(debugSerial.available()) {
    data += (char)debugSerial.read();
  }
  return data;
}

void modeSwitch() {
  pinMode(8, OUTPUT);
}

void debug(){
  if (debugSerial.available()) {
    Serial.write(debugSerial.read());
  }
  if (Serial.available()) {
    debugSerial.write(Serial.read());
  }
}
