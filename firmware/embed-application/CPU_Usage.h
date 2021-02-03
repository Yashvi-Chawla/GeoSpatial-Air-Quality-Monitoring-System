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
 
#ifndef CPU_USAGE_H
#define CPU_USAGE_H
 
 
class CPU_Usage {
    
    private:
    
    // Declare Private variables ////
    Timer *_t;
    float _active;
    float _inactive;
    float _time_ballast;
    
    public:
    
    // Constructor //////////////////
    CPU_Usage( Timer &_t , float time_ballast);    
    
    // Class Function Prototypes ////
    void    working (void);
    void    stopped (void);
    uint8_t update  (void);
    void    delay   (float duration);
    
    // Destructor //////////////////
    ~CPU_Usage(void);
    
};
 
 
 
#endif
