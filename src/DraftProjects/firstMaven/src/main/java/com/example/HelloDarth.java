package com.example;

import org.apache.commons.lang3.time.*;

import java.io.File;
import java.sql.Date;

/**
 * Created by Дмитрий on 22.06.2017.
 */
public class HelloDarth {

    public static void main(String[] args) {

        String repositoryStorageFolder="E:\\Repositories";

        File dir = new File(repositoryStorageFolder);

        dir.getAbsolutePath();

        System.out.println("Hello, Darth");

        //DateFormatUtils.format(new Date(0), "yyyyMMdd");
    }

}
