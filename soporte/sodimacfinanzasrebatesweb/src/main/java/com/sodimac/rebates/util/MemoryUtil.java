package com.sodimac.rebates.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(MemoryUtil.class);

	public static void showMemoryStats() {
        int mb = 1024*1024;
                       
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        
        logger.info("##### Heap utilization statistics [MB] #####");
                 
        //Print used memory
        logger.info("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        
        //Print free memory
        logger.info("Free Memory:" + runtime.freeMemory() / mb);
        
        //Print total available memory
        logger.info("Total Memory:" + runtime.totalMemory() / mb);
        
        //Print Maximum available memory
        logger.info("Max Memory:" + runtime.maxMemory() / mb);
        
    }
    
    public static long tiempoInicial(){
        return System.currentTimeMillis();
    }
    
    public static long tiempoFinal(long tiempoInicial){
        long tiempoFinal = System.currentTimeMillis();
        return tiempoFinal - tiempoInicial;
    }
}
