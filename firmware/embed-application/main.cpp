#include "mbed.h"
#include "platform/mbed_thread.h"
#include "MMA8652.h" //accelerometer
#include "MiCS6814_GasSensor/MiCS6814_GasSensor.h" //Mics Gas Sensor
#include "Adafruit_SGP30.h" // MOX Sensor
#include "inttypes.h" // For formatting uint16_t
#include "BME280/BME280.h" // Temperature Sensor

#include "CPU_Usage.h"


#define 	   MAIN_SLEEP_MS                   500
#define        COV_RATIO                       0.2            //ug/mmm / mv
#define        NO_DUST_VOLTAGE                 500          //mv   

#include<stdlib.h>
#include<math.h>
#include<string.h>

using namespace std;


Serial pc(USBTX, USBRX);
Timer t1;                        // create your timer object
CPU_Usage cpu(t1, 1);    // create you CPU_Usage object

RawSerial phone(D1, D0, 9600);
RawSerial mac_terminal(USBTX, USBRX);

DigitalOut iled(D7);
AnalogIn analog_value(A0); 

// defining class as objects //
MMA8652 mma8652(D14, D15);
MiCS6814_GasSensor mic_GS(D14, D15);
Adafruit_SGP30 SGP30(D14, D15);
BME280 bme_280(D14, D15);

float accel_data[3];
float density, voltage;


//starting encryption


int x, y, n, t;
long int e[50], d[50],temp[200];

//char msg[100];
int prime(long int); //function to check for prime number
void encryption_key();
long int cd(long int);
string encrypt(char *);
void set_encrypt_key();
string encrypt_main(string);

