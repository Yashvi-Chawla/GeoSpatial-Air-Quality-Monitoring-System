/******************************************************
    CPU_Usage Library V1.0   Ian Weston  15.06.2014
    CPU_Usage Library V1.01  Ian Weston  08.01.2016
    
A very lightweight tool for calculating the CPU usage
of the mbed micro. Based in the time domain, this will
calculate the percentage of CPU time used.
 
Please see the project page for more details.
 
Enjoy!
 
******************************************************/
#include "mbed.h"
#include "CPU_Usage.h"
 
 
// Constructor - time_ballast can be considered as a smoothing
// number, representing time to reset all data. 1 seconds is
// a good place to start.
CPU_Usage::CPU_Usage(Timer &t, float time_ballast) {
    _t = &t;
    _active = 0;
    _inactive = 0;
    _time_ballast = time_ballast;
    }
    
// Use this function when isolating precise areas of code for measurement
// place it at the start of the code of interest    
void CPU_Usage::working(void) {
    _t->stop();
    _inactive += _t->read();
    _t->reset();
    _t->start();
    }
    
// Use this function when isolating precise areas of code for measurement
// place this at the end of the code thats of interest
void CPU_Usage::stopped(void) {
    _t->stop();
    _active += _t->read();
    _t->reset();
    _t->start();
    }
    
    
// call this to return the usage value. returns the value as a percentage
uint8_t CPU_Usage::update(void) {
    float total = _active + _inactive;
    float percentile = 100 / total;
    uint8_t usage = _active * percentile;
    
    if (total > _time_ballast) {
        _active = 0;
        _inactive = 0;
        }
    
    return usage;
    }    
    
 
// use this function instead of wait() to exclude CPU wait processing from the value.
void CPU_Usage::delay(float duration) {
    this->stopped();
    wait(duration);
    this->working();
    }
 
// Destructor... empty... as you can see...
CPU_Usage::~CPU_Usage(void) {
    
    }
 
