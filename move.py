#!/usr/bin/env python3
import ev3dev.ev3 as ev3
#NEEDS TESTING WITH CURRENT BASE ROBOT

fr = ev3.LargeMotor('outA');#Front Right wheel to port A
fl = ev3.LargeMotor('outB');#Front Left wheel to port B
br = ev3.LargeMotor('outC');#Back Right wheel to port C
bl = ev3.LargeMotor('outD');#Back Left wheel to port D
fr.stop_action = 'hold'
fl.stop_action = 'hold'
br.stop_action = 'hold'
bl.stop_action = 'hold'

def forward(speed, time):
    #Move robot forward at speed(tacho counts per second) for time(milliseconds)
    fr.run_timed(speed_sp = speed, time_sp = time);
    fl.run_timed(speed_sp = speed, time_sp = time);
    br.run_timed(speed_sp = -speed, time_sp = time);
    bl.run_timed(speed_sp = -speed, time_sp = time);
    return

def back(speed, time):
    #Move robot back at speed(tacho counts per second) for time(milliseconds)
    forward(-speed,time)
    return

def right(speed, time):
    #Move robot right at speed(tacho counts per second) for time(milliseconds)
    fr.run_timed(speed_sp = -speed, time_sp = time);
    fl.run_timed(speed_sp = speed, time_sp = time);
    br.run_timed(speed_sp = -speed, time_sp = time);
    bl.run_timed(speed_sp = speed, time_sp = time);
    return

def left(speed, time):
    #Move robot left at speed(tacho counts per second) for time(milliseconds)
    right(-speed,time)
    return

def rotr(speed,time):
    #Rotate robot clockwise(right) at speed(tacho counts per second) for time(milliseconds)
    fr.run_timed(speed_sp = -speed, time_sp = time);
    fl.run_timed(speed_sp = speed, time_sp = time);
    br.run_timed(speed_sp = speed, time_sp = time);
    bl.run_timed(speed_sp = -speed, time_sp = time);
    return

def rotl(speed,time):
    #Rotate robot anticlockwise(left) at speed(tacho counts per second) for time(milliseconds)
    rotr(-speed,time)
    return

def fright(speed,time):
    #Move diagonally front right
    fr.run_timed(speed_sp = 0, time_sp = time);
    fl.run_timed(speed_sp = speed, time_sp = time);
    br.run_timed(speed_sp = -speed, time_sp = time);
    bl.run_timed(speed_sp = 0, time_sp = time);
    return

def bleft(speed,time):
    #Move diagonally back left
    fright(-speed,time)
    return

def fleft(speed,time):
    #Move diagonally front left
    fr.run_timed(speed_sp = speed, time_sp = time);
    fl.run_timed(speed_sp = 0, time_sp = time);
    br.run_timed(speed_sp = 0, time_sp = time);
    bl.run_timed(speed_sp = -speed, time_sp = time);
    return

def bright(speed,time):
    fleft(-speed,time)
    return

def stop():
    fr.stop();
    fl.stop();
    br.stop();
    bl.stop();
    return

def square(speed,time):
    forward(speed,time)
    if(fr.wait_until('holding')):
        left(speed,time)
        if(fr.wait_until('holding')):
            back(speed,time)
            if(fr.wait_until('holding')):
                right(speed,time)
       

    ir = ev3.InfraredSensor('in3')
    bs = ev3.BeaconSeeker(sensor = ir,channel = 1)
    us = ev3.UltrasonicSensor('in4')
    us.mode = 'US-DIST-CM'
    #rs = ev3.RemoteControl(sensor = ir, channel = 1)
    while(True):
        print('ir = '+str(bs.distance)+', us = '+str(us.value()))
        #if(us.value()<500):
            #stop()
            #ev3.Sound.beep(args = '-l 500')
        #if(rs.red_down or rs.red_up or rs.blue_up or rs.blue_down):
            #stop()
            #break
        #else
        if(bs.distance>30 and abs(bs.heading)<4):
            forward(500,100)
        elif( bs.heading>4):
            rotl(500,100)
        elif(bs.heading<-4):
            rotr(500,100)
        else:
            continue

