//**********************
// BME280.h for mbed
//
// (C)Copyright 2015 All rights reserved by Y.Onodera
// http://einstlab.web.fc2.com
//**********************
#ifndef BME280_H_
#define BME280_H_

#define BME280_ADDR            0xEC
#define hum_lsb     0xFE
#define hum_msb     0xFD
#define temp_xlsb   0xFC
#define temp_lsb    0xFB
#define temp_msb    0xFA
#define press_xlsb  0xF9
#define press_lsb   0xF8
#define press_msb   0xF7
#define config      0xF5
#define ctrl_meas   0xF4
#define status      0xF3
#define ctrl_hum    0xF2
#define calib41     0xF0
#define calib40     0xEF
#define calib39     0xEE
#define calib38     0xED
#define calib37     0xEC
#define calib36     0xEB
#define calib35     0xEA
#define calib34     0xE9
#define calib33     0xE8
#define calib32     0xE7
#define calib31     0xE6
#define calib30     0xE5
#define calib29     0xE4
#define calib28     0xE3
#define calib27     0xE2
#define calib26     0xE1
#define reset       0xE0
#define id          0xD0
#define calib25     0xA1
#define calib24     0xA0
#define calib23     0x9F
#define calib22     0x9E
#define calib21     0x9D
#define calib20     0x9C
#define calib19     0x9B
#define calib18     0x9A
#define calib17     0x99
#define calib16     0x98
#define calib15     0x97
#define calib14     0x96
#define calib13     0x95
#define calib12     0x94
#define calib11     0x93
#define calib10     0x92
#define calib09     0x91
#define calib08     0x90
#define calib07     0x8F
#define calib06     0x8E
#define calib05     0x8D
#define calib04     0x8C
#define calib03     0x8B
#define calib02     0x8A
#define calib01     0x89
#define calib00     0x88

#include "mbed.h"

// see BST-BME280-DS001-10
#define BME280_S32_t    signed int
#define BME280_S64_t    signed long long
#define BME280_U32_t    unsigned int


union WORD32 {
    signed int  s32;
    unsigned int    u32;
    struct {
        unsigned char dummy;
        unsigned char XLSB;
        unsigned char LSB;
        unsigned char MSB;
    };
};

union WORD16 {
    signed short    s16;
    unsigned short  u16;
    struct {
        unsigned char   LSB;
        unsigned char   MSB;
    };
};


class BME280{
public:
    BME280 (PinName sda, PinName scl);
    BME280 (I2C& p_i2c);

    void set(unsigned char a,unsigned char b);
    unsigned char get(unsigned char a);
    void getALL();
    unsigned int humidity();
    signed int temperature();
    unsigned int pressure();
    void init();

protected:
    
    I2C _i2c;

    union WORD16 calib;
    union WORD16 hum;
    union WORD32 temp;
    union WORD32 press;
    char buf[8];

    unsigned short dig_T1;
    signed short dig_T2;
    signed short dig_T3;
    unsigned short dig_P1;
    signed short dig_P2;
    signed short dig_P3;
    signed short dig_P4;
    signed short dig_P5;
    signed short dig_P6;
    signed short dig_P7;
    signed short dig_P8;
    signed short dig_P9;
    unsigned char dig_H1;
    signed short dig_H2;
    unsigned char dig_H3;
    signed short dig_H4;
    signed short dig_H5;
    signed char dig_H6;

    BME280_S32_t t_fine;
    BME280_S32_t BME280_compensate_T_int32(BME280_S32_t adc_T);
    BME280_U32_t BME280_compensate_P_int64(BME280_S32_t adc_P);
    BME280_U32_t BME280_compensate_H_int32(BME280_S32_t adc_H);
    unsigned int t;
    signed int p;
    signed int h;
};

#endif /* BME280_H_ */


