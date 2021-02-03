//**********************
// BME280.cpp for mbed
//
// BME280 bme280(P0_5,P0_4);
// or
// I2C i2c(P0_5,P0_4);
// BME280 bme280(i2c);
//
// (C)Copyright 2015 All rights reserved by Y.Onodera
// http://einstlab.web.fc2.com
//**********************

#include "mbed.h"
#include "BME280.h"

BME280::BME280 (PinName sda, PinName scl) : _i2c(sda, scl) {
    init();
}
BME280::BME280 (I2C& p_i2c) : _i2c(p_i2c) {
    init();
}


unsigned char BME280::get(unsigned char a)
{
    
    buf[0] = a;  // register
    _i2c.write(BME280_ADDR, buf, 1);   // with stop
    // get data
    _i2c.read( BME280_ADDR, buf, 1);
    return buf[0];

}

void BME280::set(unsigned char a, unsigned char b)
{
    
    buf[0] = a;  // register
    buf[1] = b;
    _i2c.write(BME280_ADDR, buf, 2);   // with stop

}


void BME280::getALL()
{
    // set ctrl_meas : forced mode
    set(ctrl_meas, 0x25);

    // wait 11.5ms for forced mode
    wait_ms(12);
    
    // wait status
    if(get(status) != 0)wait_ms(1);

    // get temp
    temp.XLSB = get(temp_xlsb);
    temp.LSB = get(temp_lsb);
    temp.MSB = get(temp_msb);
    temp.dummy = 0;

    // get press
    press.XLSB = get(press_xlsb);
    press.LSB = get(press_lsb);
    press.MSB = get(press_msb);
    press.dummy = 0;

    // get hum
    hum.LSB = get(hum_lsb);
    hum.MSB = get(hum_msb);


    // compensation
    t = BME280_compensate_T_int32(temp.s32>>12);
    p = BME280_compensate_P_int64(press.s32>>12);
    h = BME280_compensate_H_int32((signed int)hum.u16);
}

unsigned int BME280::humidity()
{

    // get hum
    getALL();
    return h;
    
}

signed int BME280::temperature()
{

    // get temp
    getALL();
    return t;
    
}

unsigned int BME280::pressure()
{

    // get hum
    getALL();
    return p;
    
}

void BME280::init()
{
    
    // get calibrations
    calib.LSB = get(calib00);
    calib.MSB = get(calib01);
    dig_T1 = calib.u16;
    calib.LSB = get(calib02);
    calib.MSB = get(calib03);
    dig_T2 = calib.s16;
    calib.LSB = get(calib04);
    calib.MSB = get(calib05);
    dig_T3 = calib.s16;
    calib.LSB = get(calib06);
    calib.MSB = get(calib07);
    dig_P1 = calib.u16;
    calib.LSB = get(calib08);
    calib.MSB = get(calib09);
    dig_P2 = calib.s16;
    calib.LSB = get(calib10);
    calib.MSB = get(calib11);
    dig_P3 = calib.s16;
    calib.LSB = get(calib12);
    calib.MSB = get(calib13);
    dig_P4 = calib.s16;
    calib.LSB = get(calib14);
    calib.MSB = get(calib15);
    dig_P5 = calib.s16;
    calib.LSB = get(calib16);
    calib.MSB = get(calib17);
    dig_P6 = calib.s16;
    calib.LSB = get(calib18);
    calib.MSB = get(calib19);
    dig_P7 = calib.s16;
    calib.LSB = get(calib20);
    calib.MSB = get(calib21);
    dig_P8 = calib.s16;
    calib.LSB = get(calib22);
    calib.MSB = get(calib23);
    dig_P9 = calib.s16;
    dig_H1 = get(calib25);
    calib.LSB = get(calib26);
    calib.MSB = get(calib27);
    dig_H2 = calib.s16;
    dig_H3 = get(calib28);
    calib.MSB = get(calib29);
    calib.LSB = get(calib30) << 4;
    dig_H4 = calib.s16>>4;
    calib.LSB = get(calib30);
    calib.MSB = get(calib31);
    dig_H5 = calib.s16>>4;
    dig_H6 = get(calib32);

    // Set configuration
    set(config, 0x00);
    set(ctrl_hum, 0x01);

}

