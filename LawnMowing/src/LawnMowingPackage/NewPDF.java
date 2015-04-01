/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LawnMowingPackage;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Alex
 */
public class NewPDF extends File {

    public NewPDF(String fileName) {
        super(fileName + ".pdf");

        if (this.createNewFile()) {
            System.out.println("File created");
        }

    }

    @Override
    public boolean createNewFile() {
        try {
            super.createNewFile();

            return true;
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
            return false;
        }
    }

}
