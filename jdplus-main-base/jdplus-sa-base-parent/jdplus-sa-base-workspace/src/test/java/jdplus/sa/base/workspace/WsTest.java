/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.sa.base.workspace;

import java.io.IOException;
import jdplus.sa.base.api.SaManager;


/**
 *
 * @author Jean Palate
 */
public class WsTest {
    
    public WsTest() {
    }

    public static void main(String[] args) throws IOException{
        
        SaManager.reload();
        Ws ws = Ws.open("c:\\cruncher\\workspaces\\XM.xml");
        ws.computeAll();
    }
}