int main(){
		//setting encryption key
	
		
		//initialising sensors//
		
   		uint8_t value = 0;
		mic_GS.initialize();
		mma8652.MMA8652_config();
		SGP30.begin();
		SGP30.IAQinit();
		SGP30.setIAQBaseline(0x8973, 0x8aae);
		bme_280.init();
		int data_packet = 0;		
		iled = 0;
		
		while(1) {
			   // tell the CPU_Usage object that work has started
			cpu.working();	
			// Reading Gas Sensor Data
			
			float _NH3 = mic_GS.getGas(NH3);
			float _CO = mic_GS.getGas(CO);
			float _NO2 = mic_GS.getGas(NO2);
			float _C3H8 = mic_GS.getGas(C3H8);
			float _C4H10 = mic_GS.getGas(C4H10);
			float _CH4 = mic_GS.getGas(CH4);
			float _H2 = mic_GS.getGas(H2);
			float _C2H5OH = mic_GS.getGas(C2H5OH);

			// Reading SGP sensor data
			SGP30.IAQmeasure();
			uint16_t my_TVOC = SGP30.TVOC;
			uint16_t my_eCO2 = SGP30.eCO2;

			// Accessing voltage readings on the dust sensor to extrapolate PM2.5 data //
			iled = 1;
			wait_us(320);
			voltage = analog_value.read() * 3300 * 11;
			iled = 0;

			// voltage = _filter(voltage);

			if(voltage >= NO_DUST_VOLTAGE){

				voltage -= NO_DUST_VOLTAGE;
				density = voltage * COV_RATIO;
			}else{

				density = 0;
			}

			float PM2_5_Conc = density;
			double temperature = bme_280.temperature() / 100.0 ; // actual temperature in **.** DegC

			if (temperature >= 23.0){
				temperature = (temperature / 4.0);
			}

			double pressure = (bme_280.pressure()/256.0) / 100.0;
			double humidity = (bme_280.humidity()/1024.0);
			data_packet = data_packet + 1;
			
			/* JSON Print format */
			//const char* s = "NH3";
			//char* t = new char[44];
			//strncpy(t, s, 44);
			
			
			set_encrypt_key();
			string nh3 = encrypt_main("NH3");
			
			string co = encrypt_main("CO");
			string no2 = encrypt_main("NO2");
			string tvoc = encrypt_main("TVOC");
			string eco2 = encrypt_main("eco2");
			string dust = encrypt_main("Dust");
			string temp = encrypt_main("Temperature");
			string press = encrypt_main("Pressure");
			string hum = encrypt_main("Humidity");
			
			string nh3_val = encrypt_main(to_string(floorf(_NH3 * 100) / 100));
			string co_val = encrypt_main(to_string(floorf(_CO * 100) / 100));
			string no2_val = encrypt_main(to_string(floorf(_NO2 * 100) / 100));
			string tvoc_val = encrypt_main(to_string(int(floorf(my_TVOC * 100) / 100)));
			string eco2_val = encrypt_main(to_string(int(floorf(my_eCO2 * 100) / 100)));

			string dust_val = encrypt_main(to_string(floorf(PM2_5_Conc * 10) / 10));
			string temp_val = encrypt_main(to_string(floorf(temperature * 100) / 100));
			string press_val = encrypt_main(to_string(floorf(pressure * 10000) / 10000));
			string hum_val = encrypt_main(to_string(floorf(humidity * 10000) / 10000));
			
			//mac_terminal.printf("{%s:\"%.2f\"}",nh3.c_str(),_NH3);
			//mac_terminal.printf("{  \"NH3\": \"%.2f\", \"CO\": \"%.2f\", \"NO2\": \"%.2f\", \"TVOC\": \"" "%" PRIu16 "\", \"eCO2\": \"" "%" PRIu16 "\", \"Dust_Concentration\": \"%4.1f\", \"Temperature\": \"%.2f\", \"Pressure\": \"%.4f\", \"Humidity\": \"%.4f\" }", _NH3, _CO, _NO2, my_TVOC, my_eCO2, PM2_5_Conc, temperature, pressure, humidity);
			//mac_terminal.printf("{  %s: , %s: \"%.2f\", %s: \"%.2f\", %s: \"" "%" PRIu16 "\", %s: \"" "%" PRIu16 "\", %s: \"%4.1f\", %s: \"%.2f\", %s: \"%.4f\", %s: \"%.4f\" }", nh3.c_str(),_NH3,co.c_str(),_CO, no2.c_str(),_NO2,tvoc.c_str(), my_TVOC,eco2.c_str(), my_eCO2, dust.c_str(),PM2_5_Conc, temp.c_str(),temperature, press.c_str(),pressure,hum.c_str(), humidity);
			mac_terminal.printf("{%s:%s, %s: %s, %s: %s, %s: %s, %s: %s, %s: %s, %s: %s, %s: %s, %s:%s}", nh3.c_str(),nh3_val.c_str(),co.c_str(),co_val.c_str(), no2.c_str(),no2_val.c_str(),tvoc.c_str(),tvoc_val.c_str(),eco2.c_str(),eco2_val.c_str(), dust.c_str(),dust_val.c_str(), temp.c_str(),temp_val.c_str(), press.c_str(),press_val.c_str(),hum.c_str(), hum_val.c_str());
			mac_terminal.printf("\r\n"); 
			//phone.printf("{  %s: \"%.2f\", %s: \"%.2f\", %s: \"%.2f\", %s: \"" "%" PRIu16 "\", %s: \"" "%" PRIu16 "\", %s: \"%4.1f\", %s: \"%.2f\", %s: \"%.4f\", %s: \"%.4f\" }",  nh3.c_str(),_NH3,co.c_str(),_CO, no2.c_str(),_NO2,tvoc.c_str(), my_TVOC,eco2.c_str(), my_eCO2, dust.c_str(),PM2_5_Conc, temp.c_str(),temperature, press.c_str(),pressure,hum.c_str(), humidity);
			//phone.printf("{  \"NH3\": \"%.2f\", \"CO\": \"%.2f\", \"NO2\": \"%.2f\", \"TVOC\": \"" "%" PRIu16 "\", \"eCO2\": \"" "%" PRIu16 "\", \"Dust_Concentration\": \"%4.1f\", \"Temperature\": \"%.2f\", \"Pressure\": \"%.4f\", \"Humidity\": \"%.4f\" }", _NH3, _CO, _NO2, my_TVOC, my_eCO2, PM2_5_Conc, temperature, pressure, humidity); 
			phone.printf("{%s:%s, %s: %s, %s: %s, %s: %s, %s: %s, %s: %s, %s: %s, %s: %s, %s:%s}", nh3.c_str(),nh3_val.c_str(),co.c_str(),co_val.c_str(), no2.c_str(),no2_val.c_str(),tvoc.c_str(),tvoc_val.c_str(),eco2.c_str(),eco2_val.c_str(), dust.c_str(),dust_val.c_str(), temp.c_str(),temp_val.c_str(), press.c_str(),press_val.c_str(),hum.c_str(), hum_val.c_str());
			phone.printf("\r\n"); 
			//phone.printf("{nh3:%s}",nh3_val);
			
			
			value = cpu.update();    // retrieves the usage value
			pc.printf("CPU %i", value);  // send the value over serial.. if you want.
			ThisThread::sleep_for(5000); // 5 second delay on each reading

	}
}




