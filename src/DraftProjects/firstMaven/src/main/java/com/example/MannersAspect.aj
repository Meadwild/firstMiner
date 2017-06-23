package com.example;

import java.io.File;
import org.apache.commons.lang3.time.*;

public aspect MannersAspect {
        pointcut callSayMessage() : call(public * File.getAbsolutePath(..));
        before() : callSayMessage() {
                System.out.println("Good day!");
        }
        after() : callSayMessage() {
                System.out.println("Thank you!");
        }
}