// Returns temperature in DegC, resolution is 0.01 DegC. Output value of "5123" equals 51.23 DegC.
// t_fine carries fine temperature as global value
BME280_S32_t BME280::BME280_compensate_T_int32(BME280_S32_t adc_T)
{
    BME280_S32_t var1, var2, T;
    var1  = ((((adc_T>>3) - ((BME280_S32_t)dig_T1<<1))) * ((BME280_S32_t)dig_T2)) >> 11;
    var2  = (((((adc_T>>4) - ((BME280_S32_t)dig_T1)) * ((adc_T>>4) - ((BME280_S32_t)dig_T1))) >> 12) *
        ((BME280_S32_t)dig_T3)) >> 14;
    t_fine = var1 + var2;
    T  = (t_fine * 5 + 128) >> 8;
    return T;
}

// Returns pressure in Pa as unsigned 32 bit integer in Q24.8 format (24 integer bits and 8 fractional bits).
// Output value of "24674867" represents 24674867/256 = 96386.2 Pa = 963.862 hPa
BME280_U32_t BME280::BME280_compensate_P_int64(BME280_S32_t adc_P)
{
    BME280_S64_t var1, var2, p;
    var1 = ((BME280_S64_t)t_fine) - 128000;
    var2 = var1 * var1 * (BME280_S64_t)dig_P6;
    var2 = var2 + ((var1*(BME280_S64_t)dig_P5)<<17);
    var2 = var2 + (((BME280_S64_t)dig_P4)<<35);
    var1 = ((var1 * var1 * (BME280_S64_t)dig_P3)>>8) + ((var1 * (BME280_S64_t)dig_P2)<<12);
    var1 = (((((BME280_S64_t)1)<<47)+var1))*((BME280_S64_t)dig_P1)>>33;
    if (var1 == 0)
    {
        return 0; // avoid exception caused by division by zero
    }
    p = 1048576-adc_P;
    p = (((p<<31)-var2)*3125)/var1;
    var1 = (((BME280_S64_t)dig_P9) * (p>>13) * (p>>13)) >> 25;
    var2 = (((BME280_S64_t)dig_P8) * p) >> 19;
    p = ((p + var1 + var2) >> 8) + (((BME280_S64_t)dig_P7)<<4);
    return (BME280_U32_t)p;
}

// Returns humidity in %RH as unsigned 32 bit integer in Q22.10 format (22 integer and 10 fractional bits).
// Output value of "47445" represents 47445/1024 = 46.333 %RH
BME280_U32_t BME280::BME280_compensate_H_int32(BME280_S32_t adc_H)
{
    BME280_S32_t v_x1_u32r;
    v_x1_u32r = (t_fine - ((BME280_S32_t)76800));
    v_x1_u32r = (((((adc_H << 14) - (((BME280_S32_t)dig_H4) << 20) - (((BME280_S32_t)dig_H5) * v_x1_u32r)) +
        ((BME280_S32_t)16384)) >> 15) * (((((((v_x1_u32r * ((BME280_S32_t)dig_H6)) >> 10) * (((v_x1_u32r *
        ((BME280_S32_t)dig_H3)) >> 11) + ((BME280_S32_t)32768))) >> 10) + ((BME280_S32_t)2097152)) *
        ((BME280_S32_t)dig_H2) + 8192) >> 14));
    v_x1_u32r = (v_x1_u32r - (((((v_x1_u32r >> 15) * (v_x1_u32r >> 15)) >> 7) * ((BME280_S32_t)dig_H1)) >> 4));
    v_x1_u32r = (v_x1_u32r < 0 ? 0 : v_x1_u32r);
    v_x1_u32r = (v_x1_u32r > 419430400 ? 419430400 : v_x1_u32r);
    return (BME280_U32_t)(v_x1_u32r>>12); 
}