string encrypt_main(string s){
	//char m[50];
	int i=0;
	long int pt, ct, key = e[0], k,j;
	//const char* msg = s;
    int len1 = s.length();
	char* m1 = new char[len1];
	strncpy(m1, s.c_str(), len1);
	string finals = ""; 
	long int temp[len1];
	while (i != len1)
    {
        pt = m1[i];
        pt = pt - 96;
        k = 1;
        for (j = 0; j < key; j++)
        {
            k = k * pt;
            k = k % n;
        }
        
        temp[i] = k;
        //cout << k<<"\n";
        //ct = k + 96;
        //en[i] = ct;
        //en[i] = k;
		i++;
    }
    for (int z = 0; z < len1; z++) { 
      finals = finals + to_string(temp[z]);
	  finals = finals+"a"; 
    }
    finals = finals.substr(0,finals.length()-1);
    //en[i] = -1;
    //for (i = 0; i< len1; i++)
      //  printf("%d\n", temp[i]);
    //cout << "\nTHE ENCRYPTED MESSAGE IS\n";
    //for (i = 0; en[i] != -1; i++)
      //  printf("%c", en[i]);
	
	
    //string encrypted = encrypt(m1);
    return finals;
}
void set_encrypt_key()
{
   //cout << "\nENTER FIRST PRIME NUMBER\n";
   //cin >> x;

   
	x=5;
	y=13;
   
      
   n = x * y;
   t = (x - 1) * (y - 1);

   encryption_key();
   //cout << "\nPOSSIBLE VALUES OF e AND d ARE\n";

   //for(i = 0; i < j - 1; i++)
     // cout << "\n" << e[i] << "\t" << d[i];

   //encrypt();
   //decrypt();
   //return 0;
} //end of the main program

int prime(long int pr)
{
   int i;
   long int j;
   j = sqrt(pr);
   for(i = 2; i <= j; i++)
   {
      if(pr % i == 0)
         return 0;
   }
   return 1;
 }

//function to generate encryption key
void encryption_key()
{
   int k;
   long int flag;
   k = 0;
   int i;
   for(i = 2; i < t; i++)
   {
      if(t % i == 0)
         continue;
      flag = prime(i);
      if(flag == 1 && i != x && i != y)
      {
         e[k] = i;
         flag = cd(e[k]);
         if(flag > 0)
         {
            d[k] = flag;
            k++;
         }
         if(k == 99)
         break;
      }
   }
}

long int cd(long int a)
{
   long int k = 1;
   while(1)
   {
      k = k + t;
      if(k % a == 0)
         return(k/a);
   }
}

//function to decrypt the message



//ending encryption




